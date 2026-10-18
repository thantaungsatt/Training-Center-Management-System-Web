package com.trainingcenter.repositories.projection;

/** Aggregate projection used to construct the course-enrollment doughnut chart. */
public interface CourseEnrollmentProjection {

	String getCourseName();

	Long getEnrollmentCount();
}
