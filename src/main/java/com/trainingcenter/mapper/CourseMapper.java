package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;
import com.trainingcenter.dto.CourseDTO;
import com.trainingcenter.entities.Course;

@Component
public class CourseMapper {
    
	public CourseDTO toDTO(Course course) {
		if(course==null)return null;
		
		CourseDTO dto=new CourseDTO();
		
		dto.setId(course.getId());
		dto.setCourseName(course.getCourseName());
		dto.setPrefix(course.getPrefix());
		dto.setDuration(course.getDuration());
		dto.setFee(course.getFee());
		dto.setDescription(course.getDescription());
		dto.setCreatedTime(course.getCreatedTime());
		dto.setUpdatedTime(course.getUpdatedTime());
		if(course.getCreatedBy()!=null) {
			dto.setCreatedByName(course.getCreatedBy().getName());
		}
		
		if(course.getUpdatedBy()!=null) {
			dto.setUpdatedByName(course.getUpdatedBy().getName());
		}
		return dto;
	}
	
	public Course toEntity(CourseDTO dto) {
		Course course=new Course();
		course.setCourseName(dto.getCourseName());
		course.setPrefix(dto.getPrefix());
		course.setDuration(dto.getDuration());
		course.setFee(dto.getFee());
		course.setDescription(dto.getDescription());
		return course;
	}

	public void updateEntity(Course course, CourseDTO dto) {
		course.setCourseName(dto.getCourseName());
		course.setPrefix(dto.getPrefix());
		course.setDuration(dto.getDuration());
		course.setFee(dto.getFee());
		course.setDescription(dto.getDescription());
	}
}
