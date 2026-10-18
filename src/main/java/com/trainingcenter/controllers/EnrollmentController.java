package com.trainingcenter.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentListViewDTO;
import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.services.enrollment.EnrollmentService;
import com.trainingcenter.services.payment.PaymentService;
import com.trainingcenter.services.student.StudentService;
import com.trainingcenter.services.trainingClass.TrainingClassService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

	private final EnrollmentService enrollmentService;
	private final StudentService studentService;
	private final TrainingClassService trainingClassService;
	private final PaymentService paymentService;

	public EnrollmentController(EnrollmentService enrollmentService, StudentService studentService,
			TrainingClassService trainingClassService, PaymentService paymentService) {
		this.enrollmentService = enrollmentService;
		this.studentService = studentService;
		this.trainingClassService = trainingClassService;
		this.paymentService=paymentService;
	}

	@GetMapping
	public String list(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "paymentStatus", required = false) PaymentStatus paymentStatus,
			@RequestParam(name = "page", defaultValue = "0") int page, Model model) throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").descending());
		Page<EnrollmentListViewDTO> result = enrollmentService.getEnrollmentListView(keyword, paymentStatus, pageable);

		model.addAttribute("enrollments", result.getContent());
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("pageSize", result.getSize());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("totalItems", result.getTotalElements());
		model.addAttribute("hasPrevious", result.hasPrevious());
		model.addAttribute("hasNext", result.hasNext());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		model.addAttribute("paymentStatus", paymentStatus);
		model.addAttribute("paymentStatuses", PaymentStatus.values());
		return "modules/enrollment/list";
	}
	
	@GetMapping("/details/{id}")
	public String details(@PathVariable Long id, Model model) throws Exception {
		StudentDTO student = studentService.getStudentById(id);
		model.addAttribute("student", student);
		model.addAttribute("enrollments", enrollmentService.getByStudentId(id));
		model.addAttribute("payments", paymentService.getByStudentId(id));
		return "modules/student/details";
	}

	@GetMapping("/create")
	public String createForm(Model model) throws Exception {
		loadFormOptions(model);
		model.addAttribute("enrollment", new EnrollmentDTO());
		return "modules/enrollment/form";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		loadFormOptions(model);
		model.addAttribute("enrollment", enrollmentService.getEnrollmentById(id));
		return "modules/enrollment/form";
	}

	@GetMapping("/change-time/{id}")
	public String changeTimeForm(@PathVariable Long id, Model model) throws Exception {
		EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
		Long courseId = resolveCourseId(enrollment);
		List<TrainingClassDTO> sameCourseClasses = trainingClassService.getAllTrainingClasses().stream()
				.filter(c -> c.getCourseId() != null && c.getCourseId().equals(courseId))
				.toList();
		model.addAttribute("enrollment", enrollment);
		model.addAttribute("sameCourseClasses", sameCourseClasses);
		return "modules/enrollment/change-time";
	}

	@PostMapping("/change-time/{id}")
	public String changeTime(@PathVariable Long id,
			@RequestParam(name = "trainingClassId") Long trainingClassId,
			RedirectAttributes redirectAttributes) {
		try {
			enrollmentService.changeClassTime(id, trainingClassId);
			redirectAttributes.addFlashAttribute("success", "Class time changed successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/students";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("enrollment") EnrollmentDTO dto, RedirectAttributes redirectAttributes) {
		try {
			enrollmentService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Enrollment saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/enrollments";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			enrollmentService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Enrollment deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "enrollment", "payment records for this enrollment"));
		}
		return "redirect:/enrollments";
	}

	private void loadFormOptions(Model model) throws Exception {
		model.addAttribute("students", studentService.getAllStudents());
		model.addAttribute("classes", trainingClassService.getAllTrainingClasses());
		model.addAttribute("statuses", EnrollStatus.values());
	}

	private Long resolveCourseId(EnrollmentDTO enrollment) throws Exception {
		TrainingClassDTO currentClass = trainingClassService.getTrainingClassById(enrollment.getTrainingClassId());
		return currentClass.getCourseId();
	}
}
