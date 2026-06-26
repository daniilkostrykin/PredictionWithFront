package org.example.prediction.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.prediction.models.enums.PrizeStatus;
import java.time.Instant;

@Entity
@Table(name = "prizes")
@Getter
@Setter
@NoArgsConstructor
public class Prize extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "ticket_price")
    private int ticketPrice;

    @Column(nullable = false, name = "draw_date")
    private Instant drawDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrizeStatus status;
}
