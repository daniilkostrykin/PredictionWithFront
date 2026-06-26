package org.example.prediction.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.prediction.models.enums.PredictionStatus;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "predictions")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
public class Prediction extends BaseEntity{
    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_option_id", nullable = false)
    private EventOption chosenOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictionStatus status; // PLACED, WON, LOST

}
