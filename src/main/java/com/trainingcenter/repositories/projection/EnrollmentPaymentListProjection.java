package com.trainingcenter.repositories.projection;

import java.math.BigDecimal;

public interface EnrollmentPaymentListProjection {

	Long getEnrollmentId();

	Long getStudentId();

	String getStudentName();

	String getFatherName();

	String getCourseName();

	String getPaymentStatus();

	BigDecimal getBalance();
}
