package com.trainingcenter.services.certificate;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.CertificateDTO;
import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentPaymentSummaryDTO;
import com.trainingcenter.entities.Certificate;
import com.trainingcenter.entities.Course;
import com.trainingcenter.entities.Student;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.CertificateMapper;
import com.trainingcenter.repositories.CertificateRepository;
import com.trainingcenter.repositories.CourseRepository;
import com.trainingcenter.repositories.StudentRepository;
import com.trainingcenter.repositories.UserRepository;
import com.trainingcenter.services.enrollment.EnrollmentService;
import com.trainingcenter.services.payment.PaymentService;

@Service
public class CertificateServiceImpl implements CertificateService {

	private final CertificateRepository certificateRepository;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;
	private final CertificateMapper certificateMapper;
	private final EnrollmentService enrollmentService;
	private final PaymentService paymentService;

	public CertificateServiceImpl(CertificateRepository certificateRepository, StudentRepository studentRepository,
			CourseRepository courseRepository, UserRepository userRepository, CertificateMapper certificateMapper,
			EnrollmentService enrollmentService, PaymentService paymentService) {
		this.certificateRepository = certificateRepository;
		this.studentRepository = studentRepository;
		this.courseRepository = courseRepository;
		this.userRepository = userRepository;
		this.certificateMapper = certificateMapper;
		this.enrollmentService = enrollmentService;
		this.paymentService = paymentService;
	}

	@Override
	public CertificateDTO save(CertificateDTO dto) throws Exception {
		Student student = studentRepository.findById(dto.getStudentId())
				.orElseThrow(() -> new Exception("Student not found with id: " + dto.getStudentId()));
		Course course = courseRepository.findById(dto.getCourseId())
				.orElseThrow(() -> new Exception("Course not found with id: " + dto.getCourseId()));

		validateCoursePaid(student.getId(), course.getId());
		validateCertificateNo(dto);

		User currentUser = getCurrentUser();
		Certificate entity;
		if (dto.getId() != null) {
			entity = certificateRepository.findById(dto.getId())
					.orElseThrow(() -> new Exception("Certificate not found with id: " + dto.getId()));
			certificateMapper.updateEntity(entity, dto, student, course);
			if (currentUser != null) {
				entity.setUpdatedBy(currentUser);
			}
		} else {
			entity = certificateMapper.toEntity(dto, student, course);
			if (currentUser != null) {
				entity.setCreatedBy(currentUser);
				entity.setUpdatedBy(currentUser);
			}
		}

		certificateRepository.save(entity);
		return certificateMapper.toDTO(entity);
	}

	@Override
	public void delete(Long id) throws Exception {
		Certificate entity = certificateRepository.findById(id)
				.orElseThrow(() -> new Exception("Certificate not found with id: " + id));
		certificateRepository.delete(entity);
	}

	@Override
	public CertificateDTO getById(Long id) throws Exception {
		Certificate entity = certificateRepository.findById(id)
				.orElseThrow(() -> new Exception("Certificate not found with id: " + id));
		return certificateMapper.toDTO(entity);
	}

	@Override
	public Page<CertificateDTO> search(String keyword, Pageable pageable) throws Exception {
		String text = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		return certificateRepository.search(text, pageable).map(certificateMapper::toDTO);
	}

	@Override
	public String generateNextSerialByCourse(Long courseId) throws Exception {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new Exception("Course not found with id: " + courseId));
		String prefix = course.getPrefix() == null ? "" : course.getPrefix().trim().toUpperCase();
		if (prefix.isBlank()) {
			throw new Exception("Course prefix is not configured for this course: " + course.getCourseName());
		}
		long next = certificateRepository.countByPrefix(prefix) + 1;
		return prefix + "-" + String.format("%06d", next);
	}

	@Override
	public boolean existsByStudentAndCourse(Long studentId, Long courseId) {
		return certificateRepository.existsByStudentIdAndCourseId(studentId, courseId);
	}

	private void validateCertificateNo(CertificateDTO dto) throws Exception {
		if (dto.getCertificateNo() == null || dto.getCertificateNo().isBlank()) {
			throw new Exception("Certificate serial number is required.");
		}
		dto.setCertificateNo(dto.getCertificateNo().trim().toUpperCase());
		boolean exists = dto.getId() == null ? certificateRepository.existsByCertificateNo(dto.getCertificateNo())
				: certificateRepository.existsByCertificateNoAndIdNot(dto.getCertificateNo(), dto.getId());
		if (exists) {
			throw new Exception("Certificate serial already exists: " + dto.getCertificateNo());
		}
	}

	private void validateCoursePaid(Long studentId, Long courseId) throws Exception {
		EnrollmentDTO eligibleEnrollment = enrollmentService.getByStudentId(studentId).stream()
				.filter(enrollment -> Objects.equals(enrollment.getCourseId(), courseId))
				.findFirst()
				.orElseThrow(() -> new Exception("A student must be enrolled in this course before a certificate can be issued."));
		EnrollmentPaymentSummaryDTO summary = paymentService.getEnrollmentPaymentSummary(eligibleEnrollment.getId(), null);
		if (summary.getRemainingBalance() == null || summary.getRemainingBalance().signum() > 0) {
			throw new Exception("The course balance must be fully paid before a certificate can be issued.");
		}
	}

	private User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		String username = authentication.getName();
		if (username == null || username.isBlank() || Objects.equals(username, "anonymousUser")) {
			return null;
		}
		return userRepository.findByUsername(username).orElse(null);
	}
}
