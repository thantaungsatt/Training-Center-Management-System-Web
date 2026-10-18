package com.trainingcenter.controllers;

import java.time.LocalTime;

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

import com.trainingcenter.dto.TrainingClassDTO;
import com.trainingcenter.enums.ScheduleType;
import com.trainingcenter.services.course.CourseService;
import com.trainingcenter.services.instructor.InstructorService;
import com.trainingcenter.services.trainingClass.TrainingClassService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("classes")
public class TrainingClassController {

	private final TrainingClassService trainingClassService;
	private final CourseService courseService;
	private final InstructorService instructorService;

	public TrainingClassController(TrainingClassService trainingClassService, CourseService courseService,
			InstructorService instructorService) {
		this.trainingClassService = trainingClassService;
		this.courseService = courseService;
		this.instructorService = instructorService;
	}

	@GetMapping
	public String list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime, @RequestParam(required = false) ScheduleType schedule,
			@RequestParam(defaultValue = "0") int page, Model model) throws Exception {

		LocalTime start = (startTime != null && !startTime.isBlank()) ? LocalTime.parse(startTime) : null;

		LocalTime end = (endTime != null && !endTime.isBlank()) ? LocalTime.parse(endTime) : null;

		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("startTime").ascending());

		Page<TrainingClassDTO> result = trainingClassService.search(keyword, start, end, schedule, pageable);

		model.addAttribute("classes", result.getContent());
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("pageSize", result.getSize());
		model.addAttribute("totalPages", result.getTotalPages());
		model.addAttribute("totalItems", result.getTotalElements());
		model.addAttribute("hasPrevious", result.hasPrevious());
		model.addAttribute("hasNext", result.hasNext());

		model.addAttribute("keyword", keyword == null ? "" : keyword);
		model.addAttribute("startTime", startTime == null ? "" : startTime);
		model.addAttribute("endTime", endTime == null ? "" : endTime);
		model.addAttribute("schedule", schedule);
		model.addAttribute("scheduleTypes", ScheduleType.values());

		return "modules/classess/list";
	}
	
	@GetMapping("/create")
	public String createForm(Model model)throws Exception{
		loadFormOptions(model);
		model.addAttribute("trainingClass", new TrainingClassDTO());
		return "/modules/classess/form";
	}
	
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) throws Exception {
		loadFormOptions(model);
		model.addAttribute("trainingClass", trainingClassService.getTrainingClassById(id));
		return "modules/classess/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute("trainingClass") TrainingClassDTO dto, RedirectAttributes redirectAttributes){
		try {
			trainingClassService.save(dto);
			redirectAttributes.addFlashAttribute("success", "Class saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/classes";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes){
		try {
			trainingClassService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Class deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "class", "student enrollments in this class"));
		}
		return "redirect:/classes";
	}
	
	private void loadFormOptions(Model model) throws Exception {
		model.addAttribute("courses", courseService.getAllCourses());
		model.addAttribute("instructors", instructorService.getAllInstructors());
		model.addAttribute("scheduleType", ScheduleType.values());
	}
}
