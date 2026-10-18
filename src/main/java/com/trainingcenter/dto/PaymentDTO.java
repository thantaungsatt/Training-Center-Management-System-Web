package com.trainingcenter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO extends BaseDTO {

	private Long enrollmentId;
	private String studentName;
	private String className;

	private BigDecimal amount;
	private BigDecimal discountPercent;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate paymentDate;

	private PaymentType paymentType;
	private PaymentStatus status;

	private String note;
}
