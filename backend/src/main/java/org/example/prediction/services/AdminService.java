package org.example.prediction.services;

import lombok.RequiredArgsConstructor;
import org.example.prediction.dto.admin.AdminDashboardViewModel;
import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.User;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PredictionRepository predictionRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Event> getPendingEvents() {
        // Ищем события, которые планировщик уже перевел в CLOSED
        return eventRepository.findPendingEventsWithOptions(EventStatus.CLOSED, Instant.now());
    }

    public AdminDashboardViewModel getDashboardStats() {
        AdminDashboardViewModel dashboardStats = new AdminDashboardViewModel();
        dashboardStats.setTotalUsers(userRepository.count());
        dashboardStats.setActiveEvents(eventRepository.findAll()
                .stream()
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .count());
        
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = startOfDay.plusSeconds(86400 - 1); // до конца дня
        
        long predictionsMadeToday = predictionRepository.findAll()
                .stream()
                .filter(prediction -> {
                    Instant predictionTime = prediction.getCreatedAt();
                    return predictionTime.isAfter(startOfDay) && predictionTime.isBefore(endOfDay);
                })
                .count();
        
        dashboardStats.setPredictionsMadeToday(predictionsMadeToday);
        
        return dashboardStats;
    }
}