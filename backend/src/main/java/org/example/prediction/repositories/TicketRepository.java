package org.example.prediction.repositories;

import org.example.prediction.models.entities.Prize;
import org.example.prediction.models.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByPrize(Prize prize);
}
