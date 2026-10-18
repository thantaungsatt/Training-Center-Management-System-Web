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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.InstructorDTO;
import com.trainingcenter.enums.ActiveStatus;
import com.trainingcenter.services.instructor.InstructorService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/instructors")
public class InstructorController {

	private final InstructorService instructorService;

	public InstructorController(InstructorService instructorService) {
		this.instructorService = instructorService;
	}

	@GetMapping
	public String list(@RequestParam(name="keyword",required = false)String keyword,
	@RequestParam (name = "page", defaultValue = "0") int page,
	Model model) throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());
		Page<InstructorDTO> coursePage = instructorService.searchInstructorsByName(keyword, pageable);
		model.addAttribute("instructors", coursePage.getContent());
		model.addAttribute("currentPage", coursePage.getNumber());
		model.addAttribute("pageSize", coursePage.getSize());
		model.addAttribute("totalPages", coursePage.getTotalPages());
		model.addAttribute("totalItems", coursePage.getTotalElements());
		model.addAttribute("hasPrevious", coursePage.hasPrevious());
		model.addAttribute("hasNext", coursePage.hasNext());
		model.addAttribute("keyword", keyword==null?"":keyword);
		return "modules/instructor/list";
	}
	
	@GetMapping("/create")
	public String createForm(Model model) throws Exception {
		model.addAttribute("statuses", ActiveStatus.values());
		model.addAttribute("instructor", new InstructorDTO());
		return "modules/instructor/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("instructor") InstructorDTO dto, RedirectAttributes redirectAttributes){
		try {
			instructorService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Instructor saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/instructors";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes){
		try {
			instructorService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Instructor deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "instructor", "classes assigned to this instructor"));
		}
		return "redirect:/instructors";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		InstructorDTO dto = instructorService.getInstructorById(id);
		model.addAttribute("statuses", ActiveStatus.values());
		model.addAttribute("instructor", dto);
		return "modules/instructor/form";
	}

}
