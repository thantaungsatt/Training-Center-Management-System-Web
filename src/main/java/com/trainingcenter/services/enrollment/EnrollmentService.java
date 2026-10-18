package com.trainingcenter.services.enrollment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentListViewDTO;
import com.trainingcenter.enums.PaymentStatus;

public interface EnrollmentService {

	EnrollmentDTO save(EnrollmentDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	EnrollmentDTO getEnrollmentById(Long id) throws Exception;

	List<EnrollmentDTO> getAllEnrollments() throws Exception;

	List<EnrollmentDTO> getByStudentId(Long studentId) throws Exception;

	Page<EnrollmentListViewDTO> getEnrollmentListView(String keyword, PaymentStatus paymentStatus, Pageable pageable)
			throws Exception;

	Page<EnrollmentDTO> searchEnrollments(String keyword, Pageable pageable) throws Exception;

	Page<EnrollmentDTO> searchStudentListRows(String keyword, Long courseId, Long classTimeId, boolean certificated, Pageable pageable) throws Exception;

	EnrollmentDTO changeClassTime(Long enrollmentId, Long targetTrainingClassId) throws Exception;
}
