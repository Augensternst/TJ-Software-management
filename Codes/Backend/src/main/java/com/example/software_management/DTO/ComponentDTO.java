// ComponentDTO.java
package com.example.software_management.DTO;

import com.example.software_management.Model.Component;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComponentDTO {
    private Integer id;
    private String name;
    private Integer status;
    private Integer lifeForecast;
    private String location;
    private LocalDateTime updatedTime;
    private String description;
    private Integer modelId;
    private String modelName;
    private String username;

    // 构造函数
    public ComponentDTO() {
    }

    // 从 Component 实体转换为 DTO
    public ComponentDTO(Component component) {
        this.id = component.getId();
        this.name = component.getName();
        this.status = component.getStatus();
        this.lifeForecast = component.getLifeForecast();
        this.location = component.getLocation();
        this.updatedTime = component.getUpdatedTime();
        this.description = component.getDescription();

        // 处理关联实体，只获取必要的信息而不是整个对象
        if (component.getModel() != null) {
            this.modelId = component.getModel().getId();
            this.modelName = component.getModel().getName();
        }

        if (component.getUser() != null) {
            this.username = component.getUser().getUsername();
        }


    }
}
