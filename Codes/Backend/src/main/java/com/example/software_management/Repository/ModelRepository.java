package com.example.software_management.Repository;

import com.example.software_management.Model.MModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ModelRepository extends JpaRepository<MModel, Integer> {

    // 根据名称查找模型
    boolean existsByName(String name);

    // 根据MD5值查找模型
    boolean existsByMd5(String md5);

    // 查找所有模型的简要信息（不包括文件内容）
    @Query("SELECT m.id as id, m.name as name, m.style as style, m.status as status, "
            + "m.description as description, m.uploadedTime as uploadedTime, m.md5 as md5 "
            + "FROM MModel m")
    List<Map<String, Object>> findAllModelsWithoutFile();

    // 获取所有风格
    @Query("SELECT m.style FROM MModel m")
    List<String> findAllStyles();

    // 获取所有状态
    @Query("SELECT m.status FROM MModel m")
    List<String> findAllStatuses();

    // 根据用户名查询模型
    List<MModel> findByUserUsername(String username);
}