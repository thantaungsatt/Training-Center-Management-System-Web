package com.trainingcenter.services.instructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.InstructorDTO;

public interface InstructorService {

	InstructorDTO save(InstructorDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	InstructorDTO getInstructorById(Long id) throws Exception;

	List<InstructorDTO> getAllInstructors() throws Exception;

	Page<InstructorDTO> searchInstructorsByName(String name, Pageable pageable) throws Exception;
}
