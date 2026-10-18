package com.trainingcenter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trainingcenter.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

	boolean existsByCourseName(String courseName);

	boolean existsByPrefixIgnoreCase(String prefix);

	boolean existsByPrefixIgnoreCaseAndIdNot(String prefix, Long id);
	
	Page<Course> findByCourseNameContainingIgnoreCase(String courseName, Pageable pageable);
}
