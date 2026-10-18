package com.trainingcenter.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDTO {

	 private Long id;

	    private LocalDateTime createdTime;

	    private LocalDateTime updatedTime;

	    private String createdByName;
	    private Long createdBy;

	    private String updatedByName;
	    private Long updatedBy;
}
