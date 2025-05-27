package com.example.software_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDTO {
    private Long id;
    private Integer modelId;
    private String modelName;
    private Integer componentId;
    private String componentName;
    private LocalDateTime predictionTime;
    private String result;
    private Double confidence;
    private String username;
}