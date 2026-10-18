package com.trainingcenter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.trainingcenter.security.CustomUserDetailsService;

@Controller
public class AuthController {

	@Autowired
	private CustomUserDetailsService userDetailsService;
	@GetMapping("/login")
	public String login() {
		boolean hasLoginUser= userDetailsService.getLoggedInUser()!=null;
		
		if(hasLoginUser) {
			return "redirect:/";
		}
		return "modules/auth/login";
	}
}
