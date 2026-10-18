package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.InstructorDTO;
import com.trainingcenter.entities.Instructor;
import com.trainingcenter.enums.ActiveStatus;

@Component
public class InstructorMapper {

	public InstructorDTO toDTO(Instructor instructor) {

		if (instructor == null) {
			return null;
		}
		InstructorDTO dto = new InstructorDTO();

		dto.setId(instructor.getId());
		dto.setName(instructor.getName());
		dto.setPhoneNumber(instructor.getPhoneNumber());
		dto.setEmail(instructor.getEmail());
		dto.setAddress(instructor.getAddress());
		dto.setStatus(instructor.getStatus());
		dto.setCreatedTime(instructor.getCreatedTime());
		dto.setUpdatedTime(instructor.getUpdatedTime());
		if (instructor.getCreatedBy() != null) {
			dto.setCreatedByName(instructor.getCreatedBy().getName());
		}

		if (instructor.getUpdatedBy() != null) {
			dto.setUpdatedByName(instructor.getUpdatedBy().getName());
		}
		return dto;
	}
	
	public Instructor toEntity(InstructorDTO dto) {
		Instructor instructor=new Instructor();
		instructor.setName(dto.getName());
		instructor.setPhoneNumber(dto.getPhoneNumber());
		instructor.setEmail(dto.getEmail());
		instructor.setAddress(dto.getAddress());
		instructor.setStatus(ActiveStatus.ACTIVE);
		return instructor;
	}
	
	public void updateEntity(Instructor instructor, InstructorDTO dto) {
		instructor.setName(dto.getName());
		instructor.setPhoneNumber(dto.getPhoneNumber());
		instructor.setEmail(dto.getEmail());
		instructor.setAddress(dto.getAddress());
		instructor.setStatus(dto.getStatus());
	}
	
}
