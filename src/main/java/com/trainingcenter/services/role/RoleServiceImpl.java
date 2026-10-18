package com.trainingcenter.services.role;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.RoleDTO;
import com.trainingcenter.entities.Role;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.RoleMapper;
import com.trainingcenter.repositories.RoleRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
                           RoleMapper roleMapper,
                           UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.userRepository = userRepository;
    }

    // CREATE + UPDATE
    @Override
    public RoleDTO save(RoleDTO dto) throws Exception {

        Role role;
        User currentUser = getCurrentUser();

        if (dto.getId() != null) {
            // UPDATE
            role = roleRepository.findById(dto.getId())
                    .orElseThrow(() -> new Exception("Role not found with id: " + dto.getId()));

            roleMapper.update(role, dto);
            if (currentUser != null) {
                role.setUpdatedBy(currentUser);
            }

        } else {
            // CREATE
            role = roleMapper.toEntity(dto);

            // Optional: prevent duplicate code
            roleRepository.findByCode(dto.getCode())
                    .ifPresent(r -> {
                        throw new RuntimeException("Role code already exists");
                    });
            if (currentUser != null) {
                role.setCreatedBy(currentUser);
                role.setUpdatedBy(currentUser);
            }
        }

        roleRepository.save(role);

        return roleMapper.toDTO(role);
    }

    // DELETE
    @Override
    public void delete(Long id) throws Exception {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new Exception("Role not found with id: " + id));

        roleRepository.delete(role);
    }

    // GET BY ID
    @Override
    public RoleDTO getRoleById(Long id) throws Exception {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new Exception("Role not found with id: " + id));

        return roleMapper.toDTO(role);
    }

    // GET ALL
    @Override
    public List<RoleDTO> getAllRoles() throws Exception {

        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(roleMapper::toDTO)
                .toList();
    }

    @Override
    public Page<RoleDTO> getRoles(Pageable pageable) throws Exception {
        return roleRepository.findAll(pageable).map(roleMapper::toDTO);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
