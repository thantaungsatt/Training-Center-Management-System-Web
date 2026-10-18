package com.trainingcenter.dto;

import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trainingcenter.enums.ScheduleType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingClassDTO extends BaseDTO{

	@JsonFormat(pattern = "HH:mm")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime startTime;
	
	@JsonFormat(pattern = "HH:mm")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime endTime;
	
	private ScheduleType scheduleType;
	
	private Long courseId;
	private String courseName;
	
	private Long instructorId;
	private String name;
	
	private String room;
	
	private Integer maxStudents;
}
