package org.example.prediction.services;

import java.util.List;

import org.example.prediction.dto.ShowDetailedEventInfoDto;
import org.example.prediction.dto.ShowEventInfoDto;
import org.example.prediction.dto.form.AddEventDto;
import org.example.prediction.models.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    
    void finishEvent(Long eventId, Long winningOptionId);
    void createEvent(AddEventDto form);
    List<ShowEventInfoDto> allEvents();
    ShowDetailedEventInfoDto findEventById(Long id);
    void deleteEvent(Long id);
    Page<ShowEventInfoDto> searchEvents(String query, Pageable pageable);
    org.example.prediction.models.entities.User getCurrentUserByUsername(String username);
    Event findEventWithStats(Long id);
    boolean hasUserVoted(String username, Long eventId);

}