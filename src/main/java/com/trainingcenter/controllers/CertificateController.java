package com.trainingcenter.controllers;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.CertificateDTO;
import com.trainingcenter.dto.CourseDTO;
import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentPaymentSummaryDTO;
import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.services.certificate.CertificateService;
import com.trainingcenter.services.enrollment.EnrollmentService;
import com.trainingcenter.services.payment.PaymentService;
import com.trainingcenter.services.course.CourseService;
import com.trainingcenter.services.student.StudentService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/certificates")
public class CertificateController {

	private final CertificateService certificateService;
	private final StudentService studentService;
	private final EnrollmentService enrollmentService;
	private final PaymentService paymentService;
	private final CourseService courseService;

	public CertificateController(CertificateService certificateService, StudentService studentService,
			EnrollmentService enrollmentService, PaymentService paymentService, CourseService courseService) {
		this.certificateService = certificateService;
		this.studentService = studentService;
		this.enrollmentService = enrollmentService;
		this.paymentService = paymentService;
		this.courseService = courseService;
	}

	@GetMapping
	public String list(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").descending());
		Page<CertificateDTO> result = certificateService.search(keyword, pageable);
		model.addAttribute("certificates", result.getContent());
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("pageSize", result.getSize());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("totalItems", result.getTotalElements());
		model.addAttribute("hasPrevious", result.hasPrevious());
		model.addAttribute("hasNext", result.hasNext());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		return "modules/certificate/list";
	}

	@GetMapping("/create")
	public String createForm(@RequestParam(name = "studentId", required = false) Long studentId, Model model)
			throws Exception {
		CertificateDTO certificate = new CertificateDTO();
		certificate.setStudentId(studentId);
		loadFormOptions(model, certificate);
		model.addAttribute("certificate", certificate);
		return "modules/certificate/form";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		CertificateDTO certificate = certificateService.getById(id);
		loadFormOptions(model, certificate);
		model.addAttribute("certificate", certificate);
		return "modules/certificate/form";
	}

	@GetMapping("/details/{id}")
	public String details(@PathVariable Long id, Model model) throws Exception {
		model.addAttribute("certificate", certificateService.getById(id));
		return "modules/certificate/details";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("certificate") CertificateDTO dto, RedirectAttributes redirectAttributes) {
		try {
			CertificateDTO saved = certificateService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Certificate saved successfully!");
			return "redirect:/certificates/details/" + saved.getId();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return dto.getId() != null ? "redirect:/certificates/edit/" + dto.getId()
					: "redirect:/certificates/create" + (dto.getStudentId() != null ? "?studentId=" + dto.getStudentId() : "");
		}
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			certificateService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Certificate deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "certificate", "records that refer to this certificate"));
		}
		return "redirect:/certificates";
	}

	@GetMapping("/serial")
	@ResponseBody
	public Map<String, String> nextSerial(@RequestParam("courseId") Long courseId) throws Exception {
		Map<String, String> response = new LinkedHashMap<>();
		response.put("serial", certificateService.generateNextSerialByCourse(courseId));
		return response;
	}

	private void loadFormOptions(Model model, CertificateDTO currentCertificate) throws Exception {
		List<EnrollmentDTO> eligibleEnrollments = new ArrayList<>();
		Set<Long> eligibleStudentIds = new HashSet<>();
		Set<Long> eligibleCourseIds = new HashSet<>();
		for (EnrollmentDTO enrollment : enrollmentService.getAllEnrollments()) {
			if (enrollment.getStudentId() == null || enrollment.getCourseId() == null) {
				continue;
			}
			EnrollmentPaymentSummaryDTO summary = paymentService.getEnrollmentPaymentSummary(enrollment.getId(), null);
			boolean fullyPaid = summary.getRemainingBalance() != null
					&& summary.getRemainingBalance().signum() <= 0;
			boolean currentCertificateCourse = currentCertificate.getId() != null
					&& Objects.equals(currentCertificate.getStudentId(), enrollment.getStudentId())
					&& Objects.equals(currentCertificate.getCourseId(), enrollment.getCourseId());
			boolean alreadyCertified = certificateService.existsByStudentAndCourse(
					enrollment.getStudentId(), enrollment.getCourseId());
			if (fullyPaid && (!alreadyCertified || currentCertificateCourse)) {
				eligibleEnrollments.add(enrollment);
				eligibleStudentIds.add(enrollment.getStudentId());
				eligibleCourseIds.add(enrollment.getCourseId());
			}
		}
		List<StudentDTO> eligibleStudents = studentService.getAllStudents().stream()
				.filter(student -> eligibleStudentIds.contains(student.getId())).toList();
		Map<Long, CourseDTO> eligibleCourses = courseService.getAllCourses().stream()
				.filter(course -> eligibleCourseIds.contains(course.getId()))
				.collect(java.util.stream.Collectors.toMap(CourseDTO::getId, course -> course));
		model.addAttribute("students", eligibleStudents);
		model.addAttribute("eligibleEnrollments", eligibleEnrollments);
		model.addAttribute("eligibleCourses", eligibleCourses);
	}
}
