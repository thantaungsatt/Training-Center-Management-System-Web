package com.trainingcenter.services.course;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.CourseDTO;
import com.trainingcenter.entities.Course;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.CourseMapper;
import com.trainingcenter.repositories.CourseRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class CourseServiceImpl implements CourseService {

	private final CourseRepository courseRepository;
	private final CourseMapper courseMapper;
	private final UserRepository userRepository;

	public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper, UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.courseMapper = courseMapper;
		this.userRepository = userRepository;
	}

	@Override
	public CourseDTO save(CourseDTO dto) throws Exception {
		String normalizedPrefix = normalizePrefix(dto.getPrefix());
		dto.setPrefix(normalizedPrefix);

		Course course;
		User currentUser = getCurrentUser();

		if (dto.getId() != null) {
			course = courseRepository.findById(dto.getId()).orElseThrow(() -> new Exception("Course not found"));
			courseMapper.updateEntity(course, dto);
			if (currentUser != null) {
				course.setUpdatedBy(currentUser);
			}
		} else {
			if (courseRepository.existsByCourseName(dto.getCourseName())) {
				throw new Exception("Course alrerady exists: " + dto.getCourseName());
			}
			course = courseMapper.toEntity(dto);
			if (currentUser != null) {
				course.setCreatedBy(currentUser);
				course.setUpdatedBy(currentUser);
			}
		}
		courseRepository.save(course);

		return courseMapper.toDTO(course);
	}

	@Override
	public void delete(Long id) throws Exception {
		Course course = courseRepository.findById(id)
				.orElseThrow(() -> new Exception("Course not found with id: " + id));
		courseRepository.delete(course);
	}

	@Override
	public CourseDTO getCourseById(Long id) throws Exception {
		Course course = courseRepository.findById(id)
				.orElseThrow(() -> new Exception("Course not found with id: " + id));
		return courseMapper.toDTO(course);
	}

	@Override
	public List<CourseDTO> getAllCourses() throws Exception {
		List<Course> courses = courseRepository.findAll();

		return courses.stream().map(courseMapper::toDTO).toList();
	}

	@Override
	public Page<CourseDTO> searchCourseByCourseName(String courseName, Pageable pageable) throws Exception {
		if (Objects.isNull(courseName) || courseName.isBlank()) {
			return courseRepository.findAll(pageable).map(courseMapper::toDTO);
		}
		return courseRepository.findByCourseNameContainingIgnoreCase(courseName.trim(), pageable).map(courseMapper::toDTO);
	}

	private User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		String username = authentication.getName();
		if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
			return null;
		}
		return userRepository.findByUsername(username).orElse(null);
	}

	private String normalizePrefix(String prefix) throws Exception {
		if (prefix == null || prefix.isBlank()) {
			throw new Exception("Course prefix is required.");
		}
		String normalized = prefix.trim().toUpperCase();
		if (!normalized.matches("[A-Z0-9]+")) {
			throw new Exception("Course prefix must contain only letters and numbers.");
		}
		return normalized;
	}

}
