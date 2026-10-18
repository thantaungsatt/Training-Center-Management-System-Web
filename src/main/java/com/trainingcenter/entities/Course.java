package com.trainingcenter.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "courses")
public class Course extends BaseEntity {

	@Column(name = "course_name", nullable = false, unique = true, length = 100)
	private String courseName;
	
	private String prefix;

	@Column(nullable = false)
	private String duration;

	@Column(precision = 10, scale = 2)
	private BigDecimal fee;

	@Column(length = 500)
	private String description;
}
