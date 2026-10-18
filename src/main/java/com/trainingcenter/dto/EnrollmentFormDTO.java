package com.trainingcenter.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.enums.ScheduleType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentFormDTO extends BaseDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public interface Step1 {
	}

	public interface Step2 {
	}

	public interface Step3 {
	}

	private Boolean returningStudent = Boolean.FALSE;

	private Long existingStudentId;

	@NotBlank(message = "Student name is required.", groups = Step1.class)
	private String name;

	@NotBlank(message = "Father name is required.", groups = Step1.class)
	private String fatherName;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateOfBirth;

	@NotBlank(message = "Phone number is required.", groups = Step1.class)
	private String phoneNumber;

	private String email;

	private String address;

	private StudentStatus studentStatus = StudentStatus.ACTIVE;

	@NotNull(message = "Please select a course.", groups = Step2.class)
	private Long selectedCourseId;

	@NotNull(message = "Please select weekday or weekend.", groups = Step2.class)
	private ScheduleType selectedScheduleType;

	@NotNull(message = "Please select a class time.", groups = Step2.class)
	private Long trainingClassId;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate enrollDate = LocalDate.now();

	private EnrollStatus enrollmentStatus = EnrollStatus.ACTIVE;

	@NotNull(message = "Payment amount is required.", groups = Step3.class)
	@DecimalMin(value = "0.01", message = "Payment amount must be greater than zero.", groups = Step3.class)
	private BigDecimal amount;

	@DecimalMin(value = "0.00", message = "Discount must be 0% or higher.", groups = Step3.class)
	@DecimalMax(value = "100.00", message = "Discount cannot be greater than 100%.", groups = Step3.class)
	private BigDecimal discountPercent = BigDecimal.ZERO;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate paymentDate = LocalDate.now();

	private PaymentType paymentType = PaymentType.CASH;

	private PaymentStatus paymentStatus = PaymentStatus.PAID;

	private String note;

	private Boolean installment = Boolean.FALSE;

	@Min(value = 2, message = "Installment count must be at least 2.", groups = Step3.class)
	private Integer installmentCount = 2;
}
