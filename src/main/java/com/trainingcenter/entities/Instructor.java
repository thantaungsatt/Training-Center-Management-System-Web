package com.trainingcenter.entities;

import com.trainingcenter.enums.ActiveStatus;

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
@Table(name = "instructors")
public class Instructor extends BaseEntity {
	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;

	@Column(unique = true)
	private String email;

	@Column(length = 255)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ActiveStatus status = ActiveStatus.ACTIVE;
}
