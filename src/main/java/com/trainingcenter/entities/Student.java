package com.trainingcenter.entities;

import java.time.LocalDateTime;

import com.trainingcenter.enums.StudentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "students")
public class Student extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "father_name", nullable = false, length = 100)
	private String fatherName;

	@Column(name = "dob")
	private LocalDateTime dateOfBirth;

	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;

//	@Column(unique = true)
	private String email;

	@Column(length = 255)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudentStatus status = StudentStatus.ACTIVE;
}
