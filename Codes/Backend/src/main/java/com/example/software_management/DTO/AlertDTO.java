package com.example.software_management.DTO;

import com.example.software_management.Model.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private Integer componentId;
    private String componentName;
    private LocalDateTime alertTime;
    private Alert.Severity severity;
    private String details;
    private boolean isProcessed;
    private LocalDateTime processedAt;
    private String processedBy;
    private LocalDateTime createdAt;
}