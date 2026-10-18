package com.trainingcenter.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.EnrollmentFormDTO;
import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;
import com.trainingcenter.enums.ScheduleType;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.services.enrollment.EnrollmentWizardService;
import com.trainingcenter.services.student.StudentService;
import com.trainingcenter.services.trainingClass.TrainingClassService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/enrollments/enroll")
public class EnrollmentWizardController {

	private static final String SESSION_KEY = "ENROLLMENT_WIZARD_FORM";
	private static final String VIEW = "modules/enrollment/wizard";

	private final TrainingClassService trainingClassService;
	private final StudentService studentService;
	private final EnrollmentWizardService enrollmentWizardService;

	public EnrollmentWizardController(TrainingClassService trainingClassService, StudentService studentService,
			EnrollmentWizardService enrollmentWizardService) {
		this.trainingClassService = trainingClassService;
		this.studentService = studentService;
		this.enrollmentWizardService = enrollmentWizardService;
	}

	@GetMapping
	public String start() {
		return "redirect:/enrollments/enroll/step1";
	}

	@GetMapping("/step1")
	public String step1(HttpSession session, Model model) throws Exception {
		EnrollmentFormDTO form = getOrCreateForm(session);
		renderStep(model, form, 1);
		return VIEW;
	}

	@PostMapping("/step1/next")
	public String step1Next(@ModelAttribute("wizardForm") EnrollmentFormDTO submitted, BindingResult bindingResult,
			HttpSession session, Model model) throws Exception {
		validateStep1(submitted, bindingResult);
		if (bindingResult.hasErrors()) {
			renderStep(model, submitted, 1);
			return VIEW;
		}
		EnrollmentFormDTO sessionForm = getOrCreateForm(session);
		copyStep1(submitted, sessionForm);
		session.setAttribute(SESSION_KEY, sessionForm);
		return "redirect:/enrollments/enroll/step2";
	}

	@GetMapping("/step2")
	public String step2(HttpSession session, Model model) throws Exception {
		EnrollmentFormDTO form = getOrCreateForm(session);
		renderStep(model, form, 2);
		return VIEW;
	}

	@PostMapping("/step2/next")
	public String step2Next(@Validated(EnrollmentFormDTO.Step2.class) @ModelAttribute("wizardForm") EnrollmentFormDTO submitted,
			BindingResult bindingResult, HttpSession session, Model model) throws Exception {
		validateClassSelection(submitted, bindingResult);
		if (bindingResult.hasErrors()) {
			renderStep(model, submitted, 2);
			return VIEW;
		}
		EnrollmentFormDTO sessionForm = getOrCreateForm(session);
		copyStep2(submitted, sessionForm);
		session.setAttribute(SESSION_KEY, sessionForm);
		return "redirect:/enrollments/enroll/step3";
	}

	@PostMapping("/step2/back")
	public String step2Back(@ModelAttribute("wizardForm") EnrollmentFormDTO submitted, HttpSession session) {
		EnrollmentFormDTO sessionForm = getOrCreateForm(session);
		copyStep2(submitted, sessionForm);
		session.setAttribute(SESSION_KEY, sessionForm);
		return "redirect:/enrollments/enroll/step1";
	}

	@GetMapping("/step3")
	public String step3(HttpSession session, Model model) throws Exception {
		EnrollmentFormDTO form = getOrCreateForm(session);
		renderStep(model, form, 3);
		return VIEW;
	}

	@PostMapping("/step3/back")
	public String step3Back(@ModelAttribute("wizardForm") EnrollmentFormDTO submitted, HttpSession session) {
		EnrollmentFormDTO sessionForm = getOrCreateForm(session);
		copyStep3(submitted, sessionForm);
		session.setAttribute(SESSION_KEY, sessionForm);
		return "redirect:/enrollments/enroll/step2";
	}

	@PostMapping("/confirm")
	public String confirm(@Validated(EnrollmentFormDTO.Step3.class) @ModelAttribute("wizardForm") EnrollmentFormDTO submitted,
			BindingResult bindingResult, HttpSession session, RedirectAttributes redirectAttributes, Model model)
			throws Exception {
		EnrollmentFormDTO sessionForm = getOrCreateForm(session);
		copyStep3(submitted, sessionForm);

		if (sessionForm.getTrainingClassId() == null) {
			model.addAttribute("error", "Please complete Step 2 and select a class before confirming.");
			renderStep(model, sessionForm, 2);
			return VIEW;
		}

		if (sessionForm.getAmount() == null || sessionForm.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			model.addAttribute("error", "Paid amount must be greater than 0. Enter an amount before confirming enrollment.");
			model.addAttribute("amountValidationError", true);
			renderStep(model, sessionForm, 3);
			return VIEW;
		}

		if (bindingResult.hasErrors()) {
			renderStep(model, sessionForm, 3);
			return VIEW;
		}

		try {
			enrollmentWizardService.saveAll(sessionForm);
			session.removeAttribute(SESSION_KEY);
			redirectAttributes.addFlashAttribute("success", "Enrollment completed successfully!");
			return "redirect:/enrollments";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			renderStep(model, sessionForm, 3);
			return VIEW;
		}
	}

