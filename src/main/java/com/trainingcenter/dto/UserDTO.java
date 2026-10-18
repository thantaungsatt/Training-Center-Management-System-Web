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
public class UserDTO extends BaseDTO {

	private String name;

    private String username;

    private String phoneNumber;

    private String email;
    
    private String password;

    private Long roleId;
    private String roleName;

    private ActiveStatus status;
	
}
