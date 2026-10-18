package com.trainingcenter.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.trainingcenter.entities.User;
import com.trainingcenter.repositories.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

	private final UserRepository userRepository;

	public GlobalControllerAdvice(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute("currentUser")
	public User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}

		return userRepository.findByUsername(authentication.getName()).orElse(null);
	}
}