	@GetMapping("/cancel")
	public String cancel(HttpSession session, RedirectAttributes redirectAttributes) {
		session.removeAttribute(SESSION_KEY);
		redirectAttributes.addFlashAttribute("success", "Enrollment cancelled.");
		return "redirect:/enrollments";
	}

	@PostMapping("/cancel")
	public String cancelPost(HttpSession session, RedirectAttributes redirectAttributes) {
		return cancel(session, redirectAttributes);
	}

	private EnrollmentFormDTO getOrCreateForm(HttpSession session) {
		EnrollmentFormDTO form = (EnrollmentFormDTO) session.getAttribute(SESSION_KEY);
		if (form == null) {
			form = new EnrollmentFormDTO();
			session.setAttribute(SESSION_KEY, form);
		}
		return form;
	}

	private void renderStep(Model model, EnrollmentFormDTO form, int currentStep) throws Exception {
		model.addAttribute("wizardForm", form);
		model.addAttribute("currentStep", currentStep);
		model.addAttribute("studentStatuses", StudentStatus.values());
		model.addAttribute("enrollmentStatuses", EnrollStatus.values());
		model.addAttribute("paymentTypes", PaymentType.values());
		model.addAttribute("paymentStatuses", PaymentStatus.values());
		List<ClassOption> classOptions = loadClassOptions();
		model.addAttribute("classOptions", classOptions);
		model.addAttribute("courseOptions", loadCourseOptions(classOptions));
		model.addAttribute("scheduleTypes", ScheduleType.values());
		model.addAttribute("studentOptions", loadStudentOptions());
		populatePaymentPreview(model, form);
	}

	private List<ClassOption> loadClassOptions() throws Exception {
		return trainingClassService.getAllTrainingClasses().stream()
				.sorted(Comparator.comparing(TrainingClassDTO::getId))
				.map(c -> new ClassOption(c.getId(), c.getCourseId(), c.getCourseName(), c.getScheduleType(),
						formatClassTime(c), formatClassSummary(c)))
				.toList();
	}

	private List<CourseOption> loadCourseOptions(List<ClassOption> classes) {
		return classes.stream()
				.collect(java.util.stream.Collectors.toMap(ClassOption::courseId,
						item -> new CourseOption(item.courseId(), item.courseName()), (first, ignored) -> first,
						java.util.LinkedHashMap::new))
				.values().stream().sorted(Comparator.comparing(CourseOption::courseName,
						String.CASE_INSENSITIVE_ORDER)).toList();
	}

	private String formatClassTime(TrainingClassDTO dto) {
		String start = dto.getStartTime() != null ? dto.getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")) : "--:--";
		String end = dto.getEndTime() != null ? dto.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")) : "--:--";
		String room = dto.getRoom() != null && !dto.getRoom().isBlank() ? " | Room: " + dto.getRoom() : "";
		return start + " - " + end + room;
	}

	private String formatClassSummary(TrainingClassDTO dto) {
		return dto.getCourseName() + " (" + dto.getScheduleType().getLabel() + ") - " + formatClassTime(dto);
	}

	private void validateClassSelection(EnrollmentFormDTO submitted, BindingResult bindingResult) {
		if (submitted.getTrainingClassId() == null || submitted.getSelectedCourseId() == null
				|| submitted.getSelectedScheduleType() == null) {
			return;
		}
		try {
			TrainingClassDTO selectedClass = trainingClassService.getTrainingClassById(submitted.getTrainingClassId());
			if (!submitted.getSelectedCourseId().equals(selectedClass.getCourseId())
					|| submitted.getSelectedScheduleType() != selectedClass.getScheduleType()) {
				bindingResult.rejectValue("trainingClassId", "trainingClassId.mismatch",
						"Please choose a class time available for the selected course and schedule.");
			}
		} catch (Exception e) {
			bindingResult.rejectValue("trainingClassId", "trainingClassId.invalid",
					"That class time is no longer available. Please choose another one.");
		}
	}

	private void populatePaymentPreview(Model model, EnrollmentFormDTO form) throws Exception {
		BigDecimal fee = enrollmentWizardService.resolveCourseFee(form.getTrainingClassId());
		BigDecimal discountPercent = normalizeDiscountPercent(form.getDiscountPercent());
		BigDecimal discountedFee = enrollmentWizardService.resolveDiscountedFee(form.getTrainingClassId(), discountPercent);
		BigDecimal paid = form.getAmount() != null ? form.getAmount() : BigDecimal.ZERO;
		BigDecimal remaining = discountedFee.subtract(paid);
		if (remaining.compareTo(BigDecimal.ZERO) < 0) {
			remaining = BigDecimal.ZERO;
		}
		int count = form.getInstallmentCount() != null && form.getInstallmentCount() > 0 ? form.getInstallmentCount() : 2;
		BigDecimal each = count > 0 ? remaining.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : remaining;

		model.addAttribute("courseFee", fee);
		model.addAttribute("discountPercent", discountPercent);
		model.addAttribute("discountedCourseFee", discountedFee);
		model.addAttribute("remainingAmount", remaining);
		model.addAttribute("installmentAmount", each);
		model.addAttribute("installmentCount", count);
	}

