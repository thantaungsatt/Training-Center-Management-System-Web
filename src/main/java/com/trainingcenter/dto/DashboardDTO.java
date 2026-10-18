package com.trainingcenter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.trainingcenter.enums.EnrollStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Read model for the dashboard. Keeping presentation-specific values together
 * prevents the controller and Thymeleaf template from knowing repository details.
 */
@Getter
@AllArgsConstructor
public class DashboardDTO {

	private final long totalStudents;
	private final long activeStudents;
	private final long activeCourses;
	private final BigDecimal monthlyRevenue;
	private final long totalInstructors;
	private final ChartData revenueChart;
	private final ChartData enrollmentChart;
	private final List<RecentEnrollment> recentEnrollments;

	/** Generic label/value structure consumed by both Chart.js visualizations. */
	@Getter
	@AllArgsConstructor
	public static class ChartData {
		private final List<String> labels;
		private final List<BigDecimal> values;
	}

	/** Compact row model used by the recent-enrollment table. */
	@Getter
	@AllArgsConstructor
	public static class RecentEnrollment {
		private final String studentName;
		private final String courseName;
		private final LocalDate enrollmentDate;
		private final EnrollStatus status;
	}
}
