package com.example.software_management.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "life_forecast", nullable = false)
    @NotNull
    private Integer lifeForecast = -1;

    @Column(name = "location", length = 50, nullable = false)
    @Size(max = 50)
    @NotNull
    private String location;

    @Column(name = "updated_time", nullable = false)
    @NotNull
    private LocalDateTime updatedTime;

    @Lob
    @Column(name = "pic")
    private byte[] pic;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private MModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username", nullable = false)
    @NotNull
    private User user;

    // 与DData的一对多关系
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private List<DData> data;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Prediction> predictions;

    // 设置更新时间的自动更新
    @PrePersist
    public void prePersist() {
        if (this.updatedTime == null) {
            this.updatedTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }


    // alert 一对多关系
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Alert> alerts;
}