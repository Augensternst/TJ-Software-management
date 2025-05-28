package com.example.software_management.Repository;

import com.example.software_management.Model.Model;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer> {

    // 标准分页查询
    @NotNull Page<Model> findAll(@NotNull Pageable pageable);

    // 按名称搜索的分页查询
    Page<Model> findByNameContaining(String name, Pageable pageable);

    /**
     * 根据名称查找模型
     * @param name 模型名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据类型查找模型
     * @param type 模型类型
     * @param pageable 分页参数
     * @return 模型分页结果
     */
    Page<Model> findByType(String type, Pageable pageable);

    /**
     * 统计模型总数
     * @return 模型总数
     */
    @Query("SELECT COUNT(m) FROM Model m")
    long countModels();

    /**
     * 获取所有模型类型
     * @return 模型类型列表
     */
    @Query("SELECT DISTINCT m.type FROM Model m WHERE m.type IS NOT NULL")
    List<String> findAllTypes();

    /**
     * 根据组件ID查询使用过的模型
     * @param componentId 组件ID
     * @return 模型列表
     */
    @Query("SELECT DISTINCT m FROM Model m JOIN Forecast f ON f.model = m " +
            "WHERE f.component.id = :componentId")
    List<Model> findModelsByComponentId(@Param("componentId") Integer componentId);
}