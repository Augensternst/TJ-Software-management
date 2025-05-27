package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Model.Alert;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Repository.AlertRepository;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Service.ReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final AlertRepository alertRepository;
    private final ComponentRepository componentRepository;
    private final DataRepository dataRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportServiceImpl(
            AlertRepository alertRepository,
            ComponentRepository componentRepository,
            DataRepository dataRepository,
            JdbcTemplate jdbcTemplate) {
        this.alertRepository = alertRepository;
        this.componentRepository = componentRepository;
        this.dataRepository = dataRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Long> getTodayAlertStats(Integer userId) {
        Map<String, Long> stats = new HashMap<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1);

        // 使用JDBC模板进行统计查询，提高性能
        String sql = "SELECT a.is_confirmed, COUNT(*) as count " +
                "FROM alert a " +
                "JOIN component c ON a.component_id = c.id " +
                "WHERE c.user_id = ? " +
                "AND a.alert_time BETWEEN ? AND ? " +
                "GROUP BY a.is_confirmed";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql, userId, startOfDay, endOfDay);

        // 初始化统计数据
        long confirmedToday = 0;
        long unconfirmedToday = 0;

        // 处理查询结果
        for (Map<String, Object> row : rows) {
            boolean isConfirmed = (boolean) row.get("is_confirmed");
            long count = ((Number) row.get("count")).longValue();

            if (isConfirmed) {
                confirmedToday = count;
            } else {
                unconfirmedToday = count;
            }
        }

        stats.put("confirmedToday", confirmedToday);
        stats.put("unconfirmedToday", unconfirmedToday);

        return stats;
    }

    @Override
    public Map<String, Long> getAllAlertStats(Integer userId) {
        Map<String, Long> stats = new HashMap<>();

        // 使用JDBC模板进行统计查询，提高性能
        String sql = "SELECT a.is_confirmed, COUNT(*) as count " +
                "FROM alert a " +
                "JOIN component c ON a.component_id = c.id " +
                "WHERE c.user_id = ? " +
                "GROUP BY a.is_confirmed";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);

        // 初始化统计数据
        long confirmed = 0;
        long unconfirmed = 0;

        // 处理查询结果
        for (Map<String, Object> row : rows) {
            boolean isConfirmed = (boolean) row.get("is_confirmed");
            long count = ((Number) row.get("count")).longValue();

            if (isConfirmed) {
                confirmed = count;
            } else {
                unconfirmed = count;
            }
        }

        // 计算总警报数
        long totalAlerts = confirmed + unconfirmed;

        stats.put("totalAlerts", totalAlerts);
        stats.put("confirmed", confirmed);
        stats.put("unconfirmed", unconfirmed);

        return stats;
    }

    @Override
    public ReportDTO getWeeklyAlertStats(Integer userId) {
        // 获取本周的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);

        // 使用JDBC模板查询每天的警报统计
        String dailySql = "SELECT DATE(a.alert_time) as date, a.is_confirmed, COUNT(*) as count " +
                "FROM alert a " +
                "JOIN component c ON a.component_id = c.id " +
                "WHERE c.user_id = ? " +
                "AND a.alert_time BETWEEN ? AND ? " +
                "GROUP BY DATE(a.alert_time), a.is_confirmed " +
                "ORDER BY DATE(a.alert_time)";

        List<Map<String, Object>> dailyRows = jdbcTemplate.queryForList(
                dailySql, userId, startDateTime, endDateTime);

        // 处理每日统计数据
        Map<LocalDate, Map<Boolean, Long>> dailyStats = new HashMap<>();

        for (Map<String, Object> row : dailyRows) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            boolean isConfirmed = (boolean) row.get("is_confirmed");
            long count = ((Number) row.get("count")).longValue();

            dailyStats.computeIfAbsent(date, k -> new HashMap<>())
                    .put(isConfirmed, count);
        }

        // 构建每日统计列表
        List<ReportDTO.DailyStatDTO> dailyStatsList = new ArrayList<>();
        long totalWeekly = 0;
        long confirmedWeekly = 0;
        long unconfirmedWeekly = 0;

        // 确保每一天都有数据，包括没有警报的日期
        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
            Map<Boolean, Long> dayStat = dailyStats.getOrDefault(date, Collections.emptyMap());

            long confirmed = dayStat.getOrDefault(true, 0L);
            long unconfirmed = dayStat.getOrDefault(false, 0L);

            dailyStatsList.add(new ReportDTO.DailyStatDTO(date, confirmed, unconfirmed));

            confirmedWeekly += confirmed;
            unconfirmedWeekly += unconfirmed;
        }

        totalWeekly = confirmedWeekly + unconfirmedWeekly;

        // 构建响应对象
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setTotalWeekly(totalWeekly);
        reportDTO.setConfirmedWeekly(confirmedWeekly);
        reportDTO.setUnconfirmedWeekly(unconfirmedWeekly);
        reportDTO.setDailyStats(dailyStatsList);

        return reportDTO;
    }

    @Override
    public List<DataDTO> getDeviceAttributes(Integer deviceId) {
        // 获取设备的最新数据
        Optional<Data> latestDataOpt = dataRepository.findFirstByComponentIdOrderByTimeDesc(deviceId);

        if (!latestDataOpt.isPresent()) {
            return Collections.emptyList();
        }

        Data latestData = latestDataOpt.get();
        List<DataDTO> attributes = new ArrayList<>();

        // 添加各个属性
        if (latestData.getHptEffMod() != null) {
            attributes.add(DataDTO.createAttributeDTO("高压涡轮效率", latestData.getHptEffMod()));
        }

        if (latestData.getNf() != null) {
            attributes.add(DataDTO.createAttributeDTO("风扇转速", latestData.getNf()));
        }

        if (latestData.getSmFan() != null) {
            attributes.add(DataDTO.createAttributeDTO("风扇裕度", latestData.getSmFan()));
        }

        if (latestData.getT24() != null) {
            attributes.add(DataDTO.createAttributeDTO("风扇出口温度", latestData.getT24()));
        }

        if (latestData.getWf() != null) {
            attributes.add(DataDTO.createAttributeDTO("燃油流量", latestData.getWf()));
        }

        if (latestData.getT48() != null) {
            attributes.add(DataDTO.createAttributeDTO("HPT出口温度", latestData.getT48()));
        }

        if (latestData.getNc() != null) {
            attributes.add(DataDTO.createAttributeDTO("高压压气机转速", latestData.getNc()));
        }

        if (latestData.getSmHPC() != null) {
            attributes.add(DataDTO.createAttributeDTO("高压压气机裕度", latestData.getSmHPC()));
        }

        return attributes;
    }

    @Override
    public Resource exportDeviceAttributes(Integer deviceId) {
        try {
            // 获取设备信息
            Optional<Component> componentOpt = componentRepository.findById(deviceId);
            if (!componentOpt.isPresent()) {
                throw new RuntimeException("设备不存在");
            }
            Component component = componentOpt.get();

            // 获取设备的最新数据
            Optional<Data> latestDataOpt = dataRepository.findFirstByComponentIdOrderByTimeDesc(deviceId);
            if (!latestDataOpt.isPresent()) {
                throw new RuntimeException("设备数据不存在");
            }
            Data latestData = latestDataOpt.get();

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("设备属性");

            // 设置标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "设备ID", "设备名称", "高压涡轮效率", "风扇转速", "风扇裕度",
                    "风扇出口温度", "燃油流量", "HPT出口温度", "高压压气机转速", "高压压气机裕度"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(component.getId());
            dataRow.createCell(1).setCellValue(component.getName());

            // 设置属性值
            setCellValue(dataRow.createCell(2), latestData.getHptEffMod());
            setCellValue(dataRow.createCell(3), latestData.getNf());
            setCellValue(dataRow.createCell(4), latestData.getSmFan());
            setCellValue(dataRow.createCell(5), latestData.getT24());
            setCellValue(dataRow.createCell(6), latestData.getWf());
            setCellValue(dataRow.createCell(7), latestData.getT48());
            setCellValue(dataRow.createCell(8), latestData.getNc());
            setCellValue(dataRow.createCell(9), latestData.getSmHPC());

            // 自动调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 创建资源
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
            return resource;

        } catch (Exception e) {
            throw new RuntimeException("导出设备属性失败: " + e.getMessage(), e);
        }
    }

    /**
     * 辅助方法：设置单元格值
     * @param cell 单元格
     * @param value 值
     */
    private void setCellValue(Cell cell, Double value) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("N/A");
        }
    }
}