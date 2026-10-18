package com.trainingcenter.mapper;

import org.springframework.stereotype.Component;

import com.trainingcenter.dto.RoleDTO;
import com.trainingcenter.entities.Role;

@Component
public class RoleMapper {

    public RoleDTO toDTO(Role role) {

        RoleDTO dto = new RoleDTO();

        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());

        dto.setCreatedTime(role.getCreatedTime());
        dto.setUpdatedTime(role.getUpdatedTime());

        if (role.getCreatedBy() != null) {
            dto.setCreatedByName(role.getCreatedBy().getName());
        }

        if (role.getUpdatedBy() != null) {
            dto.setUpdatedByName(role.getUpdatedBy().getName());
        }

        return dto;
    }

    public Role toEntity(RoleDTO dto) {

        Role role = new Role();

        role.setName(dto.getName());
        role.setCode(dto.getCode());

        return role;
    }

    public void update(Role role, RoleDTO dto) {

        role.setName(dto.getName());
        role.setCode(dto.getCode());
    }
}
