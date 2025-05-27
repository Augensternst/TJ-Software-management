package com.example.software_management.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {

    @Id
    @Column(name = "username", length = 50, nullable = false)
    @Size(max = 50)
    private String username;

    @Column(name = "hashed_password", length = 128, nullable = false)
    @Size(max = 128)
    @NotNull
    private String hashedPassword;

    @Column(name = "phone", length = 50, nullable = false, unique = true)
    @Size(max = 50)
    @NotNull
    private String phone;

    // 与MModel的一对多关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<MModel> models;

    // 与Component的一对多关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Component> components;

    // 在User类中添加
    @OneToMany(mappedBy = "processedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Alert> processedAlerts;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Prediction> createdPredictions;

}