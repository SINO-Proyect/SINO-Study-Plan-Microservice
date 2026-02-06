package com.studyplan.studyPlanMicroservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_events")
    private Integer idEvents;

    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @Column(name = "id_course", nullable = false)
    private Integer idCourse;

    @Column(name = "dsc_title", nullable = false, length = 200)
    private String dscTitle;

    @Column(name = "dsc_description", length = 500)
    private String dscDescription;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "type_event", nullable = false, length = 50)
    private String typeEvent;

    @Column(name = "status", length = 45)
    private String status = "PENDING";
}