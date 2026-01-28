package com.studyplan.studyPlanMicroservice.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventData {

    private Integer idEvents;

    @NotNull(message = "User id is required")
    private Integer idUser;

    @NotNull(message = "Course id is required")
    private Integer idCourse;

    @NotBlank(message = "Title is required")
    private String dscTitle;

    private String dscDescription;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime eventTime;

    @NotBlank(message = "Event type is required")
    private String typeEvent;

    @NotBlank(message = "Status is required")
    private String status;
}
