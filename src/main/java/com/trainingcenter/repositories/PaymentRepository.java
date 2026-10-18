package com.trainingcenter.repositories;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainingcenter.entities.Payment;
import com.trainingcenter.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByEnrollmentId(Long enrollmentId);

	@Query("""
			SELECT p FROM Payment p
			WHERE :keyword IS NULL
			OR LOWER(p.enrollment.student.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			OR LOWER(p.enrollment.trainingClass.course.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""")
	Page<Payment> search(@Param("keyword") String keyword, Pageable pageable);

	List<Payment> findByEnrollment_Student_IdOrderByPaymentDateDescIdDesc(Long studentId);

	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.enrollment.id = :enrollmentId")
	Optional<java.math.BigDecimal> sumAmountByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

	@Query("""
			SELECT COALESCE(SUM(p.amount), 0)
			FROM Payment p
			WHERE p.enrollment.id = :enrollmentId
			AND (:excludePaymentId IS NULL OR p.id <> :excludePaymentId)
			""")
	Optional<java.math.BigDecimal> sumAmountByEnrollmentIdExcludingPaymentId(
			@Param("enrollmentId") Long enrollmentId,
			@Param("excludePaymentId") Long excludePaymentId);

	Optional<Payment> findTopByEnrollment_IdAndDiscountPercentIsNotNullOrderByPaymentDateDescIdDesc(Long enrollmentId);

	/** Counts collected money only; reversed and outstanding payments are excluded. */
	@Query("""
			SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
			WHERE p.paymentDate >= :fromDate AND p.paymentDate < :toDate
			AND p.status IN :collectedStatuses
			""")
	BigDecimal sumCollectedBetween(@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate,
			@Param("collectedStatuses") List<PaymentStatus> collectedStatuses);
}
