package com.trainingcenter.services.role;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.RoleDTO;

public interface RoleService {

    RoleDTO save(RoleDTO dto) throws Exception;   // create + update

    void delete(Long id) throws Exception;

    RoleDTO getRoleById(Long id) throws Exception;

    List<RoleDTO> getAllRoles() throws Exception;

    Page<RoleDTO> getRoles(Pageable pageable) throws Exception;
}
