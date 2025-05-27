package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "forecast")
@Getter
@Setter
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "life_forecast", nullable = false)
    @NotNull
    private Double lifeForecast;

    @Column(name = "forecast_time", nullable = false)
    @NotNull
    private LocalDateTime forecastTime;

    @Column(name = "health_index")
    private Integer healthIndex;

    @Column(name = "damage_location")
    private String damageLocation;

    @Column(name = "image_url")
    private String imageUrl;

    // 与Model的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    // 与Component的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private Component component;

    // 与Alert的一对一关系
    @OneToOne(mappedBy = "forecast", cascade = CascadeType.ALL)
    private Alert alert;

    // 设置预测时间的自动更新
    @PrePersist
    public void prePersist() {
        if (this.forecastTime == null) {
            this.forecastTime = LocalDateTime.now();
        }
    }

    /**
     * 创建告警
     * 当预测寿命低于特定阈值时，自动创建告警
     */
    @PostPersist
    public void createAlertIfNeeded() {
        // 如果预测寿命低于阈值，自动创建告警
        if (this.lifeForecast != null && this.lifeForecast < 100) {
            Alert alert = new Alert();
            alert.setComponent(this.component);
            alert.setAlertTime(LocalDateTime.now());
            alert.setAlertDescription("设备预测寿命低于阈值：" + this.lifeForecast + " 小时");
            alert.setIsConfirmed(false);
            alert.setForecast(this);

            // 根据预测寿命设置不同的告警状态
            if (this.lifeForecast < 50) {
                alert.setStatus(Alert.Status.CRITICAL);
            } else if (this.lifeForecast < 80) {
                alert.setStatus(Alert.Status.WARNING);
            } else {
                alert.setStatus(Alert.Status.NORMAL);
            }

            this.alert = alert;
        }
    }
}