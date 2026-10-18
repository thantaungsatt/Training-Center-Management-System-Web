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

import com.trainingcenter.dto.RoleDTO;
import com.trainingcenter.services.role.RoleService;
import com.trainingcenter.utils.DeleteErrorMessages;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) throws Exception {
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());
        Page<RoleDTO> rolePage = roleService.getRoles(pageable);
        model.addAttribute("roles", rolePage.getContent());
        model.addAttribute("currentPage", rolePage.getNumber());
        model.addAttribute("pageSize", rolePage.getSize());
        model.addAttribute("totalPages", rolePage.getTotalPages());
        model.addAttribute("totalItems", rolePage.getTotalElements());
        model.addAttribute("hasPrevious", rolePage.hasPrevious());
        model.addAttribute("hasNext", rolePage.hasNext());
        model.addAttribute("role", new RoleDTO()); // form default

        return "modules/role/list"; // single page
    }

    // CREATE FORM
    @GetMapping("/create")
    public String createForm() {
        return "redirect:/roles";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) throws Exception {
        Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by("id").ascending());
        Page<RoleDTO> rolePage = roleService.getRoles(pageable);
        model.addAttribute("roles", rolePage.getContent());
        model.addAttribute("currentPage", rolePage.getNumber());
        model.addAttribute("pageSize", rolePage.getSize());
        model.addAttribute("totalPages", rolePage.getTotalPages());
        model.addAttribute("totalItems", rolePage.getTotalElements());
        model.addAttribute("hasPrevious", rolePage.hasPrevious());
        model.addAttribute("hasNext", rolePage.hasNext());
        model.addAttribute("role", roleService.getRoleById(id));

        return "modules/role/list";
    }

    // SAVE (CREATE + UPDATE)
    @PostMapping("/save")
    public String save(@ModelAttribute("role") RoleDTO dto,
                       RedirectAttributes redirectAttributes) {

        try {
            roleService.save(dto);
            redirectAttributes.addFlashAttribute("success", "Role saved successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/roles";
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        try {
            roleService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Role deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", DeleteErrorMessages.forDeleteFailure(e, "role", "users assigned to this role"));
        }

        return "redirect:/roles";
    }
}
