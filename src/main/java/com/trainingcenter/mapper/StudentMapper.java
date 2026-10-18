package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.entities.Student;
import com.trainingcenter.enums.StudentStatus;

@Component
public class StudentMapper {

	public StudentDTO toDTO(Student student) {
		if (student == null) {
			return null;
		}

		StudentDTO dto = new StudentDTO();
		dto.setId(student.getId());
		dto.setName(student.getName());
		dto.setFatherName(student.getFatherName());
		dto.setDateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().toLocalDate() : null);
		dto.setPhoneNumber(student.getPhoneNumber());
		dto.setEmail(student.getEmail());
		dto.setAddress(student.getAddress());
		dto.setStatus(student.getStatus());
		dto.setCreatedTime(student.getCreatedTime());
		dto.setUpdatedTime(student.getUpdatedTime());

		if (student.getCreatedBy() != null) {
			dto.setCreatedByName(student.getCreatedBy().getName());
		}
		if (student.getUpdatedBy() != null) {
			dto.setUpdatedByName(student.getUpdatedBy().getName());
		}

		return dto;
	}

	public Student toEntity(StudentDTO dto) {
		Student student = new Student();
		student.setName(dto.getName());
		student.setFatherName(dto.getFatherName());
		student.setDateOfBirth(dto.getDateOfBirth() != null ? dto.getDateOfBirth().atStartOfDay() : null);
		student.setPhoneNumber(dto.getPhoneNumber());
		student.setEmail(dto.getEmail());
		student.setAddress(dto.getAddress());
		student.setStatus(StudentStatus.ACTIVE);
		return student;
	}

	public void updateEntity(Student student, StudentDTO dto) {
		student.setName(dto.getName());
		student.setFatherName(dto.getFatherName());
		student.setDateOfBirth(dto.getDateOfBirth() != null ? dto.getDateOfBirth().atStartOfDay() : null);
		student.setPhoneNumber(dto.getPhoneNumber());
		student.setEmail(dto.getEmail());
		student.setAddress(dto.getAddress());
		student.setStatus(dto.getStatus());
	}
}
