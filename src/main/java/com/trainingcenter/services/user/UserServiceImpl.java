package com.trainingcenter.services.user;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trainingcenter.dto.UserDTO;
import com.trainingcenter.entities.Role;
import com.trainingcenter.entities.User;
import com.trainingcenter.mapper.UserMapper;
import com.trainingcenter.repositories.RoleRepository;
import com.trainingcenter.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDTO save(UserDTO dto) throws Exception {
		Role role = roleRepository.findById(dto.getRoleId())
				.orElseThrow(() -> new Exception("Role not found with id: " + dto.getRoleId()));
		User currentUser = getCurrentUser();

		User user;

		if (dto.getId() != null) {
			user = userRepository.findById(dto.getId())
					.orElseThrow(() -> new Exception("User not found with id: " + dto.getId()));

			userMapper.updateEntity(user, dto, role);
			if (currentUser != null) {
				user.setUpdatedBy(currentUser);
			}
		} else {
			if (userRepository.existsByUsername(dto.getUsername())) {
				throw new Exception("Username already exists: " + dto.getUsername());
			}

			user = userMapper.toEntity(dto, role, passwordEncoder);
			if (currentUser != null) {
				user.setCreatedBy(currentUser);
				user.setUpdatedBy(currentUser);
			}
		}

		userRepository.save(user);

		return userMapper.toDTO(user);
	}

	@Override
	public void delete(Long id) throws Exception {
		User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found with id: " + id));

		userRepository.delete(user);
	}

	@Override
	public UserDTO getUserById(Long id) throws Exception {
		User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found with id: " + id));

		return userMapper.toDTO(user);
	}

	@Override
	public List<UserDTO> getAllUsers() throws Exception {
		List<User> users = userRepository.findAll();

		return users.stream().map(userMapper::toDTO).toList();
	}

	@Override
	public Page<UserDTO> searchUsersByName(String name, Pageable pageable) throws Exception {
		if (Objects.isNull(name) || name.isBlank()) {
			return userRepository.findAll(pageable).map(userMapper::toDTO);
		}

		return userRepository.findByNameContainingIgnoreCase(name.trim(), pageable).map(userMapper::toDTO);
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
