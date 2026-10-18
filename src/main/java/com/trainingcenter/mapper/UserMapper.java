package com.trainingcenter.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.trainingcenter.dto.UserDTO;
import com.trainingcenter.entities.Role;
import com.trainingcenter.entities.User;
import com.trainingcenter.enums.ActiveStatus;

@Component
public class UserMapper {

    //  Entity -> DTO
    public UserDTO toDTO(User user) {

        if (user == null) return null;

        UserDTO dto = new UserDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());

        // Role
        if (user.getRole() != null) {
        	dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
        }

        // BaseEntity
        dto.setCreatedTime(user.getCreatedTime());
        dto.setUpdatedTime(user.getUpdatedTime());

        if (user.getCreatedBy() != null) {
            dto.setCreatedByName(user.getCreatedBy().getName());
        }

        if (user.getUpdatedBy() != null) {
            dto.setUpdatedByName(user.getUpdatedBy().getName());
        }

        return dto;
    }

    //  DTO -> Entity (Create)
    public User toEntity(UserDTO dto, Role role, PasswordEncoder encoder) {

        User user = new User();

        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setStatus(ActiveStatus.ACTIVE);

        return user;
    }

    // Update Entity
    public void updateEntity(User user, UserDTO dto, Role role) {

        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setRole(role);
        user.setStatus(dto.getStatus());
    }
}