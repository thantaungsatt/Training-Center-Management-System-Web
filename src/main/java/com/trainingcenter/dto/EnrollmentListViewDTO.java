package com.trainingcenter.dto;

import java.math.BigDecimal;

import com.trainingcenter.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentListViewDTO {

	private Long enrollmentId;
	private Long studentId;
	private String studentName;
	private String fatherName;
	private String courseName;
	private PaymentStatus paymentStatus;
	private BigDecimal balance;
}
