package com.trainingcenter.services.certificate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.CertificateDTO;

public interface CertificateService {

	CertificateDTO save(CertificateDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	CertificateDTO getById(Long id) throws Exception;

	Page<CertificateDTO> search(String keyword, Pageable pageable) throws Exception;

	String generateNextSerialByCourse(Long courseId) throws Exception;

	boolean existsByStudentAndCourse(Long studentId, Long courseId);
}
