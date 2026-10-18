package com.trainingcenter.services.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.EnrollmentPaymentSummaryDTO;
import com.trainingcenter.dto.PaymentDTO;
import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.entities.Payment;
import com.trainingcenter.entities.User;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.mapper.PaymentMapper;
import com.trainingcenter.repositories.EnrollmentRepository;
import com.trainingcenter.repositories.PaymentRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final PaymentMapper paymentMapper;
	private final UserRepository userRepository;

	public PaymentServiceImpl(PaymentRepository paymentRepository, EnrollmentRepository enrollmentRepository,
			PaymentMapper paymentMapper, UserRepository userRepository) {
		this.paymentRepository = paymentRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.paymentMapper = paymentMapper;
		this.userRepository = userRepository;
	}

	@Override
	public PaymentDTO save(PaymentDTO dto) throws Exception {
		validateAmount(dto);
		Enrollment enrollment = enrollmentRepository.findById(dto.getEnrollmentId())
				.orElseThrow(() -> new Exception("Enrollment not found with id: " + dto.getEnrollmentId()));
		BigDecimal courseFee = resolveEnrollmentCourseFee(enrollment);
		EnrollmentPaymentSummaryDTO summary = getEnrollmentPaymentSummary(dto.getEnrollmentId(), dto.getId());
		BigDecimal effectiveTotalFee = dto.getDiscountPercent() != null
				? resolveEffectiveTotalFee(courseFee, dto.getDiscountPercent())
				: summary.getTotalFee();
		validateAmountAgainstRemaining(dto, summary);
		validateAmountAgainstDiscountedRemaining(dto, summary, effectiveTotalFee);
		dto.setStatus(resolvePaymentStatus(summary, dto.getAmount(), effectiveTotalFee));

		User currentUser = getCurrentUser();
		Payment payment;
		if (dto.getId() != null) {
			payment = paymentRepository.findById(dto.getId())
					.orElseThrow(() -> new Exception("Payment not found with id: " + dto.getId()));
			paymentMapper.updateEntity(payment, dto, enrollment);
			if (currentUser != null) {
				payment.setUpdatedBy(currentUser);
			}
		} else {
			payment = paymentMapper.toEntity(dto, enrollment);
			if (currentUser != null) {
				payment.setCreatedBy(currentUser);
				payment.setUpdatedBy(currentUser);
			}
		}

		paymentRepository.save(payment);
		return paymentMapper.toDTO(payment);
	}


	@Override
	public PaymentDTO getPaymentById(Long id) throws Exception {
		Payment payment = paymentRepository.findById(id).orElseThrow(() -> new Exception("Payment not found with id: " + id));
		return paymentMapper.toDTO(payment);
	}

	@Override
	public List<PaymentDTO> getByStudentId(Long studentId) throws Exception {
		return paymentRepository.findByEnrollment_Student_IdOrderByPaymentDateDescIdDesc(studentId).stream()
				.map(paymentMapper::toDTO).toList();
	}

	@Override
	public Page<PaymentDTO> searchPayments(String keyword, Pageable pageable) throws Exception {
		if (Objects.isNull(keyword) || keyword.isBlank()) {
			return paymentRepository.findAll(pageable).map(paymentMapper::toDTO);
		}
		return paymentRepository.search(keyword.trim(), pageable).map(paymentMapper::toDTO);
	}

	@Override
	public EnrollmentPaymentSummaryDTO getEnrollmentPaymentSummary(Long enrollmentId, Long excludePaymentId) throws Exception {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new Exception("Enrollment not found with id: " + enrollmentId));
		BigDecimal totalFee = resolveEnrollmentCourseFee(enrollment);
		BigDecimal savedDiscountPercent = resolveSavedDiscountPercent(enrollmentId);
		BigDecimal effectiveTotalFee = resolveEffectiveTotalFee(totalFee, savedDiscountPercent);

		BigDecimal totalPaid = paymentRepository
				.sumAmountByEnrollmentIdExcludingPaymentId(enrollmentId, excludePaymentId)
				.orElse(BigDecimal.ZERO);
		BigDecimal remaining = effectiveTotalFee.subtract(totalPaid);
		if (remaining.compareTo(BigDecimal.ZERO) < 0) {
			remaining = BigDecimal.ZERO;
		}
		return new EnrollmentPaymentSummaryDTO(enrollmentId, effectiveTotalFee, totalPaid, remaining);
	}

	private void validateAmount(PaymentDTO dto) throws Exception {
		if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new Exception("Payment amount must be greater than zero.");
		}
	}

	private void validateAmountAgainstRemaining(PaymentDTO dto, EnrollmentPaymentSummaryDTO summary) throws Exception {
		if (dto.getAmount().compareTo(summary.getRemainingBalance()) > 0) {
			throw new Exception("Payment amount cannot be greater than remaining balance.");
		}
	}

	private void validateAmountAgainstDiscountedRemaining(PaymentDTO dto, EnrollmentPaymentSummaryDTO summary,
			BigDecimal effectiveTotalFee) throws Exception {
		BigDecimal remaining = effectiveTotalFee.subtract(summary.getTotalPaid());
		if (remaining.compareTo(BigDecimal.ZERO) < 0) {
			remaining = BigDecimal.ZERO;
		}
		if (dto.getAmount().compareTo(remaining) > 0) {
			throw new Exception("Payment amount cannot be greater than discounted remaining balance.");
		}
	}

	private PaymentStatus resolvePaymentStatus(EnrollmentPaymentSummaryDTO summary, BigDecimal currentAmount,
			BigDecimal effectiveTotalFee) {
		BigDecimal cumulativePaid = summary.getTotalPaid().add(currentAmount);
		if (cumulativePaid.compareTo(effectiveTotalFee) >= 0) {
			return PaymentStatus.PAID;
		}
		return PaymentStatus.PARTIAL;
	}

	private BigDecimal resolveEffectiveTotalFee(BigDecimal totalFee, BigDecimal discountPercent) {
		BigDecimal safeTotalFee = totalFee != null ? totalFee : BigDecimal.ZERO;
		if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
			return safeTotalFee;
		}
		BigDecimal normalizedDiscount = discountPercent.min(BigDecimal.valueOf(100));
		BigDecimal factor = BigDecimal.ONE.subtract(
				normalizedDiscount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
		BigDecimal discounted = safeTotalFee.multiply(factor).setScale(2, RoundingMode.HALF_UP);
		if (discounted.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		return discounted;
	}

	private BigDecimal resolveEnrollmentCourseFee(Enrollment enrollment) {
		if (enrollment.getTrainingClass() == null
				|| enrollment.getTrainingClass().getCourse() == null
				|| enrollment.getTrainingClass().getCourse().getFee() == null) {
			return BigDecimal.ZERO;
		}
		return enrollment.getTrainingClass().getCourse().getFee();
	}

	private BigDecimal resolveSavedDiscountPercent(Long enrollmentId) {
		return paymentRepository.findTopByEnrollment_IdAndDiscountPercentIsNotNullOrderByPaymentDateDescIdDesc(enrollmentId)
				.map(Payment::getDiscountPercent)
				.orElse(BigDecimal.ZERO);
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
