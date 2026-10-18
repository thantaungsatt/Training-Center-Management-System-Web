package com.trainingcenter.services.student;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.entities.Student;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.StudentMapper;
import com.trainingcenter.repositories.EnrollmentRepository;
import com.trainingcenter.repositories.StudentRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class StudentServiceImpl implements StudentService {

	private final StudentRepository studentRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final StudentMapper studentMapper;
	private final UserRepository userRepository;

	public StudentServiceImpl(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
			StudentMapper studentMapper, UserRepository userRepository) {
		this.studentRepository = studentRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.studentMapper = studentMapper;
		this.userRepository = userRepository;
	}

	@Override
	public StudentDTO save(StudentDTO dto) throws Exception {
		Student student;
		User currentUser = getCurrentUser();

		validateEmail(dto);

		if (dto.getId() != null) {
			student = studentRepository.findById(dto.getId()).orElseThrow(() -> new Exception("Student not found"));
			studentMapper.updateEntity(student, dto);
			if (currentUser != null) {
				student.setUpdatedBy(currentUser);
			}
		} else {
			student = studentMapper.toEntity(dto);
			if (currentUser != null) {
				student.setCreatedBy(currentUser);
				student.setUpdatedBy(currentUser);
			}
		}

		studentRepository.save(student);
		return studentMapper.toDTO(student);
	}

	@Override
	public void delete(Long id) throws Exception {
		if (enrollmentRepository.existsByStudentId(id)) {
			throw new Exception("Cannot delete student. This student is already linked to enrollment records.");
		}
		Student student = studentRepository.findById(id).orElseThrow(() -> new Exception("Student not found with id: " + id));
		studentRepository.delete(student);
	}

	@Override
	public StudentDTO getStudentById(Long id) throws Exception {
		Student student = studentRepository.findById(id).orElseThrow(() -> new Exception("Student not found with id: " + id));
		return studentMapper.toDTO(student);
	}

	@Override
	public List<StudentDTO> getAllStudents() throws Exception {
		return studentRepository.findAll().stream().map(studentMapper::toDTO).toList();
	}

	@Override
	public Page<StudentDTO> searchStudents(String keyword, Long trainingClassId, Pageable pageable) throws Exception {
		String text = (Objects.isNull(keyword) || keyword.isBlank()) ? null : keyword.trim();
		return studentRepository.searchByKeywordAndClass(text, trainingClassId, pageable).map(studentMapper::toDTO);
	}

	private void validateEmail(StudentDTO dto) throws Exception {
		if (dto.getEmail() == null || dto.getEmail().isBlank()) {
			return;
		}
		String email = dto.getEmail().trim();
		dto.setEmail(email);
		boolean exists = dto.getId() == null ? studentRepository.existsByEmail(email)
				: studentRepository.existsByEmailAndIdNot(email, dto.getId());
		if (exists) {
			throw new Exception("Email already exists: " + email);
		}
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
