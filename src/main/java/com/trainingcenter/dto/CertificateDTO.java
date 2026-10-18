package com.trainingcenter.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateDTO extends BaseDTO {

	private String certificateNo;

	private Long studentId;
	private String studentName;
	private String fatherName;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateOfBirth;

	private Long courseId;
	private String courseName;
	private String courseDescription;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate enrollDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate issueDate;
}

