package com.example.software_management.Repository;

import com.example.software_management.Model.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DataRepository extends JpaRepository<Data, Integer> {

    /**
     * 根据组件ID查找最新的数据
     * @param componentId 组件ID
     * @return 最新的数据记录
     */
    Optional<Data> findFirstByComponentIdOrderByTimeDesc(Integer componentId);

    /**
     * 根据组件ID查找最近7天的数据
     * @param componentId 组件ID
     * @param startDate 开始日期时间
     * @return 数据列表
     */
    List<Data> findByComponentIdAndTimeGreaterThanEqualOrderByTimeAsc(
            Integer componentId, LocalDateTime startDate);

    /**
     * 获取组件的健康指数数据（最近7天）
     * @param componentId 组件ID
     * @return 每日健康指数
     */
    @Query("SELECT FUNCTION('DATE', d.time) as date, AVG(d.hptEffMod + d.nf/30 + d.smFan + (100-d.t24) + (100-d.wf*3) + (100-d.t48/10) + d.nc/30 + d.smHPC)/8 as health " +
            "FROM Data d WHERE d.component.id = :componentId " +
            "AND d.time >= :startDate " +
            "GROUP BY FUNCTION('DATE', d.time) " +
            "ORDER BY FUNCTION('DATE', d.time)")
    List<Object[]> getHealthIndexTrend(
            @Param("componentId") Integer componentId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * 获取组件的能耗数据（最近7天）
     * @param componentId 组件ID
     * @return 每日能耗数据
     */
    @Query("SELECT FUNCTION('DATE', d.time) as date, AVG(d.wf) as energy " +
            "FROM Data d WHERE d.component.id = :componentId " +
            "AND d.time >= :startDate " +
            "GROUP BY FUNCTION('DATE', d.time) " +
            "ORDER BY FUNCTION('DATE', d.time)")
    List<Object[]> getEnergyConsumptionTrend(
            @Param("componentId") Integer componentId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * 获取组件当前的能耗成本
     * @param componentId 组件ID
     * @return 能耗成本
     */
    @Query("SELECT AVG(d.wf) * 0.75 FROM Data d " +
            "WHERE d.component.id = :componentId " +
            "AND FUNCTION('DATE', d.time) = CURRENT_DATE")
    Double getCurrentEnergyCost(@Param("componentId") Integer componentId);

    /**
     * 获取组件的指标卡片数据
     * @param componentId 组件ID
     * @return 指标卡片数据列表
     */
    @Query("SELECT d FROM Data d WHERE d.component.id = :componentId " +
            "ORDER BY d.time DESC")
    Page<Data> getComponentMetricData(
            @Param("componentId") Integer componentId,
            Pageable pageable);

    /**
     * 查询指定用户的所有设备数据
     * @param userId 用户ID
     * @return 数据列表
     */
    @Query("SELECT d FROM Data d JOIN d.component c WHERE c.user.id = :userId")
    List<Data> findByUserId(@Param("userId") Integer userId);

    /**
     * 统计指定用户的所有设备数据总数
     * @param userId 用户ID
     * @return 数据总数
     */
    @Query("SELECT COUNT(d) FROM Data d JOIN d.component c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Integer userId);

    /**
     * 获取组件的最新数据
     * @param componentId 组件ID
     * @return 最新的数据
     */
    @Query("SELECT d FROM Data d WHERE d.component.id = :componentId ORDER BY d.time DESC")
    Optional<Data> getLatestComponentData(@Param("componentId") Integer componentId);

    /**
     * 将数据转换为属性列表格式
     * 注意：这不是JPQL查询，而是一个辅助方法，应该在Service层使用
     */
    default List<Map<String, Object>> getComponentAttributes(Integer componentId) {
        Optional<Data> dataOptional = getLatestComponentData(componentId);

        if (!dataOptional.isPresent()) {
            return List.of();
        }

        Data data = dataOptional.get();
        List<Map<String, Object>> attributes = new ArrayList<>();

        if (data.getHptEffMod() != null) {
            attributes.add(Map.of("name", "高压涡轮效率", "value", data.getHptEffMod()));
        }

        if (data.getNf() != null) {
            attributes.add(Map.of("name", "风扇转速", "value", data.getNf()));
        }

        if (data.getSmFan() != null) {
            attributes.add(Map.of("name", "风扇裕度", "value", data.getSmFan()));
        }

        if (data.getT24() != null) {
            attributes.add(Map.of("name", "风扇出口温度", "value", data.getT24()));
        }

        if (data.getWf() != null) {
            attributes.add(Map.of("name", "燃油流量", "value", data.getWf()));
        }

        if (data.getT48() != null) {
            attributes.add(Map.of("name", "HPT出口温度", "value", data.getT48()));
        }

        if (data.getNc() != null) {
            attributes.add(Map.of("name", "高压压气机转速", "value", data.getNc()));
        }

        if (data.getSmHPC() != null) {
            attributes.add(Map.of("name", "高压压气机裕度", "value", data.getSmHPC()));
        }

        return attributes;
    }

    /**
     * 获取健康指数值列表（最近7天）
     * @param componentId 组件ID
     * @return 健康指数值列表
     */
    default List<Double> getHealthValues(Integer componentId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<Object[]> healthData = getHealthIndexTrend(componentId, startDate);

        List<Double> values = new ArrayList<>();
        for (Object[] row : healthData) {
            if (row[1] != null) {
                values.add(((Number) row[1]).doubleValue());
            }
        }

        return values;
    }

    /**
     * 获取能耗值列表（最近7天）
     * @param componentId 组件ID
     * @return 能耗值列表
     */
    default List<Double> getEnergyValues(Integer componentId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<Object[]> energyData = getEnergyConsumptionTrend(componentId, startDate);

        List<Double> values = new ArrayList<>();
        for (Object[] row : energyData) {
            if (row[1] != null) {
                values.add(((Number) row[1]).doubleValue());
            }
        }

        return values;
    }
}