package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "device_data")
@Getter
@Setter
public class Data {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "file")
    private String file;

    @Column(name = "time", nullable = false)
    @NotNull
    private LocalDateTime time;

    @Column(name = "HPT_eff_mod")
    private Double hptEffMod;

    @Column(name = "Nf")
    private Double nf;

    @Column(name = "sm_fan")
    private Double smFan;

    @Column(name = "T24")
    private Double t24;

    @Column(name = "Wf")
    private Double wf;

    @Column(name = "T48")
    private Double t48;

    @Column(name = "Nc")
    private Double nc;

    @Column(name = "SmHPC")
    private Double smHPC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    @NotNull
    private Component component;

    // 设置上传时间的自动更新
    @PrePersist
    public void prePersist() {
        if (this.time == null) {
            this.time = LocalDateTime.now();
        }
    }

    // 参数正常范围的均值
    private static final Map<String, Double> FEATURE_MEANS = new HashMap<String, Double>() {{
        put("T24", 561.734307);
        put("T48", 1630.231993);
        put("Nf", 1993.529530);
        put("Nc", 8211.029946);
        put("Wf", 2.300304);
        put("SmFan", 19.497370);
        put("SmHPC", 28.382235);
        put("HPT_eff_mod", -0.000920);
    }};

    // 参数正常范围的标准差
    private static final Map<String, Double> FEATURE_STDS = new HashMap<String, Double>() {{
        put("T24", 19.197751);
        put("T48", 101.321474);
        put("Nf", 141.865723);
        put("Nc", 192.280626);
        put("Wf", 0.581590);
        put("SmFan", 1.727403);
        put("SmHPC", 2.149762);
        put("HPT_eff_mod", 0.000230);
    }};

    // 参数单位
    private static final Map<String, String> PARAMETER_UNITS = new HashMap<String, String>() {{
        put("T24", "K");
        put("T48", "K");
        put("Nf", "rpm");
        put("Nc", "rpm");
        put("Wf", "kg/s");
        put("SmFan", "%");
        put("SmHPC", "%");
        put("HPT_eff_mod", "");
    }};

    /**
     * 根据字段名计算健康指数，使用偏离正常值的百分比连续评估参数异常程度
     * @param fieldName 字段名称
     * @return 包含健康指数和单位的Map
     */
    @Transient
    public Map<String, Object> calculateHealthIndex(String fieldName) {
        Map<String, Object> result = new HashMap<>();

        // 设置默认值
        result.put("healthIndex", 100.0);
        result.put("unit", PARAMETER_UNITS.getOrDefault(fieldName, ""));

        // 获取字段值
        Double fieldValue = null;
        switch (fieldName) {
            case "T24": fieldValue = t24; break;
            case "T48": fieldValue = t48; break;
            case "Nf": fieldValue = nf; break;
            case "Nc": fieldValue = nc; break;
            case "Wf": fieldValue = wf; break;
            case "SmFan": fieldValue = smFan; break;
            case "SmHPC": fieldValue = smHPC; break;
            case "HPT_eff_mod": fieldValue = hptEffMod; break;
            default:
                result.put("error", "未知字段名: " + fieldName);
                return result;
        }

        // 如果字段值为空或没有对应的均值，返回默认健康指数
        if (fieldValue == null || !FEATURE_MEANS.containsKey(fieldName)) {
            result.put("error", "缺少数据或参考值");
            return result;
        }

        // 计算偏离正常值的百分比
        double mean = FEATURE_MEANS.get(fieldName);
        double deviationPercent;

        // 对于HPT_eff_mod这种接近于0的特殊参数，使用绝对偏差并归一化
        if (fieldName.equals("HPT_eff_mod") && Math.abs(mean) < 0.01) {
            double absoluteDeviation = Math.abs(fieldValue - mean);
            // 对于效率参数，0.003的偏差视为100%偏离
            deviationPercent = (absoluteDeviation / 0.003) * 100;
        } else {
            // 对于其他参数，计算相对偏离百分比
            deviationPercent = Math.abs((fieldValue - mean) / mean) * 100;
        }

        // 设置不同参数类型的最大可接受偏离百分比
        double maxAcceptableDeviation;
        if (fieldName.equals("T24") || fieldName.equals("T48")) {
            // 温度参数
            maxAcceptableDeviation = 20.0; // 20%偏离视为完全不健康
        } else if (fieldName.equals("Nf") || fieldName.equals("Nc")) {
            // 转速参数
            maxAcceptableDeviation = 18.0; // 18%偏离视为完全不健康
        } else if (fieldName.equals("HPT_eff_mod")) {
            // 效率参数 - 已经归一化为百分比
            maxAcceptableDeviation = 100.0; // 100%偏离视为完全不健康
        } else if (fieldName.equals("Wf")) {
            // 燃油流量
            maxAcceptableDeviation = 25.0; // 25%偏离视为完全不健康
        } else {
            // 其他参数
            maxAcceptableDeviation = 30.0; // 30%偏离视为完全不健康
        }

        // 连续计算健康指数: 线性映射偏离百分比到健康指数
        double healthIndex = 100.0 * (1.0 - Math.min(deviationPercent, maxAcceptableDeviation) / maxAcceptableDeviation);

        // 确保健康指数在0-100之间
        healthIndex = Math.max(0, Math.min(100, healthIndex));

        // 添加结果
        result.put("healthIndex", healthIndex);
        result.put("value", fieldValue);
        result.put("deviationPercent", deviationPercent);
        result.put("normalValue", mean); // 添加正常参考值

        return result;
    }

}