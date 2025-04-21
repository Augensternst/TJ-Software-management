package com.example.software_management.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mmodel")
@Getter
@Setter
public class MModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    @Size(max = 50)
    @NotNull
    private String name;

    @Column(name = "style", length = 50, nullable = false)
    @Size(max = 50)
    @NotNull
    private String style = "default";

    @Column(name = "uploaded_time", nullable = false)
    @NotNull
    private LocalDateTime uploadedTime;

    @Column(name = "status", length = 10, nullable = false)
    @Size(max = 10)
    @NotNull
    private String status = "未知";

    @Column(name = "description", nullable = false)
    @NotNull
    private String description;

    @Lob
    @Column(name = "modelfile", nullable = false)
    @NotNull
    private byte[] modelfile;

    @Column(name = "md5", length = 32, nullable = false, unique = true)
    @Size(max = 32)
    @NotNull
    private String md5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username")
    private User user;

    // 与Component的一对多关系
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    private List<Component> components;

    // 设置上传时间的自动更新
    @PrePersist
    public void prePersist() {
        if (this.uploadedTime == null) {
            this.uploadedTime = LocalDateTime.now();
        }
    }
}