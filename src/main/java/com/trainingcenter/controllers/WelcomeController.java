package com.trainingcenter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

	/** Keeps the application root aligned with the primary dashboard route. */
	@GetMapping("/")
	public String home() {
		return "redirect:/dashboard";
	}
}
