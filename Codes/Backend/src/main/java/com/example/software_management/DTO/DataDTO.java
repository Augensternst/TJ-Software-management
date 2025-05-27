package com.example.software_management.DTO;

import com.example.software_management.Model.DData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DataDTO {
    private Integer id;
    private String name;
    private String result;
    private LocalDateTime uploadedTime;
    private Integer componentId;
    private String componentName;
    private String fileSize; // 以可读格式存储文件大小，如 "2.5 MB"

    // 构造函数
    public DataDTO() {
    }

    // 从 DData 实体转换为 DTO
    public DataDTO(DData data) {
        this.id = data.getId();
        this.name = data.getName();
        this.result = data.getResult();
        this.uploadedTime = data.getTime();

        // 处理关联实体，只获取必要的信息
        if (data.getComponent() != null) {
            this.componentId = data.getComponent().getId();
            this.componentName = data.getComponent().getName();
        }

        // 如果有文件数据，计算文件大小（但不包含实际数据）
        if (data.getFile() != null && data.getFile().length > 0) {
            this.fileSize = formatFileSize(data.getFile().length);
        } else {
            this.fileSize = "0 B";
        }
    }

    // 格式化文件大小为可读格式
    private String formatFileSize(long size) {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}