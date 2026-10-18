package com.trainingcenter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainingcenter.entities.Student;
import com.trainingcenter.enums.StudentStatus;

public interface StudentRepository extends JpaRepository<Student, Long> {

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByEmail(String email);

	long countByStatus(StudentStatus status);

	Page<Student> findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
			String name, String phoneNumber, String email, Pageable pageable);

	@Query("""
			SELECT s FROM Student s
			WHERE (:keyword IS NULL
				OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:trainingClassId IS NULL
				OR EXISTS (
					SELECT 1 FROM Enrollment e
					WHERE e.student.id = s.id
					AND e.trainingClass.id = :trainingClassId
				))
			""")
	Page<Student> searchByKeywordAndClass(@Param("keyword") String keyword, @Param("trainingClassId") Long trainingClassId,
			Pageable pageable);
}
