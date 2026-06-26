package org.example.prediction.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.prediction.models.entities.Event;
import org.example.prediction.models.entities.EventOption;
import org.example.prediction.models.enums.EventStatus;
import org.example.prediction.models.entities.Prediction;
import org.example.prediction.models.entities.User;
import org.example.prediction.dto.ShowDetailedEventInfoDto;
import org.example.prediction.dto.ShowEventInfoDto;
import org.example.prediction.models.exceptions.EventNotFoundException;
import org.example.prediction.dto.form.AddEventDto;
import org.example.prediction.models.enums.PredictionStatus;
import org.example.prediction.repositories.EventOptionRepository;
import org.example.prediction.repositories.EventRepository;
import org.example.prediction.repositories.PredictionRepository;
import org.example.prediction.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final PredictionRepository predictionRepository;
    private final EventOptionRepository eventOptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "events", allEntries = true)
    public void createEvent(AddEventDto form) {

        Event event = mapper.map(form, Event.class);
        event.setStatus(EventStatus.ACTIVE);

        java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
        event.setClosesAt(form.getClosesAt().atZone(zoneId).toInstant());

        List<EventOption> options = form.getOptions().stream()
        .map(text -> {
            EventOption option = new EventOption();
            option.setText(text);
            option.setEvent(event);
            option.setIsCorrectOutcome(false);
            return option;
        }).collect(Collectors.toList());

        if (options.size() < 2) {
             throw new IllegalArgumentException("Должно быть минимум 2 заполненных варианта");
        }

        event.setOptions(options);
        eventRepository.save(event);
    }

    @Override
    @Cacheable(value = "events", key = "'all'")
    public List<ShowEventInfoDto> allEvents() {
        log.debug("Получение всех событий из БД");
        return eventRepository.findAll()
                .stream()
                .map(event -> mapper.map(event, ShowEventInfoDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Cacheable(value = "event", key = "#id", unless = "#result == null")
    public ShowDetailedEventInfoDto findEventById(Long id) {
        Event event = eventRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new EventNotFoundException("Событие с ID " + id + " не найдено"));
        return mapper.map(event, ShowDetailedEventInfoDto.class);
    }

    @Override
    public Page<ShowEventInfoDto> searchEvents(String query, Pageable pageable) {
        Page<Event> page;
        if (query != null && !query.isEmpty()) {
            page = eventRepository.findByTitleContainingIgnoreCase(query, pageable);
        } else {
            page = eventRepository.findAll(pageable);
        }
        return page.map(event -> mapper.map(event, ShowEventInfoDto.class));
    }

    public Event findEventWithStats(Long id) {
        return eventRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new EventNotFoundException("Событие с ID " + id + " не существует"));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "event", key = "#id")
    })
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Событие с ID " + id + " не найдено для удаления");
        }
        
        predictionRepository.deleteAllByEventId(id);
        
        eventRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "event", key = "#eventId")
    })
    public void finishEvent(Long eventId, Long winningOptionId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        EventOption winningOption = eventOptionRepository.findById(winningOptionId)
                .orElseThrow(() -> new IllegalArgumentException("Опция не найдена"));

        if (event.getStatus() == EventStatus.FINISHED) {
            throw new IllegalStateException("Событие уже завершено!");
        }
        if (winningOption.getEvent() == null || !winningOption.getEvent().getId().equals(event.getId())) {
            throw new IllegalArgumentException("Опция не принадлежит этому событию");
        }

        event.setStatus(EventStatus.FINISHED);
        winningOption.setIsCorrectOutcome(true);
        eventRepository.saveAndFlush(event);
        eventOptionRepository.saveAndFlush(winningOption);

        List<Prediction> allPredictions = predictionRepository.findAllByEvent(event);

        for (Prediction prediction : allPredictions) {
            if (prediction.getChosenOption().getId().equals(winningOptionId)) {
                prediction.setStatus(PredictionStatus.WON);

                User winner = prediction.getUser();
                winner.setSuccessfulPredictions(winner.getSuccessfulPredictions() + 1);
                winner.setBalance(winner.getBalance() + 1);
                userRepository.save(winner);

            } else {
                prediction.setStatus(PredictionStatus.LOST);
            }
            predictionRepository.saveAndFlush(prediction);
        }
    }

    @Override
    public boolean hasUserVoted(String username, Long eventId) {
        return predictionRepository.existsByUserUsernameAndEventId(username, eventId);
    }

    @Override
    public User getCurrentUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    @CacheEvict(value = "events", allEntries = true)
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Выполнять каждую минуту
    public void closeExpiredEvents() {
        log.debug("Запуск проверки истекших событий...");
        
        List<Event> expiredEvents = eventRepository.findAllByStatusAndClosesAtBefore(EventStatus.ACTIVE, Instant.now());
        
        if (!expiredEvents.isEmpty()) {
            for (Event event : expiredEvents) {
                log.info("Событие id={} истекло. Закрываем прием ставок.", event.getId());
                event.setStatus(EventStatus.CLOSED);
            }
            eventRepository.saveAll(expiredEvents);
        
        }
    }
}
