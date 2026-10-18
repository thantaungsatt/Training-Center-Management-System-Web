package com.trainingcenter.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.StudentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO extends BaseDTO {

	private Long studentId;
	private String studentName;
	private String fatherName;
	private String phoneNumber;
	private StudentStatus studentStatus;

	private Long trainingClassId;
	private Long courseId;
	private String className;
	private String courseName;
	private LocalTime startTime;
	private LocalTime endTime;
	private boolean certificated;
	private String paymentStatus;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate enrollDate;

	private EnrollStatus status;
}
