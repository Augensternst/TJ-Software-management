package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prediction")
@Getter
@Setter
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private MModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(nullable = false)
    private LocalDateTime predictionTime = LocalDateTime.now();

    @Lob
    @Column(nullable = false)
    private byte[] inputFile;

    @Column(length = 1000)
    private String result;

    @Column
    private Double confidence; // 预测置信度

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username")
    private User createdBy;
}
