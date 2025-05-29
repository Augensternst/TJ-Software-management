package com.example.software_management.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "component")
@Getter
@Setter
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false)
    @Size(max = 50)
    @NotNull
    private String name;

    @Column(name = "status")
    private Integer status;

    @Column(name = "pic")
    private String pic;

    @Column(name="warning_time")
    private LocalDateTime warningTime;


    // 与Data的一对多关系
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private List<Data> dataList;

    // 与User的多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    // 与Alert的一对多关系
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Alert> alerts;

    // 与Forecast的一对多关系
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Forecast> forecasts;

    // 获取最新的健康指数
    @Transient
    public Double getHealthIndex() {
        if (forecasts == null || forecasts.isEmpty()) {
            return null;
        }

        // 从forecast中找到最新的forecast对应的healthIndex
        return forecasts.stream()
                .max(Comparator.comparing(Forecast::getForecastTime))
                .filter(forecast -> forecast.getHealthIndex() != null)
                .map(forecast -> forecast.getHealthIndex().doubleValue())
                .orElse(null);
    }

    // 获取最新的能耗数据
    @Transient
    public Double getEnergyConsumption() {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        // 从data表中找到最新的wf作为return
        return dataList.stream()
                .max(Comparator.comparing(Data::getTime))
                .map(Data::getWf)
                .orElse(null);
    }

    // 更新状态时自动设置警告时间
    @PreUpdate
    public void preUpdate() {
        if (this.status != null && this.status != 1 && this.warningTime == null) {
            this.warningTime = LocalDateTime.now();
        }
    }
}