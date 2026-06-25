package org.example.prediction.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.prediction.dto.ShowDetailedEventInfoDto;
import org.example.prediction.dto.ShowEventInfoDto;
import org.example.prediction.dto.form.AddEventDto;
import org.example.prediction.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> showAllEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search) {

        Sort sort = Sort.by("status").ascending().and(Sort.by("closesAt").ascending());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ShowEventInfoDto> eventPage = eventService.searchEvents(search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("events", eventPage.getContent());
        response.put("currentPage", eventPage.getNumber());
        response.put("totalPages", eventPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Map<String, Object>> eventDetails(@PathVariable("id") Long id, Principal principal) {
        ShowDetailedEventInfoDto event = eventService.findEventById(id);

        boolean hasVoted = false;
        if (principal != null) {
            hasVoted = eventService.hasUserVoted(principal.getName(), id);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("event", event);
        response.put("hasVoted", hasVoted);

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // НОВЫЕ МЕТОДЫ ДЛЯ АДМИНИСТРАТОРА
    // ==========================================

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody AddEventDto addEventDto) {
        // Предполагается, что в EventService у тебя есть метод addEvent(AddEventDto)
        eventService.createEvent(addEventDto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Событие успешно создано");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveEvent(
            @PathVariable("id") Long eventId,
            @RequestParam("winningOptionId") Long winningOptionId) {

        // Предполагается, что в EventService у тебя есть метод для ручного завершения события
        eventService.finishEvent(eventId, winningOptionId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Победитель выбран, итоги подведены");
        return ResponseEntity.ok(response);
    }
}