package com.trainingcenter.services.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.EnrollmentPaymentSummaryDTO;
import com.trainingcenter.dto.PaymentDTO;

public interface PaymentService {

	PaymentDTO save(PaymentDTO dto) throws Exception;


	PaymentDTO getPaymentById(Long id) throws Exception;

	List<PaymentDTO> getByStudentId(Long studentId) throws Exception;

	Page<PaymentDTO> searchPayments(String keyword, Pageable pageable) throws Exception;

	EnrollmentPaymentSummaryDTO getEnrollmentPaymentSummary(Long enrollmentId, Long excludePaymentId) throws Exception;
}
