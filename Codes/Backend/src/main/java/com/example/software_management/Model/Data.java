package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

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

    @Column(name = "SmFan")
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

    /**
     * 计算健康指数
     * @return 健康指数（0-100）
     */
    @Transient
    public Double calculateHealthIndex() {
        // 简单实现：计算所有指标的平均值并映射到0-100范围
        double sum = 0;
        int count = 0;

        if (hptEffMod != null) { sum += normalize(hptEffMod, 0, 100); count++; }
        if (nf != null) { sum += normalize(nf, 1000, 3000); count++; }
        if (smFan != null) { sum += normalize(smFan, 0, 100); count++; }
        if (t24 != null) { sum += 100 - normalize(t24, 20, 80); count++; } // 温度越低越好
        if (wf != null) { sum += 100 - normalize(wf, 0, 30); count++; } // 燃油流量越低越好
        if (t48 != null) { sum += 100 - normalize(t48, 500, 1000); count++; } // 温度越低越好
        if (nc != null) { sum += normalize(nc, 1000, 3000); count++; }
        if (smHPC != null) { sum += normalize(smHPC, 0, 100); count++; }

        return count > 0 ? sum / count : null;
    }

    /**
     * 归一化数值到0-100范围
     */
    private double normalize(double value, double min, double max) {
        if (value < min) return 0;
        if (value > max) return 100;
        return ((value - min) / (max - min)) * 100;
    }

    /**
     * 获取性能最低的指标名称
     * @return 性能最低指标名称
     */
    @Transient
    public String getLowestPerformanceAttribute() {
        double minValue = Double.MAX_VALUE;
        String attributeName = null;

        // 归一化评分越低表示性能越差
        double hptEffModScore = hptEffMod != null ? normalize(hptEffMod, 0, 100) : Double.MAX_VALUE;
        if (hptEffModScore < minValue) { minValue = hptEffModScore; attributeName = "高压涡轮效率"; }

        double nfScore = nf != null ? normalize(nf, 1000, 3000) : Double.MAX_VALUE;
        if (nfScore < minValue) { minValue = nfScore; attributeName = "风扇转速"; }

        double smFanScore = smFan != null ? normalize(smFan, 0, 100) : Double.MAX_VALUE;
        if (smFanScore < minValue) { minValue = smFanScore; attributeName = "风扇裕度"; }

        double t24Score = t24 != null ? 100 - normalize(t24, 20, 80) : Double.MAX_VALUE;
        if (t24Score < minValue) { minValue = t24Score; attributeName = "风扇出口温度"; }

        double wfScore = wf != null ? 100 - normalize(wf, 0, 30) : Double.MAX_VALUE;
        if (wfScore < minValue) { minValue = wfScore; attributeName = "燃油流量"; }

        double t48Score = t48 != null ? 100 - normalize(t48, 500, 1000) : Double.MAX_VALUE;
        if (t48Score < minValue) { minValue = t48Score; attributeName = "HPT出口温度"; }

        double ncScore = nc != null ? normalize(nc, 1000, 3000) : Double.MAX_VALUE;
        if (ncScore < minValue) { minValue = ncScore; attributeName = "高压压气机转速"; }

        double smHPCScore = smHPC != null ? normalize(smHPC, 0, 100) : Double.MAX_VALUE;
        if (smHPCScore < minValue) { minValue = smHPCScore; attributeName = "高压压气机裕度"; }

        return attributeName != null ? attributeName + "出问题" : "未知问题";
    }
}