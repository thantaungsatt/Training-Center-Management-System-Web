package com.trainingcenter.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO extends BaseDTO {

	private String courseName;

	private String prefix;
	
	private String duration;
	
	private BigDecimal fee;
	
	private String description;
}
