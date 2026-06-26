package org.example.prediction.repositories;

import org.example.prediction.models.entities.EventOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {
}
