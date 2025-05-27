package com.example.software_management.Repository;

import com.example.software_management.Model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * 创建预测记录并返回
     * 注意：该方法是对JpaRepository中save方法的自定义封装
     * @param prediction 预测实体对象
     * @return 保存后的预测实体（包含生成的ID）
     */
    @Query("SELECT p FROM Prediction p WHERE p.id = :#{#prediction.id}")
    default Prediction createPrediction(@Param("prediction") Prediction prediction) {

        return save(prediction);
    }

}