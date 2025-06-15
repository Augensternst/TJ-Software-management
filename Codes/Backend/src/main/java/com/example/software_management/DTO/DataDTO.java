package com.example.software_management.DTO;

import com.example.software_management.Model.Data;
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
public class DataDTO {
    private Integer id;
    private String name;
    private String value;
    private String unit;
    private Integer health;

    // For attributes response
    public static DataDTO createAttributeDTO(String name, Double value) {
        return DataDTO.builder()
                .name(name)
                .value(value != null ? value.toString() : "N/A")
                .build();
    }

    // For metric cards response
    public static DataDTO createMetricCardDTO(String name, Double value, String unit, Integer health) {
        return DataDTO.builder()
                .name(name)
                .value(value != null ? value.toString() : "N/A")
                .unit(unit)
                .health(health)
                .build();
    }

    // Constructor from Data entity for export
    public static DataDTO fromData(Data data) {
        return DataDTO.builder()
                .id(data.getId())
                .build();
    }
}