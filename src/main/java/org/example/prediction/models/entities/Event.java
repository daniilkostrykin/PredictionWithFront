package org.example.prediction.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.prediction.models.enums.EventStatus;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event extends BaseEntity{

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status; // PENDING, ACTIVE, CLOSED, FINISHED

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<EventOption> options;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

}