package com.trainingcenter.services.trainingClass;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.entities.Course;
import com.trainingcenter.entities.Instructor;
import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.entities.User;
import com.trainingcenter.enums.ScheduleType;
import com.trainingcenter.mapper.TrainingClassMapper;
import com.trainingcenter.repositories.CourseRepository;
import com.trainingcenter.repositories.InstructorRepository;
import com.trainingcenter.repositories.TrainingClassRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class TrainingClassServiceImpl implements TrainingClassService {

	private final TrainingClassRepository trainingClassRepository;
	private final TrainingClassMapper trainingClassMapper;
	private final InstructorRepository instructorRepository;
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	public TrainingClassServiceImpl(TrainingClassRepository trainingClassRepository,
			TrainingClassMapper trainingClassMapper, InstructorRepository instructorRepository,
			CourseRepository courseRepository, UserRepository userRepository) {
		this.trainingClassRepository = trainingClassRepository;
		this.trainingClassMapper = trainingClassMapper;
		this.instructorRepository = instructorRepository;
		this.courseRepository = courseRepository;
		this.userRepository = userRepository;
	}

	@Override
	public TrainingClassDTO save(TrainingClassDTO dto) throws Exception {
		TrainingClass trainingClass;
		User currentUser = getCurrentUser();

		Course course = courseRepository.findById(dto.getCourseId())
				.orElseThrow(() -> new Exception("Course not found with id:" + dto.getCourseId()));

		Instructor instructor = instructorRepository.findById(dto.getInstructorId())
				.orElseThrow(() -> new Exception("Instructor not found with id:" + dto.getInstructorId()));
		if (dto.getId() != null) {
			if (trainingClassRepository.existsByStartTimeAndEndTimeAndCourseIdAndIdNot(dto.getStartTime(), dto.getEndTime(),
					dto.getCourseId(), dto.getId())) {
				throw new Exception("Another class already exists for this course at the same time.");
			}
			trainingClass = trainingClassRepository.findById(dto.getId())
					.orElseThrow(() -> new Exception("Class not found"));
			trainingClassMapper.updateEntity(trainingClass, dto, course, instructor);
			if (currentUser != null) {
				trainingClass.setUpdatedBy(currentUser);
			}
			
		} else {
			if (trainingClassRepository.existsByStartTimeAndEndTimeAndCourseId(dto.getStartTime(), dto.getEndTime(),
					dto.getCourseId())) {
				throw new Exception("Class alrerady exists");
			}
			trainingClass = trainingClassMapper.toEntity(dto, course, instructor);
			if (currentUser != null) {
				trainingClass.setCreatedBy(currentUser);
				trainingClass.setUpdatedBy(currentUser);
			}
		}

		trainingClassRepository.save(trainingClass);

		return trainingClassMapper.toDTO(trainingClass);
	}

	@Override
	public void delete(Long id) throws Exception {
		TrainingClass trainingClass = trainingClassRepository.findById(id)
				.orElseThrow(() -> new Exception("Class not found with id:" + id));
		trainingClassRepository.delete(trainingClass);
	}

	@Override
	public TrainingClassDTO getTrainingClassById(Long id) throws Exception {
		TrainingClass trainingClass = trainingClassRepository.findById(id)
				.orElseThrow(() -> new Exception("Class not found with id:" + id));

		return trainingClassMapper.toDTO(trainingClass);
	}

	@Override
	public List<TrainingClassDTO> getAllTrainingClasses() throws Exception {
		List<TrainingClass> trainingClasses = trainingClassRepository.findAll();

		return trainingClasses.stream().map(trainingClassMapper::toDTO).toList();
	}

	@Override
	public Page<TrainingClassDTO> search(String keyword, LocalTime start, LocalTime end, ScheduleType schedule,
			Pageable pageable) {
		if (keyword != null && keyword.isBlank()) {
			keyword = null;
		}
		if (keyword != null) {
			keyword = keyword.trim();
		}

		Page<TrainingClass> result = trainingClassRepository.search(start, end, keyword, schedule, pageable);

		return result.map(trainingClassMapper::toDTO);
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

}
