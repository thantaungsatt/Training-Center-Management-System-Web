package com.trainingcenter.services.enrollment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainingcenter.dto.EnrollmentDTO;
import com.trainingcenter.dto.EnrollmentFormDTO;
import com.trainingcenter.dto.PaymentDTO;
import com.trainingcenter.dto.StudentDTO;
import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.enums.EnrollStatus;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.PaymentType;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.repositories.TrainingClassRepository;
import com.trainingcenter.services.payment.PaymentService;
import com.trainingcenter.services.student.StudentService;

@Service
public class EnrollmentWizardService {

	private final StudentService studentService;
	private final EnrollmentService enrollmentService;
	private final PaymentService paymentService;
	private final TrainingClassRepository trainingClassRepository;

	public EnrollmentWizardService(StudentService studentService, EnrollmentService enrollmentService,
			PaymentService paymentService, TrainingClassRepository trainingClassRepository) {
		this.studentService = studentService;
		this.enrollmentService = enrollmentService;
		this.paymentService = paymentService;
		this.trainingClassRepository = trainingClassRepository;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveAll(EnrollmentFormDTO form) throws Exception {
		if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new Exception("Paid amount must be greater than 0. Enter an amount before confirming enrollment.");
		}

		Long studentId = resolveStudentId(form);

		EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
		enrollmentDTO.setStudentId(studentId);
		enrollmentDTO.setTrainingClassId(form.getTrainingClassId());
		enrollmentDTO.setEnrollDate(form.getEnrollDate());
		enrollmentDTO
				.setStatus(form.getEnrollmentStatus() != null ? form.getEnrollmentStatus() : EnrollStatus.ACTIVE);
		EnrollmentDTO savedEnrollment = enrollmentService.save(enrollmentDTO);

		PaymentDTO paymentDTO = new PaymentDTO();
		paymentDTO.setEnrollmentId(savedEnrollment.getId());
		paymentDTO.setAmount(form.getAmount());
		paymentDTO.setDiscountPercent(normalizeDiscountPercent(form.getDiscountPercent()));
		paymentDTO.setPaymentDate(form.getPaymentDate());
		paymentDTO.setPaymentType(form.getPaymentType() != null ? form.getPaymentType() : PaymentType.CASH);
		paymentDTO.setStatus(resolvePaymentStatus(form));
		paymentDTO.setNote(buildPaymentNote(form));

		paymentService.save(paymentDTO);
	}

	private Long resolveStudentId(EnrollmentFormDTO form) throws Exception {
		if (Boolean.TRUE.equals(form.getReturningStudent())) {
			if (form.getExistingStudentId() == null) {
				throw new Exception("Please select an existing student.");
			}
			StudentDTO existingStudent = studentService.getStudentById(form.getExistingStudentId());
			return existingStudent.getId();
		}

		StudentDTO studentDTO = new StudentDTO();
		studentDTO.setName(form.getName());
		studentDTO.setFatherName(form.getFatherName());
		studentDTO.setDateOfBirth(form.getDateOfBirth());
		studentDTO.setPhoneNumber(form.getPhoneNumber());
		studentDTO.setEmail(form.getEmail());
		studentDTO.setAddress(form.getAddress());
		studentDTO.setStatus(form.getStudentStatus() != null ? form.getStudentStatus() : StudentStatus.ACTIVE);
		StudentDTO savedStudent = studentService.save(studentDTO);
		return savedStudent.getId();
	}

	public BigDecimal resolveCourseFee(Long trainingClassId) throws Exception {
		if (trainingClassId == null) {
			return BigDecimal.ZERO;
		}
		TrainingClass trainingClass = trainingClassRepository.findById(trainingClassId)
				.orElseThrow(() -> new Exception("Class not found with id: " + trainingClassId));
		if (trainingClass.getCourse() == null || trainingClass.getCourse().getFee() == null) {
			return BigDecimal.ZERO;
		}
		return trainingClass.getCourse().getFee();
	}

	private PaymentStatus resolvePaymentStatus(EnrollmentFormDTO form) throws Exception {
		BigDecimal fee = resolveDiscountedFee(form.getTrainingClassId(), form.getDiscountPercent());
		if (form.getAmount() == null || form.getAmount().compareTo(fee) < 0) {
			return PaymentStatus.PARTIAL;
		}
		return PaymentStatus.PAID;
	}

	private String buildPaymentNote(EnrollmentFormDTO form) throws Exception {
		String baseNote = form.getNote() == null ? "" : form.getNote().trim();
		if (!Boolean.TRUE.equals(form.getInstallment())) {
			return baseNote.isEmpty() ? null : baseNote;
		}

		BigDecimal fee = resolveDiscountedFee(form.getTrainingClassId(), form.getDiscountPercent());
		BigDecimal paid = form.getAmount() == null ? BigDecimal.ZERO : form.getAmount();
		BigDecimal remaining = fee.subtract(paid);
		if (remaining.compareTo(BigDecimal.ZERO) < 0) {
			remaining = BigDecimal.ZERO;
		}
		Integer count = form.getInstallmentCount() != null && form.getInstallmentCount() > 0 ? form.getInstallmentCount() : 2;
		BigDecimal eachInstallment = count > 0
				? remaining.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP)
				: remaining;

		String installmentSummary = "Installment plan: remaining " + remaining.toPlainString() + " over " + count
				+ " installments (" + eachInstallment.toPlainString() + " each).";

		if (baseNote.isEmpty()) {
			return installmentSummary;
		}
		return baseNote + " | " + installmentSummary;
	}

	public BigDecimal resolveDiscountedFee(Long trainingClassId, BigDecimal discountPercent) throws Exception {
		BigDecimal fee = resolveCourseFee(trainingClassId);
		BigDecimal discount = normalizeDiscountPercent(discountPercent);
		if (discount.compareTo(BigDecimal.ZERO) <= 0) {
			return fee;
		}
		BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
		BigDecimal discountedFee = fee.multiply(factor).setScale(2, RoundingMode.HALF_UP);
		if (discountedFee.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		return discountedFee;
	}

	private BigDecimal normalizeDiscountPercent(BigDecimal discountPercent) {
		if (discountPercent == null) {
			return BigDecimal.ZERO;
		}
		if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}
		return discountPercent;
	}
}