	private void copyStep1(EnrollmentFormDTO source, EnrollmentFormDTO target) throws Exception {
		boolean returningStudent = Boolean.TRUE.equals(source.getReturningStudent());
		target.setReturningStudent(returningStudent);
		target.setExistingStudentId(source.getExistingStudentId());
		if (returningStudent) {
			if (source.getExistingStudentId() != null) {
				StudentDTO existingStudent = studentService.getStudentById(source.getExistingStudentId());
				target.setName(existingStudent.getName());
				target.setFatherName(existingStudent.getFatherName());
				target.setDateOfBirth(existingStudent.getDateOfBirth());
				target.setPhoneNumber(existingStudent.getPhoneNumber());
				target.setEmail(existingStudent.getEmail());
				target.setAddress(existingStudent.getAddress());
				target.setStudentStatus(existingStudent.getStatus() != null ? existingStudent.getStatus() : StudentStatus.ACTIVE);
			}
			return;
		}
		target.setName(trimToNull(source.getName()));
		target.setFatherName(trimToNull(source.getFatherName()));
		target.setDateOfBirth(source.getDateOfBirth());
		target.setPhoneNumber(trimToNull(source.getPhoneNumber()));
		target.setEmail(trimToNull(source.getEmail()));
		target.setAddress(trimToNull(source.getAddress()));
		target.setStudentStatus(source.getStudentStatus() != null ? source.getStudentStatus() : StudentStatus.ACTIVE);
	}

	private List<StudentOption> loadStudentOptions() throws Exception {
		return studentService.getAllStudents().stream()
				.sorted(Comparator.comparing(StudentDTO::getId))
				.map(s -> new StudentOption(s.getId(), formatStudentLabel(s)))
				.toList();
	}

	private String formatStudentLabel(StudentDTO dto) {
		String phone = dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank() ? " | " + dto.getPhoneNumber() : "";
		return dto.getName() + phone;
	}

	private void validateStep1(EnrollmentFormDTO submitted, BindingResult bindingResult) {
		boolean returningStudent = Boolean.TRUE.equals(submitted.getReturningStudent());
		if (returningStudent) {
			if (submitted.getExistingStudentId() == null) {
				bindingResult.rejectValue("existingStudentId", "existingStudentId.required",
						"Please select an existing student.");
			} else {
				try {
					StudentDTO existingStudent = studentService.getStudentById(submitted.getExistingStudentId());
					if (existingStudent.getStatus() == StudentStatus.SUSPENDED) {
						bindingResult.rejectValue("existingStudentId", "existingStudentId.suspended",
								"Suspended students cannot enroll in new courses.");
					}
				} catch (Exception ex) {
					bindingResult.rejectValue("existingStudentId", "existingStudentId.invalid",
							"Selected student does not exist.");
				}
			}
			return;
		}
		if (isBlank(submitted.getName())) {
			bindingResult.rejectValue("name", "name.required", "Student name is required.");
		}
		if (isBlank(submitted.getFatherName())) {
			bindingResult.rejectValue("fatherName", "fatherName.required", "Father name is required.");
		}
		if (isBlank(submitted.getPhoneNumber())) {
			bindingResult.rejectValue("phoneNumber", "phoneNumber.required", "Phone number is required.");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private void copyStep2(EnrollmentFormDTO source, EnrollmentFormDTO target) {
		target.setSelectedCourseId(source.getSelectedCourseId());
		target.setSelectedScheduleType(source.getSelectedScheduleType());
		target.setTrainingClassId(source.getTrainingClassId());
		target.setEnrollDate(source.getEnrollDate());
		target.setEnrollmentStatus(
				source.getEnrollmentStatus() != null ? source.getEnrollmentStatus() : EnrollStatus.ACTIVE);
	}

	private void copyStep3(EnrollmentFormDTO source, EnrollmentFormDTO target) {
		target.setAmount(source.getAmount());
		target.setDiscountPercent(normalizeDiscountPercent(source.getDiscountPercent()));
		target.setPaymentDate(source.getPaymentDate());
		target.setPaymentType(source.getPaymentType() != null ? source.getPaymentType() : PaymentType.CASH);
		target.setPaymentStatus(source.getPaymentStatus());
		target.setNote(trimToNull(source.getNote()));
		target.setInstallment(Boolean.TRUE.equals(source.getInstallment()));
		target.setInstallmentCount(source.getInstallmentCount());
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private BigDecimal normalizeDiscountPercent(BigDecimal discountPercent) {
		if (discountPercent == null) {
			return BigDecimal.ZERO;
		}
		if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}
		return discountPercent;
	}

	public record CourseOption(Long courseId, String courseName) {
	}

	public record ClassOption(Long id, Long courseId, String courseName, ScheduleType scheduleType,
			String timeLabel, String summaryLabel) {
	}

	public record StudentOption(Long id, String label) {
	}
}
