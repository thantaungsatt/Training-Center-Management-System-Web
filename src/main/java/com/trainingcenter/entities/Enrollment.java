package com.trainingcenter.entities;

import java.time.LocalDate;
import com.trainingcenter.enums.EnrollStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "enrollments", uniqueConstraints = { @UniqueConstraint(columnNames = { "student_id", "class_id" }) })
public class Enrollment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	private TrainingClass trainingClass;

	@Column(name = "enroll_date", nullable = false)
	private LocalDate enrollDate = LocalDate.now();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EnrollStatus status = EnrollStatus.ACTIVE;
}
