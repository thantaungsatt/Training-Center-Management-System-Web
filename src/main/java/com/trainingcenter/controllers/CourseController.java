package com.trainingcenter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trainingcenter.dto.CourseDTO;
import com.trainingcenter.services.course.CourseService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/courses")
public class CourseController {

	private final CourseService courseService;
	
	public CourseController(CourseService courseService) {
		this.courseService=courseService;
	}
	
	@GetMapping
	public String list(@RequestParam(name="keyword",required = false)String keyword,
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model)throws Exception {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());
		Page<CourseDTO> coursePage = courseService.searchCourseByCourseName(keyword, pageable);
		model.addAttribute("courses", coursePage.getContent());
		model.addAttribute("currentPage", coursePage.getNumber());
		model.addAttribute("pageSize", coursePage.getSize());
		model.addAttribute("totalPages", coursePage.getTotalPages());
		model.addAttribute("totalItems", coursePage.getTotalElements());
		model.addAttribute("hasPrevious", coursePage.hasPrevious());
		model.addAttribute("hasNext", coursePage.hasNext());
		model.addAttribute("keyword", keyword==null?"":keyword);
		return "modules/course/list";
	}
	
	@GetMapping("/create")
	public String createForm(Model model)throws Exception{
		model.addAttribute("course", new CourseDTO());
		return "modules/course/form";
	}
	
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model)throws Exception{
		model.addAttribute("course", courseService.getCourseById(id));
		return "modules/course/form";
	}
	
	@PostMapping("/save")
	public String save(@ModelAttribute("course") CourseDTO dto, RedirectAttributes redirectAttributes) {
		
		try {
			courseService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Course saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/courses";
	}
	
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			courseService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "course", "classes or certificates that use this course"));
		}
		return "redirect:/courses";
	}
}
