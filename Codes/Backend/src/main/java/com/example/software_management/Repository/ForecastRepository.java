package com.example.software_management.Repository;

import com.example.software_management.Model.Forecast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Integer> {

    /**
     * 根据组件ID查找最新的预测
     * @param componentId 组件ID
     * @return 最新的预测
     */
    Optional<Forecast> findFirstByComponentIdOrderByForecastTimeDesc(Integer componentId);

    /**
     * 根据模型ID查找预测
     * @param modelId 模型ID
     * @param pageable 分页参数
     * @return 预测分页结果
     */
    Page<Forecast> findByModelId(Integer modelId, Pageable pageable);

    /**
     * 根据模型ID和组件ID查找预测
     * @param modelId 模型ID
     * @param componentId 组件ID
     * @return 预测列表
     */
    List<Forecast> findByModelIdAndComponentId(Integer modelId, Integer componentId);

    /**
     * 查询用户的所有预测
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 预测分页结果
     */
    @Query("SELECT f FROM Forecast f JOIN f.component c WHERE c.user.id = :userId")
    Page<Forecast> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    /**
     * 根据寿命预测范围查询预测
     * @param minLifeForecast 最小寿命预测
     * @param maxLifeForecast 最大寿命预测
     * @param pageable 分页参数
     * @return 预测分页结果
     */
    Page<Forecast> findByLifeForecastBetween(
            Double minLifeForecast,
            Double maxLifeForecast,
            Pageable pageable);

    /**
     * 统计预测寿命在特定范围内的组件数量
     * @param userId 用户ID
     * @param maxLifeForecast 最大寿命预测
     * @return 组件数量
     */
    @Query("SELECT COUNT(DISTINCT f.component.id) FROM Forecast f " +
            "JOIN f.component c WHERE c.user.id = :userId " +
            "AND f.id IN (SELECT MAX(f2.id) FROM Forecast f2 WHERE f2.component = f.component) " +
            "AND f.lifeForecast <= :maxLifeForecast")
    long countComponentsWithLifeForecastLessThan(
            @Param("userId") Integer userId,
            @Param("maxLifeForecast") Double maxLifeForecast);

    /**
     * 获取模拟结果
     * @param modelId 模型ID
     * @param componentId 组件ID
     * @return 最新的预测结果
     */
    @Query("SELECT f FROM Forecast f " +
            "WHERE f.model.id = :modelId AND f.component.id = :componentId " +
            "ORDER BY f.forecastTime DESC")
    Optional<Forecast> getSimulationResult(
            @Param("modelId") Integer modelId,
            @Param("componentId") Integer componentId);
}