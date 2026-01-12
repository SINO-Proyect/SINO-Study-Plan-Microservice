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
public class UniversityData {
    private Integer idUniversity;

    @NotBlank(message = "University name is required")
    private String dscName;

    @NotBlank(message = "Country is required")
    private String dscCountry;

    @NotNull(message = "Status is required")
    private Boolean status;
}
