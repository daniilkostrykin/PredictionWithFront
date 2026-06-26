package org.example.prediction.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.example.prediction.dto.admin.AdminDashboardViewModel;
import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.User;
import org.example.prediction.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        List<User> allUsers = adminService.getAllUsers();
        List<Event> pendingEvents = adminService.getPendingEvents();
        AdminDashboardViewModel dashboardStats = adminService.getDashboardStats();
        pendingEvents.forEach(e -> {
            System.out.println("Событие: " + e.getTitle() + ", Опций: " + (e.getOptions() != null ? e.getOptions().size() : "NULL"));
        });
        Map<String, Object> response = new HashMap<>();
        response.put("users", allUsers);
        response.put("pendingEvents", pendingEvents);
        response.put("dashboardStats", dashboardStats);

        return ResponseEntity.ok(response);
    }
}