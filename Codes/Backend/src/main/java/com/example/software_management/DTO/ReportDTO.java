package com.example.software_management.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    // For status summary
    private List<Map<String, Object>> statusSummary;

    // For today's alert stats
    private long unconfirmedToday;
    private long confirmedToday;

    // For all alert stats
    private long totalAlerts;
    private long unconfirmed;
    private long confirmed;

    // For weekly alert stats
    private long totalWeekly;
    private long confirmedWeekly;
    private long unconfirmedWeekly;
    private List<DailyStatDTO> dailyStats;

    // Nested class for daily stats
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStatDTO {
        private LocalDate date;
        private long confirmed;
        private long unconfirmed;
    }

    // For health and energy data
    private List<Double> values;
    private Double energyCost;

    // For metric cards
    private List<DataDTO> items;
    private int totalPages;
}