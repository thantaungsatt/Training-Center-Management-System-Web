package com.trainingcenter.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import com.trainingcenter.dto.CourseDTO;
import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.dto.PaymentDTO;
import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.services.course.CourseService;
import com.trainingcenter.services.enrollment.EnrollmentService;
import com.trainingcenter.services.payment.PaymentService;
import com.trainingcenter.services.student.StudentService;
import com.trainingcenter.services.trainingClass.TrainingClassService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/students")
public class StudentController {

	private final StudentService studentService;
	private final EnrollmentService enrollmentService;
	private final PaymentService paymentService;
	private final TrainingClassService trainingClassService;
	private final CourseService courseService;

	public StudentController(StudentService studentService, EnrollmentService enrollmentService, PaymentService paymentService,
			TrainingClassService trainingClassService, CourseService courseService) {
		this.studentService = studentService;
		this.enrollmentService = enrollmentService;
		this.paymentService = paymentService;
		this.trainingClassService = trainingClassService;
		this.courseService = courseService;
	}

	@GetMapping
	public String list(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "courseId", required = false) Long courseId,
			@RequestParam(name = "classTimeId", required = false) Long classTimeId,
			@RequestParam(name = "view", defaultValue = "active") String view,
			@RequestParam(name = "page", defaultValue = "0") int page, Model model) throws Exception {
		boolean certificatedOnly = "certificated".equalsIgnoreCase(view);
		String selectedView = certificatedOnly ? "certificated" : "active";
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").descending());
		Page<EnrollmentDTO> result = enrollmentService.searchStudentListRows(keyword, courseId, classTimeId, certificatedOnly, pageable);
		List<TrainingClassDTO> classOptions = trainingClassService.getAllTrainingClasses();
		List<CourseDTO> courseOptions = courseService.getAllCourses();
		List<TrainingClassDTO> classTimeOptions = new ArrayList<>();
		for (TrainingClassDTO option : classOptions) {
			boolean exists = classTimeOptions.stream().anyMatch(existing -> Objects.equals(existing.getStartTime(), option.getStartTime())
					&& Objects.equals(existing.getEndTime(), option.getEndTime()));
			if (!exists) {
				classTimeOptions.add(option);
			}
		}

		model.addAttribute("enrollments", result.getContent());
		model.addAttribute("courseOptions", courseOptions);
		model.addAttribute("classOptions", classOptions);
		model.addAttribute("classTimeOptions", classTimeOptions);
		model.addAttribute("courseId", courseId);
		model.addAttribute("classTimeId", classTimeId);
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("pageSize", result.getSize());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("totalItems", result.getTotalElements());
		model.addAttribute("hasPrevious", result.hasPrevious());
		model.addAttribute("hasNext", result.hasNext());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		model.addAttribute("view", selectedView);
		return "modules/student/list";
	}

//	@GetMapping("/create")
//	public String createForm(Model model) {
//		model.addAttribute("statuses", StudentStatus.values());
//		model.addAttribute("student", new StudentDTO());
//		return "modules/student/form";
//	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		model.addAttribute("statuses", StudentStatus.values());
		model.addAttribute("student", studentService.getStudentById(id));
		return "modules/student/form";
	}

	@GetMapping("/details/{id}")
	public String details(@PathVariable Long id, Model model) throws Exception {
		StudentDTO student = studentService.getStudentById(id);
		model.addAttribute("student", student);
		List<EnrollmentDTO> enrollments = enrollmentService.getByStudentId(id);
		List<PaymentDTO> payments = paymentService.getByStudentId(id);
		Map<Long, String> latestStatusByEnrollment = new LinkedHashMap<>();
		for (PaymentDTO payment : payments) {
			if (payment.getEnrollmentId() != null && payment.getStatus() != null) {
				latestStatusByEnrollment.putIfAbsent(payment.getEnrollmentId(), payment.getStatus().getLabel());
			}
		}
		for (EnrollmentDTO enrollment : enrollments) {
			enrollment.setPaymentStatus(latestStatusByEnrollment.get(enrollment.getId()));
		}
		model.addAttribute("enrollments", enrollments);
		model.addAttribute("payments", payments);
		return "modules/student/details";
	}

	@PostMapping("/status/{id}")
	public String updateStatus(@PathVariable Long id,
			@RequestParam("status") StudentStatus status,
			RedirectAttributes redirectAttributes) {
		try {
			StudentDTO student = studentService.getStudentById(id);
			student.setStatus(status);
			studentService.save(student);
			redirectAttributes.addFlashAttribute("success", "Student status updated to " + status.getLabel() + ".");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/students/details/" + id;
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("student") StudentDTO dto, RedirectAttributes redirectAttributes) {
		try {
			studentService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Student saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/students";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			studentService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "student", "enrollments or payment records for this student"));
		}
		return "redirect:/students";
	}

}
