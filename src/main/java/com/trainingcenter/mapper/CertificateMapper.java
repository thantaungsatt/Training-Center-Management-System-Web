package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.CertificateDTO;
import com.trainingcenter.entities.Certificate;
import com.trainingcenter.entities.Course;
import com.trainingcenter.entities.Student;

@Component
public class CertificateMapper {

	public CertificateDTO toDTO(Certificate entity) {
		if (entity == null) {
			return null;
		}

		CertificateDTO dto = new CertificateDTO();
		dto.setId(entity.getId());
		dto.setCertificateNo(entity.getCertificateNo());
		dto.setEnrollDate(entity.getEnrollDate());
		dto.setEndDate(entity.getEndDate());
		dto.setIssueDate(entity.getIssueDate());
		dto.setCreatedTime(entity.getCreatedTime());
		dto.setUpdatedTime(entity.getUpdatedTime());

		if (entity.getStudent() != null) {
			dto.setStudentId(entity.getStudent().getId());
			dto.setStudentName(entity.getStudent().getName());
			dto.setFatherName(entity.getStudent().getFatherName());
			dto.setDateOfBirth(entity.getStudent().getDateOfBirth() != null ? entity.getStudent().getDateOfBirth().toLocalDate() : null);
		}

		if (entity.getCourse() != null) {
			dto.setCourseId(entity.getCourse().getId());
			dto.setCourseName(entity.getCourse().getCourseName());
			dto.setCourseDescription(entity.getCourse().getDescription());
		}

		if (entity.getCreatedBy() != null) {
			dto.setCreatedByName(entity.getCreatedBy().getName());
		}
		if (entity.getUpdatedBy() != null) {
			dto.setUpdatedByName(entity.getUpdatedBy().getName());
		}

		return dto;
	}

	public Certificate toEntity(CertificateDTO dto, Student student, Course course) {
		Certificate entity = new Certificate();
		entity.setCertificateNo(dto.getCertificateNo());
		entity.setStudent(student);
		entity.setCourse(course);
		entity.setEnrollDate(dto.getEnrollDate());
		entity.setEndDate(dto.getEndDate());
		entity.setIssueDate(dto.getIssueDate());
		return entity;
	}

	public void updateEntity(Certificate entity, CertificateDTO dto, Student student, Course course) {
		entity.setCertificateNo(dto.getCertificateNo());
		entity.setStudent(student);
		entity.setCourse(course);
		entity.setEnrollDate(dto.getEnrollDate());
		entity.setEndDate(dto.getEndDate());
		entity.setIssueDate(dto.getIssueDate());
	}
}

