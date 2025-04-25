package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.DTO.ReportDTO;
import com.example.software_management.Model.Data;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DataServiceImpl implements DataService {

    private final ComponentRepository componentRepository;
    private final DataRepository dataRepository;

    @Autowired
    public DataServiceImpl(ComponentRepository componentRepository, DataRepository dataRepository) {
        this.componentRepository = componentRepository;
        this.dataRepository = dataRepository;
    }

    @Override
    public List<Double> getDeviceHealthData(Integer deviceId) {
        // 获取日期到健康数据的映射
        Map<LocalDate, Double> dateHealthMap = getComponentHealthByDate(deviceId);

        // 确保按日期顺序返回7天数据
        return getOrderedValuesByDate(dateHealthMap);
    }

    @Override
    public ReportDTO getDeviceEnergyData(Integer deviceId) {
        // 获取日期到能耗数据的映射
        Map<LocalDate, Double> dateEnergyMap = getComponentEnergyByDate(deviceId);

        // 确保按日期顺序返回7天数据
        List<Double> orderedEnergyValues = getOrderedValuesByDate(dateEnergyMap);

        // 获取当日能耗成本
        Double energyCost = componentRepository.getComponentEnergyCost(deviceId);
        if (energyCost == null) {
            energyCost = 0.0;
        }

        // 构建响应对象
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setValues(orderedEnergyValues);
        reportDTO.setEnergyCost(energyCost);

        return reportDTO;
    }

    /**
     * 获取组件健康数据按日期映射
     * @param componentId 组件ID
     * @return 日期到健康数据的映射
     */
    private Map<LocalDate, Double> getComponentHealthByDate(Integer componentId) {
        // 从数据库获取原始数据
        List<Object[]> rawData = componentRepository.getComponentHealthTrendWithDates(componentId);

        // 构建日期到健康数据的映射
        Map<LocalDate, Double> dateValueMap = new HashMap<>();
        for (Object[] row : rawData) {
            if (row.length >= 2 && row[0] != null && row[1] != null) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                Double value = ((Number) row[1]).doubleValue();
                dateValueMap.put(date, value);
            }
        }

        return dateValueMap;
    }

    /**
     * 获取组件能耗数据按日期映射
     * @param componentId 组件ID
     * @return 日期到能耗数据的映射
     */
    private Map<LocalDate, Double> getComponentEnergyByDate(Integer componentId) {
        // 从数据库获取原始数据
        List<Object[]> rawData = componentRepository.getComponentEnergyTrendWithDates(componentId);

        // 构建日期到能耗数据的映射
        Map<LocalDate, Double> dateValueMap = new HashMap<>();
        for (Object[] row : rawData) {
            if (row.length >= 2 && row[0] != null && row[1] != null) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                Double value = ((Number) row[1]).doubleValue();
                dateValueMap.put(date, value);
            }
        }

        return dateValueMap;
    }

    /**
     * 根据日期映射获取有序的值列表
     * @param dateValueMap 日期到值的映射
     * @return 按日期顺序的值列表
     */
    private List<Double> getOrderedValuesByDate(Map<LocalDate, Double> dateValueMap) {
        // 获取过去7天的日期列表
        List<LocalDate> last7Days = getLast7Days();

        // 为每一天创建对应的值，没有数据的用0填充
        List<Double> orderedValues = new ArrayList<>();

        for (LocalDate date : last7Days) {
            Double value = dateValueMap.getOrDefault(date, 0.0);
            orderedValues.add(value);
        }

        return orderedValues;
    }

    /**
     * 获取过去7天的日期列表，按从早到晚排序
     */
    private List<LocalDate> getLast7Days() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }

        return dates;
    }

    @Override
    public ReportDTO getDeviceMetricCards(Integer deviceId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 获取最新的设备数据
        Page<Data> dataPage = dataRepository.getComponentMetricData(deviceId, pageable);

        List<DataDTO> metricCards = new ArrayList<>();
        int totalPages = 0;

        if (!dataPage.isEmpty()) {
            Data latestData = dataPage.getContent().get(0);

            // 构建指标卡片数据
            // 1. 高压涡轮效率
            if (latestData.getHptEffMod() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("HPT_eff_mod");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压涡轮效率",
                        roundToTwoDecimals(latestData.getHptEffMod()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 2. 风扇转速
            if (latestData.getNf() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("Nf");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇转速",
                        roundToTwoDecimals(latestData.getNf()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 3. 风扇裕度
            if (latestData.getSmFan() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("SmFan");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇裕度",
                        roundToTwoDecimals(latestData.getSmFan()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 4. 风扇出口温度
            if (latestData.getT24() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("T24");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "风扇出口温度",
                        roundToTwoDecimals(latestData.getT24()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 5. 燃油流量
            if (latestData.getWf() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("Wf");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "燃油流量",
                        roundToTwoDecimals(latestData.getWf()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 6. HPT出口温度
            if (latestData.getT48() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("T48");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "HPT出口温度",
                        roundToTwoDecimals(latestData.getT48()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 7. 高压压气机转速
            if (latestData.getNc() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("Nc");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机转速",
                        roundToTwoDecimals(latestData.getNc()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 8. 高压压气机裕度
            if (latestData.getSmHPC() != null) {
                Map<String, Object> healthData = latestData.calculateHealthIndex("SmHPC");
                metricCards.add(DataDTO.createMetricCardDTO(
                        "高压压气机裕度",
                        roundToTwoDecimals(latestData.getSmHPC()),
                        (String) healthData.get("unit"),
                        ((Double) healthData.get("healthIndex")).intValue()
                ));
            }

            // 计算总页数 - 假设每个参数是一个卡片
            totalPages = (int) Math.ceil(8.0 / pageSize);
        }

        // 根据分页参数裁剪结果
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, metricCards.size());

        // 确保fromIndex不超出范围
        if (fromIndex < metricCards.size()) {
            metricCards = metricCards.subList(fromIndex, toIndex);
        } else {
            metricCards = new ArrayList<>();
        }

        // 构建响应对象
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setItems(metricCards);
        reportDTO.setTotalPages(totalPages);

        return reportDTO;
    }

    /**
     * 将Double值四舍五入到两位小数
     * @param value 原始数值
     * @return 保留两位小数的数值
     */
    private Double roundToTwoDecimals(Double value) {
        if (value == null) {
            return null;
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}