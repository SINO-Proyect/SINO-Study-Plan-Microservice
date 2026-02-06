package com.studyplan.studyPlanMicroservice.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventData {

    private Integer idEvents;

    @NotNull(message = "User id is required")
    @JsonProperty("id_user")
    private Integer idUser;

    @NotNull(message = "Course id is required")
    @JsonProperty("id_course")
    private Integer idCourse;

    @NotBlank(message = "Title is required")
    @JsonProperty("dsc_title")
    private String dscTitle;

    @JsonProperty("dsc_description")
    private String dscDescription;

    @NotNull(message = "Event date is required")
    @JsonProperty("event_date")
    private LocalDate eventDate;

    @JsonProperty("event_time")
    private LocalTime eventTime;

    @NotBlank(message = "Event type is required")
    @JsonProperty("type_event")
    private String typeEvent;

    @JsonProperty("status")
    private String status;
}
