package com.trainingcenter.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "payments")
public class Payment extends BaseEntity{

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "enroll_id", nullable = false)
	private Enrollment enrollment;
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "discount_percent", precision = 5, scale = 2)
	private BigDecimal discountPercent;
	
	@Column(name = "payment_date", nullable = false)
	private LocalDate paymentDate = LocalDate.now();
	
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_type", nullable = false)
	private PaymentType paymentType=PaymentType.CASH;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status = PaymentStatus.PAID;
	
	@Column(length = 500)
	private String note;
}
