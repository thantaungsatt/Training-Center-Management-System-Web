package com.trainingcenter.services.trainingClass;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.enums.ScheduleType;

public interface TrainingClassService {

	TrainingClassDTO save(TrainingClassDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	TrainingClassDTO getTrainingClassById(Long id) throws Exception;

	List<TrainingClassDTO> getAllTrainingClasses() throws Exception;

	Page<TrainingClassDTO> search(
		    String keyword,
		    LocalTime start,
		    LocalTime end,
		    ScheduleType schedule,
		    Pageable pageable
		);
}
