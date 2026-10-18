package com.trainingcenter.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainingcenter.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByCode(String code);
}
