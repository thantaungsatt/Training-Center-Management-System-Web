package com.trainingcenter.repositories;

import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainingcenter.entities.TrainingClass;
import com.trainingcenter.enums.ScheduleType;

public interface TrainingClassRepository extends JpaRepository<TrainingClass, Long> {
    
	boolean existsByStartTimeAndEndTimeAndCourseId(
            LocalTime startTime,
            LocalTime endTime,
            Long courseId
    );

	boolean existsByStartTimeAndEndTimeAndCourseIdAndIdNot(
			LocalTime startTime,
			LocalTime endTime,
			Long courseId,
			Long id
	);
	
	@Query("""
			SELECT t FROM TrainingClass t
			WHERE (:start IS NULL OR t.startTime >= :start)
			AND (:end IS NULL OR t.endTime <= :end)
			AND (
			    :keyword IS NULL
			    OR LOWER(t.course.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR LOWER(t.instructor.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			    OR LOWER(COALESCE(t.room, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:schedule IS NULL OR t.scheduleType = :schedule)
			""")
			Page<TrainingClass> search(
			    @Param("start") LocalTime start,
			    @Param("end") LocalTime end,
			    @Param("keyword") String keyword,
			    @Param("schedule") ScheduleType schedule,
			    Pageable pageable
			);
    
}
