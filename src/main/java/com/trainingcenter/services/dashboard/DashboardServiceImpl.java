package com.trainingcenter.services.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainingcenter.dto.DashboardDTO;
import com.trainingcenter.dto.DashboardDTO.ChartData;
import com.trainingcenter.dto.DashboardDTO.RecentEnrollment;
import com.trainingcenter.entities.Enrollment;
import com.trainingcenter.enums.PaymentStatus;
import com.trainingcenter.enums.StudentStatus;
import com.trainingcenter.repositories.EnrollmentRepository;
import com.trainingcenter.repositories.InstructorRepository;
import com.trainingcenter.repositories.PaymentRepository;
import com.trainingcenter.repositories.StudentRepository;
import com.trainingcenter.repositories.TrainingClassRepository;

/** Repository-backed dashboard aggregation service. */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private static final List<PaymentStatus> COLLECTED_PAYMENT_STATUSES = List.of(PaymentStatus.PAID, PaymentStatus.PARTIAL);
	private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

	private final StudentRepository studentRepository;
	private final TrainingClassRepository trainingClassRepository;
	private final PaymentRepository paymentRepository;
	private final InstructorRepository instructorRepository;
	private final EnrollmentRepository enrollmentRepository;

	public DashboardServiceImpl(StudentRepository studentRepository, TrainingClassRepository trainingClassRepository,
			PaymentRepository paymentRepository, InstructorRepository instructorRepository,
			EnrollmentRepository enrollmentRepository) {
		this.studentRepository = studentRepository;
		this.trainingClassRepository = trainingClassRepository;
		this.paymentRepository = paymentRepository;
		this.instructorRepository = instructorRepository;
		this.enrollmentRepository = enrollmentRepository;
	}

	@Override
	public DashboardDTO getDashboard() {
		YearMonth currentMonth = YearMonth.now();
		ChartData revenueChart = buildRevenueChart(currentMonth);
		List<RecentEnrollment> recentEnrollments = loadRecentEnrollments();
		BigDecimal currentRevenue = revenueChart.getValues().getLast();

		return new DashboardDTO(studentRepository.count(), studentRepository.countByStatus(StudentStatus.ACTIVE),
				trainingClassRepository.count(), currentRevenue, instructorRepository.count(), revenueChart,
				buildEnrollmentChart(), recentEnrollments);
	}

	private ChartData buildRevenueChart(YearMonth currentMonth) {
		List<String> labels = new ArrayList<>();
		List<BigDecimal> values = new ArrayList<>();
		for (int offset = 5; offset >= 0; offset--) {
			YearMonth month = currentMonth.minusMonths(offset);
			labels.add(month.format(MONTH_FORMATTER));
			values.add(paymentRepository.sumCollectedBetween(month.atDay(1), month.plusMonths(1).atDay(1),
					COLLECTED_PAYMENT_STATUSES));
		}
		return new ChartData(labels, values);
	}

	private ChartData buildEnrollmentChart() {
		Pageable topFive = PageRequest.of(0, 5);
		List<String> labels = new ArrayList<>();
		List<BigDecimal> values = new ArrayList<>();
		enrollmentRepository.findTopCourseEnrollments(topFive).forEach(course -> {
			labels.add(course.getCourseName());
			values.add(BigDecimal.valueOf(course.getEnrollmentCount()));
		});
		return new ChartData(labels, values);
	}

	private List<RecentEnrollment> loadRecentEnrollments() {
		return enrollmentRepository.findRecentEnrollments(PageRequest.of(0, 8)).stream()
				.map(this::toRecentEnrollment).toList();
	}

	private RecentEnrollment toRecentEnrollment(Enrollment enrollment) {
		return new RecentEnrollment(enrollment.getStudent().getName(),
				enrollment.getTrainingClass().getCourse().getCourseName(), enrollment.getEnrollDate(), enrollment.getStatus());
	}
}
