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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    @Size(max = 50)
    @NotNull
    private String username;

    @Column(name = "hashed_password", length = 128, nullable = false)
    @Size(max = 128)
    @NotNull
    @JsonIgnore
    private String hashedPassword;

    @Column(name = "phone", length = 50, nullable = false, unique = true)
    @Size(max = 50)
    @NotNull
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "role", length = 20)
    private String role = "USER";

    // 与Component的一对多关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Component> components;

    // 与Alert的一对多关系（用户确认的警报）
    @OneToMany(mappedBy = "confirmedBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Alert> confirmedAlerts;

}