package com.studyplan.studyPlanMicroservice.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanData {
    private Integer idStudyPlan;

    @NotNull(message = "University ID is required")
    private Integer idUniversity;

    @NotBlank(message = "Plan name is required")
    private String dscName;

    @NotBlank(message = "Career name is required")
    private String dscCareer;

    @NotBlank(message = "Period type is required")
    private String typePeriod;

    @NotBlank(message = "Year level is required")
    private String yearLevel;

    @NotNull(message = "Status is required")
    private Boolean status;
}

