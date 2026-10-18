package com.trainingcenter.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.trainingcenter.enums.StudentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO extends BaseDTO {

	private String name;

	private String fatherName;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateOfBirth;

	private String phoneNumber;

	private String email;

	private String address;

	private StudentStatus status;
}
