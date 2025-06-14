package com.example.software_management.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "model")
@Getter
@Setter
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    @Size(max = 50)
    @NotNull
    private String name;

    @Column(name = "model_file")
    private String modelfile;

    @Column(name = "type")
    private String type;

    // 与Forecast的一对多关系
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Forecast> forecasts;


}