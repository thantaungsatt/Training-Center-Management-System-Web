package com.trainingcenter.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.entities.Student;
import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.enums.EnrollStatus;

@Component
public class EnrollmentMapper {
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h a");

	public EnrollmentDTO toDTO(Enrollment enrollment) {
		if (enrollment == null) {
			return null;
		}

		EnrollmentDTO dto = new EnrollmentDTO();
		dto.setId(enrollment.getId());
		dto.setEnrollDate(enrollment.getEnrollDate());
		dto.setStatus(enrollment.getStatus());
		dto.setCreatedTime(enrollment.getCreatedTime());
		dto.setUpdatedTime(enrollment.getUpdatedTime());

		if (enrollment.getStudent() != null) {
			dto.setStudentId(enrollment.getStudent().getId());
			dto.setStudentName(enrollment.getStudent().getName());
			dto.setFatherName(enrollment.getStudent().getFatherName());
			dto.setPhoneNumber(enrollment.getStudent().getPhoneNumber());
			dto.setStudentStatus(enrollment.getStudent().getStatus());
		}

		if (enrollment.getTrainingClass() != null) {
			dto.setTrainingClassId(enrollment.getTrainingClass().getId());
			dto.setClassName(buildClassName(enrollment.getTrainingClass()));
			dto.setStartTime(enrollment.getTrainingClass().getStartTime());
			dto.setEndTime(enrollment.getTrainingClass().getEndTime());
			if (enrollment.getTrainingClass().getCourse() != null) {
				dto.setCourseId(enrollment.getTrainingClass().getCourse().getId());
				dto.setCourseName(enrollment.getTrainingClass().getCourse().getCourseName());
			}
		}

		if (enrollment.getCreatedBy() != null) {
			dto.setCreatedByName(enrollment.getCreatedBy().getName());
		}
		if (enrollment.getUpdatedBy() != null) {
			dto.setUpdatedByName(enrollment.getUpdatedBy().getName());
		}

		return dto;
	}

	public Enrollment toEntity(EnrollmentDTO dto, Student student, TrainingClass trainingClass) {
		Enrollment enrollment = new Enrollment();
		enrollment.setStudent(student);
		enrollment.setTrainingClass(trainingClass);
		enrollment.setEnrollDate(dto.getEnrollDate() != null ? dto.getEnrollDate() : enrollment.getEnrollDate());
		enrollment.setStatus(dto.getStatus() != null ? dto.getStatus() : EnrollStatus.ACTIVE);
		return enrollment;
	}

	public void updateEntity(Enrollment enrollment, EnrollmentDTO dto, Student student, TrainingClass trainingClass) {
		enrollment.setStudent(student);
		enrollment.setTrainingClass(trainingClass);
		enrollment.setEnrollDate(dto.getEnrollDate());
		enrollment.setStatus(dto.getStatus());
	}

	private String buildClassName(TrainingClass trainingClass) {
		String courseName = trainingClass.getCourse() != null ? trainingClass.getCourse().getCourseName() : "Class";
		String start = trainingClass.getStartTime() != null ? trainingClass.getStartTime().format(TIME_FORMATTER) : "--:--";
		String end = trainingClass.getEndTime() != null ? trainingClass.getEndTime().format(TIME_FORMATTER) : "--:--";
		return courseName + " (" + start + " - " + end + ")";
	}
}
