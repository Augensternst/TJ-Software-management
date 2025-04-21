package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ddata")
@Getter
@Setter
public class DData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "file", nullable = false)
    @NotNull
    private byte[] file;

    @Column(name = "name", length = 50)
    @Size(max = 50)
    private String name;

    @Column(name = "time", nullable = false)
    @NotNull
    private LocalDateTime time;

    @Column(name = "result")
    private String result;

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
}