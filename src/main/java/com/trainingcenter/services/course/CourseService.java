package com.trainingcenter.services.course;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.CourseDTO;

public interface CourseService {
	CourseDTO save(CourseDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	CourseDTO getCourseById(Long id) throws Exception;

	List<CourseDTO> getAllCourses() throws Exception;

	Page<CourseDTO> searchCourseByCourseName(String courseName, Pageable pageable) throws Exception;
}
