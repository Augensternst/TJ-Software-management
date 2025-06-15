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
     * 当预测寿命低于特定阈值或健康指数低于阈值时，自动创建告警
     */
    @PostPersist
    public void createAlertIfNeeded() {
        // 如果healthIndex为空或大于等于80，不创建告警
        if (healthIndex == null || healthIndex >= 80) {
            return;
        }

        // 创建新的告警对象
        Alert newAlert = new Alert();

        // 设置告警与当前预测的关联
        newAlert.setForecast(this);

        // 设置告警与组件的关联
        newAlert.setComponent(this.component);

        // 设置告警描述与损伤位置一致
        newAlert.setAlertDescription(this.damageLocation);

        // 设置告警时间为当前时间
        newAlert.setAlertTime(LocalDateTime.now());

        // 设置未确认状态
        newAlert.setIsConfirmed(false);

        // 根据健康指数设置告警状态
        if (healthIndex < 50) {
            // 紧急告警
            newAlert.setStatus(Alert.Status.CRITICAL);
        } else if (healthIndex < 65) {
            // 严重告警
            newAlert.setStatus(Alert.Status.WARNING);
        } else {
            // 一般告警
            newAlert.setStatus(Alert.Status.NORMAL);
        }

        // 双向关联设置
        this.alert = newAlert;

        // 更新组件状态
        updateComponentStatus();
    }

    /**
     * 更新组件状态
     * 根据健康指数设置组件的状态级别
     * 状态1-3逐渐严重:
     * 1 - 正常状态 (健康指数 >= 80)
     * 2 - 中等严重 (50 <= 健康指数 < 80)
     * 3 - 最严重状态 (健康指数 < 50)
     */
    public void updateComponentStatus() {
        if (component == null || healthIndex == null) {
            return;
        }

        int newStatus;

        if (healthIndex < 50) {
            // 最严重状态
            newStatus = 3;
        } else if (healthIndex < 80) {
            // 中等严重状态
            newStatus = 2;
        } else {
            // 正常状态
            newStatus = 1;
        }

        // 设置组件状态
        Integer oldStatus = component.getStatus();

        // 仅当状态发生变化时才更新
        if (oldStatus == null || !oldStatus.equals(newStatus)) {
            component.setStatus(newStatus);

            // 如果状态不为1(正常)，并且之前没有设置警告时间，则设置警告时间
            if (newStatus != 1 && component.getWarningTime() == null) {
                component.setWarningTime(LocalDateTime.now());
            }
        }
    }


}