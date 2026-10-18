package com.trainingcenter.repositories;

import java.util.List;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.repositories.projection.EnrollmentPaymentListProjection;
import com.trainingcenter.repositories.projection.CourseEnrollmentProjection;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndTrainingClassId(Long studentId, Long trainingClassId);

	boolean existsByStudentIdAndTrainingClassIdAndIdNot(Long studentId, Long trainingClassId, Long id);

	boolean existsByStudentIdAndTrainingClass_Course_Id(Long studentId, Long courseId);

	boolean existsByStudentIdAndTrainingClass_Course_IdAndIdNot(Long studentId, Long courseId, Long id);

	long countByTrainingClassIdAndStatus(Long trainingClassId, EnrollStatus status);

	boolean existsByStudentId(Long studentId);

	Page<Enrollment> findByStudent_NameContainingIgnoreCaseOrTrainingClass_Course_CourseNameContainingIgnoreCase(
			String studentName, String courseName, Pageable pageable);

	List<Enrollment> findByStudentIdOrderByEnrollDateDescIdDesc(Long studentId);

	/** Fetches the newest rows with their display relationships in one query. */
	@Query("""
			SELECT e FROM Enrollment e
			JOIN FETCH e.student
			JOIN FETCH e.trainingClass tc
			JOIN FETCH tc.course
			ORDER BY e.enrollDate DESC, e.id DESC
			""")
	List<Enrollment> findRecentEnrollments(Pageable pageable);

	/** Returns the most popular courses, ordered for the dashboard chart. */
	@Query("""
			SELECT tc.course.courseName AS courseName, COUNT(e) AS enrollmentCount
			FROM Enrollment e JOIN e.trainingClass tc
			GROUP BY tc.course.courseName
			ORDER BY COUNT(e) DESC, tc.course.courseName ASC
			""")
	List<CourseEnrollmentProjection> findTopCourseEnrollments(Pageable pageable);

	@Query("""
			SELECT e FROM Enrollment e
			WHERE (:keyword IS NULL
				OR LOWER(e.student.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(e.student.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(COALESCE(e.student.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (:courseId IS NULL OR e.trainingClass.course.id = :courseId)
			AND (:startTime IS NULL OR e.trainingClass.startTime = :startTime)
			AND (:endTime IS NULL OR e.trainingClass.endTime = :endTime)
			AND ((:certificated = true AND EXISTS (
				SELECT cert.id FROM Certificate cert
				WHERE cert.student = e.student AND cert.course = e.trainingClass.course
			)) OR (:certificated = false AND e.student.status = com.trainingcenter.enums.StudentStatus.ACTIVE AND NOT EXISTS (
				SELECT cert.id FROM Certificate cert
				WHERE cert.student = e.student AND cert.course = e.trainingClass.course
			)))
			""")
	Page<Enrollment> searchStudentListRows(@Param("keyword") String keyword,
			@Param("courseId") Long courseId,
			@Param("startTime") LocalTime startTime,
			@Param("endTime") LocalTime endTime,
			@Param("certificated") boolean certificated,
			Pageable pageable);

	@Query(value = """
			SELECT
				e.id AS enrollmentId,
				s.id AS studentId,
				s.name AS studentName,
				s.father_name AS fatherName,
				c.course_name AS courseName,
				COALESCE((
					SELECT p.status
					FROM payments p
					WHERE p.enroll_id = e.id
					ORDER BY p.payment_date DESC, p.id DESC
					LIMIT 1
				), 'UNPAID') AS paymentStatus,
				GREATEST(
					(
						COALESCE(c.fee, 0) * (
							1 - (
								COALESCE((
									SELECT p4.discount_percent
									FROM payments p4
									WHERE p4.enroll_id = e.id
									AND p4.discount_percent IS NOT NULL
									ORDER BY p4.payment_date DESC, p4.id DESC
									LIMIT 1
								), 0) / 100
							)
						)
					) - COALESCE((
						SELECT SUM(p2.amount)
						FROM payments p2
						WHERE p2.enroll_id = e.id
					), 0),
					0
				) AS balance
			FROM enrollments e
			INNER JOIN students s ON s.id = e.student_id
			INNER JOIN classes cl ON cl.id = e.class_id
			INNER JOIN courses c ON c.id = cl.course_id
			WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (
				:paymentStatus IS NULL
				OR COALESCE((
					SELECT p3.status
					FROM payments p3
					WHERE p3.enroll_id = e.id
					ORDER BY p3.payment_date DESC, p3.id DESC
					LIMIT 1
				), 'UNPAID') = :paymentStatus
			)
			ORDER BY e.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM enrollments e
			INNER JOIN students s ON s.id = e.student_id
			WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
			AND (
				:paymentStatus IS NULL
				OR COALESCE((
					SELECT p3.status
					FROM payments p3
					WHERE p3.enroll_id = e.id
					ORDER BY p3.payment_date DESC, p3.id DESC
					LIMIT 1
				), 'UNPAID') = :paymentStatus
			)
			""", nativeQuery = true)
	Page<EnrollmentPaymentListProjection> findEnrollmentPaymentList(
			@Param("keyword") String keyword,
			@Param("paymentStatus") String paymentStatus,
			Pageable pageable);
}
