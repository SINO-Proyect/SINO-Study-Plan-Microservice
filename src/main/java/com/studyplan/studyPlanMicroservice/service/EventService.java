package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.EventData;
import com.studyplan.studyPlanMicroservice.domain.Event;
import com.studyplan.studyPlanMicroservice.jpa.EventRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // Import Optional
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public EventData createEvent(EventData eventData) {
        if (eventData.getDscTitle() == null || eventData.getDscTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (eventData.getIdUser() == null || eventData.getIdCourse() == null || eventData.getEventDate() == null || eventData.getTypeEvent() == null || eventData.getTypeEvent().isBlank()) {
            throw new IllegalArgumentException("User ID, Course ID, Event Date and Event Type cannot be null or blank");
        }
        Event event = new Event();
        BeanUtils.copyProperties(eventData, event);
        event = eventRepository.save(event);
        BeanUtils.copyProperties(event, eventData);
        return eventData;
    }

    public Optional<EventData> getEventById(Integer eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    EventData eventData = new EventData();
                    BeanUtils.copyProperties(event, eventData);
                    return eventData;
                });
    }

    public List<EventData> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> {
                    EventData eventData = new EventData();
                    BeanUtils.copyProperties(event, eventData);
                    return eventData;
                })
                .collect(Collectors.toList());
    }

    public List<EventData> getEventsByUserId(Integer userId) {
        return eventRepository.findByIdUser(userId).stream()
                .map(event -> {
                    EventData eventData = new EventData();
                    BeanUtils.copyProperties(event, eventData);
                    return eventData;
                })
                .collect(Collectors.toList());
    }

    public Optional<EventData> updateEvent(Integer eventId, EventData eventData) {
        // Find existing event
        Optional<Event> existingEventOptional = eventRepository.findById(eventId);

        if (existingEventOptional.isEmpty()) {
            return Optional.empty(); // Event not found
        }

        Event existingEvent = existingEventOptional.get();

        // Basic validation
        if (eventData.getDscTitle() == null || eventData.getDscTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        if (eventData.getIdUser() == null || eventData.getIdCourse() == null || eventData.getEventDate() == null || eventData.getTypeEvent() == null || eventData.getTypeEvent().isBlank()) {
            throw new IllegalArgumentException("User ID, Course ID, Event Date and Event Type cannot be null or blank");
        }

        // Update mutable properties. ID_events should not be updated from DTO
        existingEvent.setIdUser(eventData.getIdUser());
        existingEvent.setIdCourse(eventData.getIdCourse());
        existingEvent.setDscTitle(eventData.getDscTitle());
        existingEvent.setDscDescription(eventData.getDscDescription());
        existingEvent.setEventDate(eventData.getEventDate());
        existingEvent.setEventTime(eventData.getEventTime());
        existingEvent.setTypeEvent(eventData.getTypeEvent());
        existingEvent.setStatus(eventData.getStatus());

        Event updatedEvent = eventRepository.save(existingEvent);

        EventData updatedEventData = new EventData();
        BeanUtils.copyProperties(updatedEvent, updatedEventData);
        return Optional.of(updatedEventData);
    }

    public boolean deleteEvent(Integer eventId) {
        if (eventRepository.existsById(eventId)) {
            eventRepository.deleteById(eventId);
            return true;
        }
        return false;
    }
}