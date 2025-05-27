package com.example.software_management.DTO;

import com.example.software_management.Model.Forecast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDTO {
    private Integer id;
    private Integer modelId;
    private String modelName;
    private Integer deviceId;
    private String deviceName;
    private String imageUrl;
    private String damageLocation;
    private Double lifespan;
    private Integer healthIndex;
    private LocalDateTime forecastTime;

    // Constructor from Forecast entity
    public ForecastDTO(Forecast forecast) {
        this.id = forecast.getId();
        this.lifespan = forecast.getLifeForecast();
        this.healthIndex = forecast.getHealthIndex();
        this.damageLocation = forecast.getDamageLocation();
        this.imageUrl = forecast.getImageUrl();
        this.forecastTime = forecast.getForecastTime();

        if (forecast.getModel() != null) {
            this.modelId = forecast.getModel().getId();
            this.modelName = forecast.getModel().getName();
        }

        if (forecast.getComponent() != null) {
            this.deviceId = forecast.getComponent().getId();
            this.deviceName = forecast.getComponent().getName();
        }
    }

    // Create a simulation result DTO
    public static ForecastDTO createSimulationResult(Forecast forecast) {
        return ForecastDTO.builder()
                .imageUrl(forecast.getImageUrl())
                .damageLocation(forecast.getDamageLocation())
                .lifespan(forecast.getLifeForecast())
                .healthIndex(forecast.getHealthIndex())
                .build();
    }
}