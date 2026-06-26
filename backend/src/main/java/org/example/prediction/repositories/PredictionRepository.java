package org.example.prediction.repositories;

import java.util.List;

import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.Prediction;
import org.example.prediction.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    boolean existsByUserAndEvent(User user, Event event);
    List<Prediction> findByUserOrderByCreatedAtDesc(User user);
    List<Prediction> findAllByEvent(Event event);
    void deleteAllByEventId(Long eventId);
    boolean existsByUserUsernameAndEventId(String username, Long id);
    int countByUser(User user);
}