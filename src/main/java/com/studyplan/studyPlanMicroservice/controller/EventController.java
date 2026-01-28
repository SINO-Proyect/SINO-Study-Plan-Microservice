package com.studyplan.studyPlanMicroservice.controller;

import com.studyplan.studyPlanMicroservice.data.ApiResponse;
import com.studyplan.studyPlanMicroservice.data.EventData;
import com.studyplan.studyPlanMicroservice.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventData>> createEvent(@RequestBody EventData eventData) {
        try {
            EventData createdEvent = eventService.createEvent(eventData);
            ApiResponse<EventData> response = ApiResponse.success(createdEvent, "Event created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ApiResponse<EventData> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventData>> getEventById(@PathVariable Integer id) {
        Optional<EventData> eventData = eventService.getEventById(id);
        if (eventData.isPresent()) {
            ApiResponse<EventData> response = ApiResponse.success(eventData.get(), "Event retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ApiResponse<EventData> response = ApiResponse.error("Event not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventData>>> getAllEvents() {
        List<EventData> events = eventService.getAllEvents();
        ApiResponse<List<EventData>> response = ApiResponse.success(events, "All events retrieved successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EventData>>> getEventsByUserId(@PathVariable Integer userId) {
        List<EventData> events = eventService.getEventsByUserId(userId);
        ApiResponse<List<EventData>> response = ApiResponse.success(events, "Events retrieved successfully for user");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventData>> updateEvent(@PathVariable Integer id, @RequestBody EventData eventData) {
        try {
            Optional<EventData> updatedEvent = eventService.updateEvent(id, eventData);
            if (updatedEvent.isPresent()) {
                ApiResponse<EventData> response = ApiResponse.success(updatedEvent.get(), "Event updated successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<EventData> response = ApiResponse.error("Event not found for update");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            ApiResponse<EventData> response = ApiResponse.error(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEvent(@PathVariable Integer id) {
        boolean deleted = eventService.deleteEvent(id);
        if (deleted) {
            ApiResponse<String> response = ApiResponse.success("Event deleted successfully", "Event deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); // 204 No Content
        } else {
            ApiResponse<String> response = ApiResponse.error("Event not found for deletion");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}