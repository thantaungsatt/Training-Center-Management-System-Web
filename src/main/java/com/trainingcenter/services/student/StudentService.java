package com.trainingcenter.services.student;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.StudentDTO;

public interface StudentService {

	StudentDTO save(StudentDTO dto) throws Exception;

	void delete(Long id) throws Exception;

	StudentDTO getStudentById(Long id) throws Exception;

	List<StudentDTO> getAllStudents() throws Exception;

	Page<StudentDTO> searchStudents(String keyword, Long trainingClassId, Pageable pageable) throws Exception;
}
