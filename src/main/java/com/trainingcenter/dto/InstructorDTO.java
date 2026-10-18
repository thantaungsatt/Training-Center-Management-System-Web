package com.trainingcenter.dto;

import com.trainingcenter.enums.ActiveStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDTO extends BaseDTO {

	private String name;
	
	private String phoneNumber;
	
	private String email;
	
	private String address;
	
	private ActiveStatus status;
}
