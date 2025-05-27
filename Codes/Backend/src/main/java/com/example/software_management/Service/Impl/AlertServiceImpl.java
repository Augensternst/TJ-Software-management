package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Alert;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.AlertRepository;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.UserRepository;
import com.example.software_management.Service.AlertService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AlertServiceImpl(AlertRepository alertRepository,
                            ComponentRepository componentRepository,
                            UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.componentRepository = componentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Alert> getAlerts(Integer componentId, LocalDateTime startTime, LocalDateTime endTime,
                                 Alert.Severity severity, Boolean isProcessed, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return alertRepository.findAlerts(componentId, startTime, endTime, severity, isProcessed, pageable);
    }

    @Override
    @Transactional
    public int batchProcessAlerts(List<Long> alertIds, String processedBy) {
        User user = userRepository.findByUsername(processedBy)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + processedBy));

        LocalDateTime now = LocalDateTime.now();
        return alertRepository.batchProcessAlerts(alertIds, processedBy, now);
    }

    @Override
    @Transactional
    public int batchDeleteAlerts(List<Long> alertIds) {
        // 获取存在的预警
        List<Alert> alertsToDelete = alertRepository.findAllById(alertIds);

        if (alertsToDelete.isEmpty()) {
            return 0;
        }

        // 删除预警
        alertRepository.deleteAll(alertsToDelete);
        return alertsToDelete.size();
    }

    @Override
    @Transactional
    public Alert createAlert(Integer componentId, Alert.Severity severity, String details, LocalDateTime alertTime) {
        Component component = componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found with ID: " + componentId));

        Alert alert = new Alert();
        alert.setComponent(component);
        alert.setSeverity(severity);
        alert.setDetails(details);
        alert.setAlertTime(alertTime);
        alert.setIsProcessed(false);
        alert.setCreatedAt(LocalDateTime.now());

        return alertRepository.save(alert);
    }

    @Override
    public byte[] exportAlerts(Integer componentId, LocalDateTime startTime, LocalDateTime endTime,
                               Alert.Severity severity, Boolean isProcessed, Integer limit) {
        // 获取符合条件的预警
        Page<Alert> alerts = alertRepository.findAlerts(
                componentId, startTime, endTime, severity, isProcessed,
                PageRequest.of(0, limit != null ? limit : 1000));

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预警报表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("设备名称");
            headerRow.createCell(2).setCellValue("预警时间");
            headerRow.createCell(3).setCellValue("严重性");
            headerRow.createCell(4).setCellValue("详情");
            headerRow.createCell(5).setCellValue("是否已处理");

            // 填充数据
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Alert alert : alerts.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(alert.getId());
                row.createCell(1).setCellValue(alert.getComponent().getName());
                row.createCell(2).setCellValue(alert.getAlertTime().format(formatter));
                row.createCell(3).setCellValue(alert.getSeverity().toString());
                row.createCell(4).setCellValue(alert.getDetails());
                row.createCell(5).setCellValue(alert.getIsProcessed() ? "是" : "否");
            }

            // 自动调整列宽
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}