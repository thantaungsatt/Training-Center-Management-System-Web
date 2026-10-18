package com.trainingcenter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainingcenter.entities.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

	boolean existsByCertificateNo(String certificateNo);

	boolean existsByCertificateNoAndIdNot(String certificateNo, Long id);

	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

	@Query("""
			SELECT c FROM Certificate c
			WHERE :keyword IS NULL
				OR LOWER(c.certificateNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(c.student.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(c.course.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""")
	Page<Certificate> search(@Param("keyword") String keyword, Pageable pageable);

	@Query("""
			SELECT COUNT(c) FROM Certificate c
			WHERE c.certificateNo LIKE CONCAT(:prefix, '-%')
			""")
	long countByPrefix(@Param("prefix") String prefix);
}
