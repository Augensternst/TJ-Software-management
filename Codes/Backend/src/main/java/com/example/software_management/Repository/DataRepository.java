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


}