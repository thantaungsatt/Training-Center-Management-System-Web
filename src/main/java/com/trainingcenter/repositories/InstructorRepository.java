package com.trainingcenter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trainingcenter.entities.Instructor;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

	boolean existsByName(String name);
	
	Page<Instructor>findByNameContainingIgnoreCase(String name, Pageable pageable);
}
