package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.AlertDTO;
import com.example.software_management.Model.Alert;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.AlertRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Repository.UserRepository;
import com.example.software_management.Service.AlertService;
import com.example.software_management.Redis.RedisUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final DataRepository dataRepository;
    private final RedisUtil redisUtil;

    @Autowired
    public AlertServiceImpl(
            AlertRepository alertRepository,
            UserRepository userRepository,
            DataRepository dataRepository,
            RedisUtil redisUtil) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.dataRepository = dataRepository;
        this.redisUtil = redisUtil;
    }

    // 获取用户未处理的警报

    @Override
    public Page<AlertDTO> getUnconfirmedAlerts(
            Integer userId,
            String deviceName,
            String startTime,
            String endTime,
            int page,
            int pageSize) {

        // 处理可选的时间参数
        LocalDateTime startDateTime = null;
        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
        }

        LocalDateTime endDateTime = null;
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 查询未确认的警报
        Page<Alert> alertPage = alertRepository.findUnconfirmedAlerts(
                userId, deviceName, startDateTime, endDateTime, pageable);

        // 转换为DTO
        List<AlertDTO> alertDTOs = alertPage.getContent().stream()
                .map(alert -> {
                    AlertDTO dto = new AlertDTO(alert);

                    // 获取最新的设备数据以便导出
                    Optional<Data> latestData = dataRepository.findFirstByComponentIdOrderByTimeDesc(alert.getComponent().getId());
                    latestData.ifPresent(data -> {
                        dto.setHptEffMod(data.getHptEffMod());
                        dto.setNf(data.getNf());
                        dto.setSmFan(data.getSmFan());
                        dto.setT24(data.getT24());
                        dto.setWf(data.getWf());
                        dto.setT48(data.getT48());
                        dto.setNc(data.getNc());
                        dto.setSmHPC(data.getSmHPC());
                    });

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(alertDTOs, pageable, alertPage.getTotalElements());
    }


   @Override
   public Page<AlertDTO> getUnconfirmedAlerts(
           Integer userId,
           String deviceName,
           String startTime,
           String endTime,
           int page,
           int pageSize) {

       // 构建缓存键，包含所有查询参数以确保唯一性
       String cacheKey = "alerts:unconfirmed:" + userId + ":"
               + (deviceName != null ? deviceName : "null") + ":"
               + (startTime != null ? startTime : "null") + ":"
               + (endTime != null ? endTime : "null") + ":"
               + page + ":" + pageSize;

       // 尝试从缓存获取
       Object cachedResult = redisUtil.get(cacheKey);
       if (cachedResult != null) {
           return (Page<AlertDTO>) cachedResult;
       }

       // 处理可选的时间参数
       LocalDateTime startDateTime = null;
       if (startTime != null && !startTime.isEmpty()) {
           startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
       }

       LocalDateTime endDateTime = null;
       if (endTime != null && !endTime.isEmpty()) {
           endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
       }

       Pageable pageable = PageRequest.of(page - 1, pageSize);

       // 查询未确认的警报
       Page<Alert> alertPage = alertRepository.findUnconfirmedAlerts(
               userId, deviceName, startDateTime, endDateTime, pageable);

       // 转换为DTO
       List<AlertDTO> alertDTOs = alertPage.getContent().stream()
               .map(alert -> {
                   AlertDTO dto = new AlertDTO(alert);

                   // 获取最新的设备数据以便导出
                   Optional<Data> latestData = dataRepository.findFirstByComponentIdOrderByTimeDesc(alert.getComponent().getId());
                   latestData.ifPresent(data -> {
                       dto.setHptEffMod(data.getHptEffMod());
                       dto.setNf(data.getNf());
                       dto.setSmFan(data.getSmFan());
                       dto.setT24(data.getT24());
                       dto.setWf(data.getWf());
                       dto.setT48(data.getT48());
                       dto.setNc(data.getNc());
                       dto.setSmHPC(data.getSmHPC());
                   });

                   return dto;
               })
               .collect(Collectors.toList());

       PageImpl<AlertDTO> result = new PageImpl<>(alertDTOs, pageable, alertPage.getTotalElements());

       // 将结果放入缓存，设置2分钟过期（警报数据较为实时，缓存时间不宜过长）
       redisUtil.set(cacheKey, result, 120);

       return result;
   }

    @Override
    @Transactional
    @CacheEvict(value = {"alertStatusSummary"}, allEntries = true)
    public Map<String, Object> confirmAlerts(List<Integer> alertIds, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        List<Integer> failedIds = new ArrayList<>();

        // 确保用户存在
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            result.put("failedIds", alertIds);
            return result;
        }

        // 批量确认警报
        try {
            int updatedCount = alertRepository.confirmAlerts(
                    alertIds, userId, LocalDateTime.now());

            // 查找未成功更新的ID
            if (updatedCount < alertIds.size()) {
                List<Alert> confirmedAlerts = alertRepository.findAllById(alertIds);
                Set<Integer> confirmedIds = confirmedAlerts.stream()
                        .filter(Alert::getIsConfirmed)
                        .map(Alert::getId)
                        .collect(Collectors.toSet());

                failedIds = alertIds.stream()
                        .filter(id -> !confirmedIds.contains(id))
                        .collect(Collectors.toList());
            }

            result.put("success", true);
            result.put("message", "已成功确认 " + updatedCount + " 条警报");
            result.put("failedIds", failedIds);

            // 清除相关缓存
            clearAlertCaches(userId);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认警报失败: " + e.getMessage());
            result.put("failedIds", alertIds);
        }

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"alertStatusSummary"}, allEntries = true)
    public Map<String, Object> deleteAlerts(List<Integer> alertIds) {

        Map<String, Object> result = new HashMap<>();
        List<Integer> failedIds = new ArrayList<>();

        try {
            // 在删除前获取这些警报，以便我们知道哪些用户的缓存需要清除
            List<Alert> alertsToDelete = alertRepository.findAllById(alertIds);
            Set<Integer> userIds = alertsToDelete.stream()
                    .map(alert -> alert.getComponent().getUser().getId())
                    .collect(Collectors.toSet());

            // 直接尝试删除，捕获不存在的ID
            int deletedCount = alertRepository.deleteByIdIn(alertIds);

            // 检查哪些ID未被删除
            List<Alert> remainingAlerts = alertRepository.findAllById(alertIds);
            failedIds = remainingAlerts.stream()
                    .map(Alert::getId)
                    .collect(Collectors.toList());

            result.put("success", true);
            result.put("message", "已成功删除 " + (alertIds.size() - failedIds.size()) + " 条警报");
            result.put("deletedCount", deletedCount);
            result.put("failedIds", failedIds);

            // 清除相关用户的所有警报相关缓存
            for (Integer userId : userIds) {
                clearAlertCaches(userId);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除警报失败: " + e.getMessage());
            result.put("failedIds", alertIds);
        }

        return result;
    }

    @Override
    public Resource exportAlertsToXLSX(Integer userId) {
        try {
            // 获取用户所有未确认的警报
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Alert> alertPage = alertRepository.findUnconfirmedAlerts(
                    userId, null, null, null, pageable);

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("未确认警报");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "设备ID", "设备名称", "报警时间", "设备状态", "报警描述",
                    "高压涡轮效率", "风扇转速", "风扇裕度", "风扇出口温度",
                    "燃油流量", "HPT出口温度", "高压压气机转速", "高压压气机裕度",
                    "是否确认"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // 填充数据行
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Alert alert : alertPage.getContent()) {
                Row row = sheet.createRow(rowNum++);
                Component component = alert.getComponent();

                // 基本警报信息
                row.createCell(0).setCellValue(component.getId());
                row.createCell(1).setCellValue(component.getName());
                row.createCell(2).setCellValue(alert.getAlertTime().format(formatter));
                row.createCell(3).setCellValue(alert.getStatus().getValue());
                row.createCell(4).setCellValue(alert.getAlertDescription());

                // 获取最新的设备数据
                Optional<Data> latestData = dataRepository.findFirstByComponentIdOrderByTimeDesc(component.getId());
                if (latestData.isPresent()) {
                    Data data = latestData.get();
                    setCellValue(row.createCell(5), data.getHptEffMod());
                    setCellValue(row.createCell(6), data.getNf());
                    setCellValue(row.createCell(7), data.getSmFan());
                    setCellValue(row.createCell(8), data.getT24());
                    setCellValue(row.createCell(9), data.getWf());
                    setCellValue(row.createCell(10), data.getT48());
                    setCellValue(row.createCell(11), data.getNc());
                    setCellValue(row.createCell(12), data.getSmHPC());
                }

                row.createCell(13).setCellValue(alert.getIsConfirmed() ? "是" : "否");
            }

            // 自动调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 创建资源
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("导出警报失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "alertStatusSummary", key = "#userId")
    public List<Map<String, Object>> getAlertStatusSummary(Integer userId) {
        List<Object[]> statusSummary = alertRepository.getAlertStatusSummary(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : statusSummary) {
            Map<String, Object> item = new HashMap<>();
            // 第一列是Status枚举，需要获取其数值
            if (row[0] instanceof Alert.Status) {
                item.put("status", ((Alert.Status) row[0]).getValue());
            } else {
                item.put("status", row[0]);
            }
            item.put("count", row[1]);
            result.add(item);
        }

        return result;
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

    /**
     * 清除用户相关的警报缓存
     * @param userId 用户ID
     */
    private void clearAlertCaches(Integer userId) {
        // 删除最常用的查询组合
        redisUtil.del("alerts:unconfirmed:" + userId + ":null:null:null:1:10");

        // 删除状态摘要缓存
        redisUtil.del("alertStatusSummary::" + userId);

        // 如果在Controller层有缓存，也需要清除
        redisUtil.del("api:alerts:count:" + userId);
        redisUtil.del("api:alerts:summary:" + userId);
    }
}