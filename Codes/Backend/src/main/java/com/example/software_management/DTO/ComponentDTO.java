package com.example.software_management.DTO;

import com.example.software_management.Model.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentDTO {
    private Integer deviceId;
    private String name;
    private String picture;
    private Integer status;
    private LocalDateTime warningTime;

    // Additional fields for detailed views
    private Integer userId;
    private String username;

    // Constructor from Component entity
    public ComponentDTO(Component component) {
        this.deviceId = component.getId();
        this.name = component.getName();
        this.picture = component.getPic();
        this.status = component.getStatus();
        this.warningTime = component.getWarningTime();

        if (component.getUser() != null) {
            this.userId = component.getUser().getId();
            this.username = component.getUser().getUsername();
        }
    }

    // Simplified constructor for listing
    public static ComponentDTO createSimplifiedDTO(Component component) {
        return ComponentDTO.builder()
                .deviceId(component.getId())
                .name(component.getName())
                .picture(component.getPic())
                .status(component.getStatus())
                .build();
    }

    // Constructor for defective devices
    public static ComponentDTO createDefectiveDTO(Component component) {
        return ComponentDTO.builder()
                .deviceId(component.getId())
                .name(component.getName())
                .status(component.getStatus())
                .warningTime(component.getWarningTime())
                .build();
    }
}