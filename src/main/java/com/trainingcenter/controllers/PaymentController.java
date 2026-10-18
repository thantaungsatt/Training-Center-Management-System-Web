package com.trainingcenter.controllers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;

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
import com.trainingcenter.dto.EnrollmentPaymentSummaryDTO;
import com.trainingcenter.dto.PaymentDTO;
import com.trainingcenter.enums.PaymentType;
import com.trainingcenter.services.enrollment.EnrollmentService;
import com.trainingcenter.services.payment.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final EnrollmentService enrollmentService;

	public PaymentController(PaymentService paymentService, EnrollmentService enrollmentService) {
		this.paymentService = paymentService;
		this.enrollmentService = enrollmentService;
	}

	@GetMapping
	public String list(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "page", defaultValue = "0") int page, Model model) throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").descending());
		Page<PaymentDTO> result = paymentService.searchPayments(keyword, pageable);

		model.addAttribute("payments", result.getContent());
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("pageSize", result.getSize());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("totalItems", result.getTotalElements());
		model.addAttribute("hasPrevious", result.hasPrevious());
		model.addAttribute("hasNext", result.hasNext());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		return "modules/payment/list";
	}

	@GetMapping("/create")
	public String createForm(@RequestParam(name = "enrollmentId", required = false) Long enrollmentId, Model model)
			throws Exception {
		loadFormOptions(model, null, enrollmentId);
		PaymentDTO paymentDTO = new PaymentDTO();
		paymentDTO.setEnrollmentId(enrollmentId);
		model.addAttribute("payment", paymentDTO);
		model.addAttribute("lockedEnrollmentId", enrollmentId);
		return "modules/payment/form";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		PaymentDTO payment = paymentService.getPaymentById(id);
		loadFormOptions(model, id, null);
		model.addAttribute("payment", payment);
		model.addAttribute("lockedEnrollmentId", null);
		return "modules/payment/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("payment") PaymentDTO dto, RedirectAttributes redirectAttributes) {
		try {
			paymentService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Payment saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/enrollments";
	}
	private void loadFormOptions(Model model, Long excludePaymentId, Long lockedEnrollmentId) throws Exception {
		List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments();
		if (lockedEnrollmentId != null) {
			enrollments = enrollments.stream()
					.filter(enrollment -> Objects.equals(enrollment.getId(), lockedEnrollmentId))
					.toList();
		}
		Map<Long, EnrollmentPaymentSummaryDTO> paymentSummaryByEnrollmentId = new HashMap<>();
		List<EnrollmentDTO> eligibleEnrollments = new ArrayList<>();
		for (EnrollmentDTO enrollment : enrollments) {
			EnrollmentPaymentSummaryDTO summary = paymentService
					.getEnrollmentPaymentSummary(enrollment.getId(), excludePaymentId);
			if (summary.getRemainingBalance() != null
					&& summary.getRemainingBalance().compareTo(BigDecimal.ZERO) > 0) {
				eligibleEnrollments.add(enrollment);
				paymentSummaryByEnrollmentId.put(enrollment.getId(), summary);
			}
		}

		model.addAttribute("enrollments", eligibleEnrollments);
		model.addAttribute("paymentSummaryByEnrollmentId", paymentSummaryByEnrollmentId);
		model.addAttribute("paymentTypes", PaymentType.values());
	}
}
