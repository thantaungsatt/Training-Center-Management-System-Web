package com.trainingcenter.services.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trainingcenter.dto.UserDTO;

public interface UserService {
    UserDTO save(UserDTO dto) throws Exception;

    void delete(Long id) throws Exception;

    UserDTO getUserById(Long id) throws Exception;

    List<UserDTO> getAllUsers() throws Exception;

    Page<UserDTO> searchUsersByName(String name, Pageable pageable) throws Exception;
}
