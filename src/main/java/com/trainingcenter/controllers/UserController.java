package com.trainingcenter.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.UserDTO;
import com.trainingcenter.enums.ActiveStatus;
import com.trainingcenter.services.role.RoleService;
import com.trainingcenter.services.user.UserService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final RoleService roleService;

	public UserController(UserService userService, RoleService roleService) {
		this.userService = userService;
		this.roleService = roleService;
	}

	@GetMapping
	public String list(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model) throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());
		Page<UserDTO> userPage = userService.searchUsersByName(keyword, pageable);
		model.addAttribute("users", userPage.getContent());
		model.addAttribute("currentPage", userPage.getNumber());
		model.addAttribute("pageSize", userPage.getSize());
		model.addAttribute("totalPages", userPage.getTotalPages());
		model.addAttribute("totalItems", userPage.getTotalElements());
		model.addAttribute("hasPrevious", userPage.hasPrevious());
		model.addAttribute("hasNext", userPage.hasNext());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		return "modules/user/list";
	}

	@GetMapping("/create")
	public String createForm(Model model) throws Exception {
		loadFormOptions(model);
		model.addAttribute("user", new UserDTO());
		return "modules/user/form";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		loadFormOptions(model);
		model.addAttribute("user", userService.getUserById(id));
		return "modules/user/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("user") UserDTO dto, RedirectAttributes redirectAttributes) {
		try {
			userService.save(dto);
			redirectAttributes.addFlashAttribute("success", "User saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/users";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			userService.delete(id);
			redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "user", "records that refer to this user"));
		}

		return "redirect:/users";
	}

	private void loadFormOptions(Model model) throws Exception {
		model.addAttribute("roles", roleService.getAllRoles());
		model.addAttribute("statuses", ActiveStatus.values());
	}
}
