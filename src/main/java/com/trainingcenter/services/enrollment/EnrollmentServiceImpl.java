package com.trainingcenter.services.enrollment;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentListViewDTO;
import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.entities.Student;
import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.entities.User;
import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.mapper.EnrollmentMapper;
import com.trainingcenter.repositories.CertificateRepository;
import com.trainingcenter.repositories.EnrollmentRepository;
import com.trainingcenter.repositories.PaymentRepository;
import com.trainingcenter.repositories.StudentRepository;
import com.trainingcenter.repositories.TrainingClassRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final StudentRepository studentRepository;
	private final TrainingClassRepository trainingClassRepository;
	private final PaymentRepository paymentRepository;
	private final CertificateRepository certificateRepository;
	private final EnrollmentMapper enrollmentMapper;
	private final UserRepository userRepository;

	public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
			TrainingClassRepository trainingClassRepository, PaymentRepository paymentRepository,
			CertificateRepository certificateRepository,
			EnrollmentMapper enrollmentMapper, UserRepository userRepository) {
		this.enrollmentRepository = enrollmentRepository;
		this.studentRepository = studentRepository;
		this.trainingClassRepository = trainingClassRepository;
		this.paymentRepository = paymentRepository;
		this.certificateRepository = certificateRepository;
		this.enrollmentMapper = enrollmentMapper;
		this.userRepository = userRepository;
	}

	@Override
	public EnrollmentDTO save(EnrollmentDTO dto) throws Exception {
		Student student = studentRepository.findById(dto.getStudentId())
				.orElseThrow(() -> new Exception("Student not found with id: " + dto.getStudentId()));
		if (dto.getId() == null && StudentStatus.SUSPENDED.equals(student.getStatus())) {
			throw new Exception("Suspended students cannot enroll in new courses.");
		}
		TrainingClass trainingClass = trainingClassRepository.findById(dto.getTrainingClassId())
				.orElseThrow(() -> new Exception("Class not found with id: " + dto.getTrainingClassId()));

		validateDuplicate(dto, trainingClass);
		validateClassCapacity(dto, trainingClass);

		User currentUser = getCurrentUser();
		Enrollment enrollment;
		if (dto.getId() != null) {
			enrollment = enrollmentRepository.findById(dto.getId())
					.orElseThrow(() -> new Exception("Enrollment not found with id: " + dto.getId()));
			enrollmentMapper.updateEntity(enrollment, dto, student, trainingClass);
			if (currentUser != null) {
				enrollment.setUpdatedBy(currentUser);
			}
		} else {
			enrollment = enrollmentMapper.toEntity(dto, student, trainingClass);
			if (currentUser != null) {
				enrollment.setCreatedBy(currentUser);
				enrollment.setUpdatedBy(currentUser);
			}
		}

		enrollmentRepository.save(enrollment);
		return enrollmentMapper.toDTO(enrollment);
	}

	@Override
	public void delete(Long id) throws Exception {
		if (paymentRepository.existsByEnrollmentId(id)) {
			throw new Exception("Cannot delete enrollment. Payment records exist for this enrollment.");
		}
		Enrollment enrollment = enrollmentRepository.findById(id)
				.orElseThrow(() -> new Exception("Enrollment not found with id: " + id));
		enrollmentRepository.delete(enrollment);
	}

	@Override
	public EnrollmentDTO getEnrollmentById(Long id) throws Exception {
		Enrollment enrollment = enrollmentRepository.findById(id)
				.orElseThrow(() -> new Exception("Enrollment not found with id: " + id));
		return enrollmentMapper.toDTO(enrollment);
	}

	@Override
	public List<EnrollmentDTO> getAllEnrollments() throws Exception {
		return enrollmentRepository.findAll().stream().map(enrollmentMapper::toDTO).toList();
	}

	@Override
	public List<EnrollmentDTO> getByStudentId(Long studentId) throws Exception {
		return enrollmentRepository.findByStudentIdOrderByEnrollDateDescIdDesc(studentId).stream().map(enrollmentMapper::toDTO)
				.toList();
	}

	@Override
	public Page<EnrollmentListViewDTO> getEnrollmentListView(String keyword, PaymentStatus paymentStatus, Pageable pageable)
			throws Exception {
		String text = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		String status = paymentStatus != null ? paymentStatus.name() : null;
		return enrollmentRepository.findEnrollmentPaymentList(text, status, pageable).map(item -> {
			PaymentStatus resolvedStatus;
			try {
				resolvedStatus = PaymentStatus.valueOf(item.getPaymentStatus());
			} catch (Exception e) {
				resolvedStatus = PaymentStatus.PARTIAL;
			}
			return new EnrollmentListViewDTO(item.getEnrollmentId(), item.getStudentId(), item.getStudentName(),
					item.getFatherName(), item.getCourseName(), resolvedStatus, item.getBalance());
		});
	}

	@Override
	public Page<EnrollmentDTO> searchEnrollments(String keyword, Pageable pageable) throws Exception {
		if (Objects.isNull(keyword) || keyword.isBlank()) {
			return enrollmentRepository.findAll(pageable).map(enrollmentMapper::toDTO);
		}
		String text = keyword.trim();
		return enrollmentRepository
				.findByStudent_NameContainingIgnoreCaseOrTrainingClass_Course_CourseNameContainingIgnoreCase(text, text,
						pageable)
				.map(enrollmentMapper::toDTO);
	}

	@Override
	public Page<EnrollmentDTO> searchStudentListRows(String keyword, Long courseId, Long classTimeId, boolean certificatedOnly, Pageable pageable)
			throws Exception {
		String text = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		LocalTime startTime = null;
		LocalTime endTime = null;
		if (classTimeId != null) {
			TrainingClass selectedClassTime = trainingClassRepository.findById(classTimeId)
					.orElseThrow(() -> new Exception("Class not found with id: " + classTimeId));
			startTime = selectedClassTime.getStartTime();
			endTime = selectedClassTime.getEndTime();
		}
		return enrollmentRepository.searchStudentListRows(text, courseId, startTime, endTime, certificatedOnly, pageable).map(enrollment -> {
			EnrollmentDTO dto = enrollmentMapper.toDTO(enrollment);
			boolean certificated = dto.getStudentId() != null
					&& dto.getCourseId() != null
					&& certificateRepository.existsByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId());
			dto.setCertificated(certificated);
			return dto;
		});
	}

	@Override
	public EnrollmentDTO changeClassTime(Long enrollmentId, Long targetTrainingClassId) throws Exception {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new Exception("Enrollment not found with id: " + enrollmentId));
		TrainingClass targetClass = trainingClassRepository.findById(targetTrainingClassId)
				.orElseThrow(() -> new Exception("Class not found with id: " + targetTrainingClassId));

		Long currentCourseId = enrollment.getTrainingClass() != null && enrollment.getTrainingClass().getCourse() != null
				? enrollment.getTrainingClass().getCourse().getId()
				: null;
		Long targetCourseId = targetClass.getCourse() != null ? targetClass.getCourse().getId() : null;
		if (currentCourseId == null || targetCourseId == null || !Objects.equals(currentCourseId, targetCourseId)) {
			throw new Exception("You can only change to another class time within the same course.");
		}

		EnrollmentDTO dto = enrollmentMapper.toDTO(enrollment);
		dto.setTrainingClassId(targetTrainingClassId);
		validateDuplicate(dto, targetClass);
		validateClassCapacity(dto, targetClass);

		enrollment.setTrainingClass(targetClass);
		User currentUser = getCurrentUser();
		if (currentUser != null) {
			enrollment.setUpdatedBy(currentUser);
		}

		enrollmentRepository.save(enrollment);
		return enrollmentMapper.toDTO(enrollment);
	}

	private void validateDuplicate(EnrollmentDTO dto, TrainingClass trainingClass) throws Exception {
		Long courseId = trainingClass.getCourse() != null ? trainingClass.getCourse().getId() : null;
		if (courseId == null) {
			throw new Exception("Selected class has no course assigned.");
		}

		boolean exists = dto.getId() == null
				? enrollmentRepository.existsByStudentIdAndTrainingClass_Course_Id(dto.getStudentId(), courseId)
				: enrollmentRepository.existsByStudentIdAndTrainingClass_Course_IdAndIdNot(dto.getStudentId(), courseId,
						dto.getId());

		if (exists) {
			throw new Exception("This student is already enrolled in the selected course.");
		}
	}

	private void validateClassCapacity(EnrollmentDTO dto, TrainingClass trainingClass) throws Exception {
		EnrollStatus targetStatus = dto.getStatus() != null ? dto.getStatus() : EnrollStatus.ACTIVE;
		if (!EnrollStatus.ACTIVE.equals(targetStatus)) {
			return;
		}

		long occupied = enrollmentRepository.countByTrainingClassIdAndStatus(dto.getTrainingClassId(), EnrollStatus.ACTIVE);
		if (dto.getId() != null) {
			Enrollment current = enrollmentRepository.findById(dto.getId()).orElse(null);
			if (current != null && current.getTrainingClass() != null
					&& Objects.equals(current.getTrainingClass().getId(), dto.getTrainingClassId())
					&& EnrollStatus.ACTIVE.equals(current.getStatus())) {
				occupied = Math.max(0, occupied - 1);
			}
		}

		Integer maxStudents = trainingClass.getMaxStudents();
		if (maxStudents != null && occupied >= maxStudents) {
			throw new Exception("This class is full. Please choose another class.");
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
