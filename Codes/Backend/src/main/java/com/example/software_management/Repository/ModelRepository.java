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

}