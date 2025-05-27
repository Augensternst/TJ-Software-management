package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
@Getter
@Setter
public class Alert {

    public enum Status {
        NORMAL(1),     // 正常
        WARNING(2),    // 警告
        CRITICAL(3);   // 严重

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + value);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @Column(nullable = false)
    private LocalDateTime alertTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "alert_description", length = 500)
    private String alertDescription;

    @Column(name = "is_confirmed", nullable = false)
    private Boolean isConfirmed = false;

    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @OneToOne
    @JoinColumn(name = "forecast_id")
    private Forecast forecast;
}