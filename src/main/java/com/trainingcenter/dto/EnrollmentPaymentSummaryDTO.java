package com.trainingcenter.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentPaymentSummaryDTO {

	private Long enrollmentId;
	private BigDecimal totalFee;
	private BigDecimal totalPaid;
	private BigDecimal remainingBalance;
}
