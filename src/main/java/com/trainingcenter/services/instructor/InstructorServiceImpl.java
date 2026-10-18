package com.trainingcenter.services.instructor;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.InstructorDTO;
import com.trainingcenter.entities.Instructor;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.InstructorMapper;
import com.trainingcenter.repositories.InstructorRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class InstructorServiceImpl implements InstructorService {

	private final InstructorRepository instructorRepository;
	private final InstructorMapper instructorMapper;
	private final UserRepository userRepository;

	public InstructorServiceImpl(InstructorRepository instructorRepository, InstructorMapper instructorMapper,
			UserRepository userRepository) {
		this.instructorRepository = instructorRepository;
		this.instructorMapper = instructorMapper;
		this.userRepository = userRepository;
	}

	@Override
	public InstructorDTO save(InstructorDTO dto) throws Exception {
		Instructor instructor;
		User currentUser=getCurrentUser();
		if (dto.getId() != null) {
			instructor = instructorRepository.findById(dto.getId()).orElseThrow(() -> new Exception("Instructor not found"));
			instructorMapper.updateEntity(instructor, dto);
			if (currentUser != null) {
				instructor.setUpdatedBy(currentUser);
			}
		} else {
			if (instructorRepository.existsByName(dto.getName())) {
				throw new Exception("Instructor alrerady exists: " + dto.getName());
			}
			instructor = instructorMapper.toEntity(dto);
			if (currentUser != null) {
				instructor.setCreatedBy(currentUser);
				instructor.setUpdatedBy(currentUser);
			}
		}
		instructorRepository.save(instructor);

		return instructorMapper.toDTO(instructor);
	}

	@Override
	public void delete(Long id) throws Exception {
		Instructor instructor = instructorRepository.findById(id)
				.orElseThrow(() -> new Exception("Instructor not found with id: " + id));
		instructorRepository.delete(instructor);

	}

	@Override
	public InstructorDTO getInstructorById(Long id) throws Exception {
		Instructor instructor = instructorRepository.findById(id)
				.orElseThrow(() -> new Exception("Instructor not found with id: " + id));
		return instructorMapper.toDTO(instructor);
	}

	@Override
	public List<InstructorDTO> getAllInstructors() throws Exception {
		List<Instructor> instructor = instructorRepository.findAll();

		return instructor.stream().map(instructorMapper::toDTO).toList();
	}

	@Override
	public Page<InstructorDTO> searchInstructorsByName(String name, Pageable pageable) throws Exception {
		if (Objects.isNull(name) || name.isBlank()) {
			return instructorRepository.findAll(pageable).map(instructorMapper::toDTO);
		}
		return instructorRepository.findByNameContainingIgnoreCase(name.trim(), pageable).map(instructorMapper::toDTO);
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
