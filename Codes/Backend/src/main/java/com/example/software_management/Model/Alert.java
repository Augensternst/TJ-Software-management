package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alert")
@Getter
@Setter
public class Alert {

    public enum Severity {
        LOW,       // 低
        MEDIUM,    // 中
        HIGH,      // 高
        CRITICAL   // 严重
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(nullable = false)
    private LocalDateTime alertTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity; // 枚举: LOW, MEDIUM, HIGH, CRITICAL

    @Column(length = 500)
    private String details;

    @Column(nullable = false)
    private Boolean isProcessed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
}

