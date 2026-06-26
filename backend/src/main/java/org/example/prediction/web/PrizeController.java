package org.example.prediction.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.prediction.dto.form.AddPrizeDto;
import org.example.prediction.models.entities.Prize;
import org.example.prediction.models.entities.User;
import org.example.prediction.services.EventService;
import org.example.prediction.services.PrizeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/prizes")
public class PrizeController {

    private final PrizeService prizeService;
    private final EventService eventService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/buy/{id}")
    public String buyTicket(@PathVariable Long id, Principal principal, RedirectAttributes attr) {
        try {
            prizeService.buyTicket(id, principal.getName());
            attr.addFlashAttribute("successMessage", "Билет куплен! Удачи!");
        } catch (Exception e) {
            attr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/prizes";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String createPrize(@Valid @ModelAttribute("addPrizeForm") AddPrizeDto form,
                              BindingResult result,
                              RedirectAttributes attr) {
        if (result.hasErrors()) {
            attr.addFlashAttribute("org.springframework.validation.BindingResult.addPrizeForm", result);
            attr.addFlashAttribute("addPrizeForm", form);
            attr.addFlashAttribute("errorMessage", "Ошибка валидации формы");
            return "redirect:/prizes";
        }
        prizeService.createPrize(form);
        attr.addFlashAttribute("successMessage", "Розыгрыш создан!");
        return "redirect:/prizes";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/draw/{id}")
    public String performDraw(@PathVariable Long id, RedirectAttributes attr) {
        prizeService.performDraw(id);
        attr.addFlashAttribute("successMessage", "Победитель определен!");
        return "redirect:/prizes";
    }

    @GetMapping
    public String showPrizes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            Model model,
            Principal principal) {

        Pageable pageable = PageRequest.of(page, 5,
                Sort.by("status").descending().and(Sort.by("drawDate").ascending()));

        Page<Prize> prizePage = prizeService.searchPrizes(search, pageable);

        model.addAttribute("prizes", prizePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prizePage.getTotalPages());
        model.addAttribute("searchQuery", search);

        if (principal != null) {
            User currentUser = eventService.getCurrentUserByUsername(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }

        if (!model.containsAttribute("addPrizeForm")) {
            model.addAttribute("addPrizeForm", new AddPrizeDto());
        }

        return "prizes/all";
    }
}