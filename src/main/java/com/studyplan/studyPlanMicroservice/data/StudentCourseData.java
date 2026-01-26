package com.studyplan.studyPlanMicroservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCourseData {
    private Integer idStudentCourse;
    private Integer idStatus;
    private String statusName;
    private Integer idUser;
    private Integer idCourse;
    private Integer numTimesTaken;
}
