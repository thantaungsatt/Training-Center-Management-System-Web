package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.PaymentDTO;
import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.entities.Payment;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;

@Component
public class PaymentMapper {

	public PaymentDTO toDTO(Payment payment) {
		if (payment == null) {
			return null;
		}

		PaymentDTO dto = new PaymentDTO();
		dto.setId(payment.getId());
		dto.setAmount(payment.getAmount());
		dto.setDiscountPercent(payment.getDiscountPercent());
		dto.setPaymentDate(payment.getPaymentDate());
		dto.setPaymentType(payment.getPaymentType());
		dto.setStatus(payment.getStatus());
		dto.setNote(payment.getNote());
		dto.setCreatedTime(payment.getCreatedTime());
		dto.setUpdatedTime(payment.getUpdatedTime());

		if (payment.getEnrollment() != null) {
			dto.setEnrollmentId(payment.getEnrollment().getId());
			if (payment.getEnrollment().getStudent() != null) {
				dto.setStudentName(payment.getEnrollment().getStudent().getName());
			}
			if (payment.getEnrollment().getTrainingClass() != null) {
				dto.setClassName(payment.getEnrollment().getTrainingClass().getCourse().getCourseName());
			}
		}

		if (payment.getCreatedBy() != null) {
			dto.setCreatedByName(payment.getCreatedBy().getName());
		}
		if (payment.getUpdatedBy() != null) {
			dto.setUpdatedByName(payment.getUpdatedBy().getName());
		}

		return dto;
	}

	public Payment toEntity(PaymentDTO dto, Enrollment enrollment) {
		Payment payment = new Payment();
		payment.setEnrollment(enrollment);
		payment.setAmount(dto.getAmount());
		payment.setDiscountPercent(dto.getDiscountPercent());
		payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : payment.getPaymentDate());
		payment.setPaymentType(dto.getPaymentType() != null ? dto.getPaymentType() : PaymentType.CASH);
		payment.setStatus(dto.getStatus() != null ? dto.getStatus() : PaymentStatus.PAID);
		payment.setNote(dto.getNote());
		return payment;
	}

	public void updateEntity(Payment payment, PaymentDTO dto, Enrollment enrollment) {
		payment.setEnrollment(enrollment);
		payment.setAmount(dto.getAmount());
		payment.setDiscountPercent(dto.getDiscountPercent());
		payment.setPaymentDate(dto.getPaymentDate());
		payment.setPaymentType(dto.getPaymentType());
		payment.setStatus(dto.getStatus());
		payment.setNote(dto.getNote());
	}
}
