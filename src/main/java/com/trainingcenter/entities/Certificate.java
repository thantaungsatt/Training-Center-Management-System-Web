package com.trainingcenter.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "certificates")
public class Certificate extends BaseEntity {
	
	@Column(name = "certificate_no")
	private String certificateNo;

	@ManyToOne
	private Student student;
	
	@ManyToOne
	private Course course;
	
	@Column(name = "issue_date")
	private LocalDate issueDate;
	
	@Column(name = "enroll_date")
	private LocalDate enrollDate;
	
	@Column(name = "end_date")
	private LocalDate endDate;
}
