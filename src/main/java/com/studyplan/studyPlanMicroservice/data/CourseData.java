package com.studyplan.studyPlanMicroservice.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseData {
    private Integer idCourse;

    @NotNull(message = "Study plan ID is required")
    private Integer idStudyPlan;

    @NotBlank(message = "Course code is required")
    @Size(max = 50)
    private String dscCode;

    @NotBlank(message = "Course name is required")
    @Size(max = 100)
    private String dscName;

    @NotBlank(message = "Level is required")
    private String dscLevel;

    @NotBlank(message = "Period is required")
    private String dscPeriod;

    @NotBlank(message = "Course type is required")
    private String typeCourse;

    @NotNull(message = "Credits is required")
    @Min(0)
    private Integer numCredits;

    private String description;

    private java.util.List<String> prerequisites;
    private java.util.List<String> corequisites;
}
