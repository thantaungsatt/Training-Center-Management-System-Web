package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.entities.Course;
import com.trainingcenter.entities.Instructor;
import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.enums.ScheduleType;

@Component
public class TrainingClassMapper {

	public TrainingClassDTO toDTO(TrainingClass trainingClass) {
		if (trainingClass == null) return null;
		
		TrainingClassDTO dto=new TrainingClassDTO();
		
		dto.setId(trainingClass.getId());
		dto.setStartTime(trainingClass.getStartTime());
		dto.setEndTime(trainingClass.getEndTime());
		dto.setScheduleType(trainingClass.getScheduleType());
		
		if (trainingClass.getCourse() != null) {
        	dto.setCourseId(trainingClass.getCourse().getId());
            dto.setCourseName(trainingClass.getCourse().getCourseName());
        }
		
		if (trainingClass.getInstructor() != null) {
        	dto.setInstructorId(trainingClass.getInstructor().getId());
            dto.setName(trainingClass.getInstructor().getName());
        }
		
		dto.setRoom(trainingClass.getRoom());
		dto.setMaxStudents(trainingClass.getMaxStudents());
		dto.setCreatedTime(trainingClass.getCreatedTime());
		dto.setUpdatedTime(trainingClass.getUpdatedTime());
		
		if (trainingClass.getCreatedBy() != null) {
			dto.setCreatedByName(trainingClass.getCreatedBy().getName());
		}

		if (trainingClass.getUpdatedBy() != null) {
			dto.setUpdatedByName(trainingClass.getUpdatedBy().getName());
		}
		return dto;
	}
	
	public TrainingClass toEntity(TrainingClassDTO dto, Course course, Instructor instructor) {
		TrainingClass trainingClass=new TrainingClass();
		trainingClass.setStartTime(dto.getStartTime());
		trainingClass.setEndTime(dto.getEndTime());
		trainingClass.setScheduleType(dto.getScheduleType());
		trainingClass.setCourse(course);
		trainingClass.setInstructor(instructor);
		trainingClass.setRoom(dto.getRoom());
		trainingClass.setMaxStudents(dto.getMaxStudents());
		return trainingClass;
	}
	
	public void updateEntity(TrainingClass trainingClass, TrainingClassDTO dto, Course course, Instructor instructor){
		trainingClass.setStartTime(dto.getStartTime());
		trainingClass.setEndTime(dto.getEndTime());
		trainingClass.setScheduleType(dto.getScheduleType());
		trainingClass.setCourse(course);
		trainingClass.setInstructor(instructor);
		trainingClass.setRoom(dto.getRoom());
		trainingClass.setMaxStudents(dto.getMaxStudents());
	}
}
