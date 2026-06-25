package org.example.prediction.repositories;

import org.springframework.data.repository.query.Param;
import org.example.prediction.models.entities.Event;
import org.example.prediction.models.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.options WHERE e.id = :id")
    Optional<Event> findByIdWithOptions(@Param("id") Long id);

    List<Event> findAllByStatusAndClosesAtBefore(EventStatus status, Instant now);

    @Query("SELECT e FROM Event e JOIN FETCH e.options WHERE e.status = :status AND e.closesAt < :now")
    List<Event> findPendingEventsWithOptions(@Param("status") EventStatus status, @Param("now") Instant now);
}