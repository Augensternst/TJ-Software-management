package com.example.software_management.Service;

import com.example.software_management.DTO.ComponentDTO;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentService {

    /**
     * 上传组件
     * @param pic 组件图片
     * @param name 组件名称
     * @param location 组件位置
     * @param modelId 模型ID（可以为null）
     * @param description 描述（可以为null）
     * @param currentUser 当前用户
     * @return 创建的组件对象
     * @throws IOException 如果文件处理出错
     */
    ComponentDTO uploadComponent(MultipartFile pic, String name, String location,
                                 Integer modelId, String description, User currentUser) throws IOException;

    /**
     * 获取用户的所有组件
     * @param currentUser 当前用户
     * @return 组件信息列表
     */
    List<Map<String, Object>> getUserComponents(User currentUser);

    /**
     * 获取用户各模型的组件数量
     * @param currentUser 当前用户
     * @return 模型名称和组件数量的映射
     */
    Map<String, Integer> getModelCount(User currentUser);

    /**
     * 获取用户各位置的组件数量
     * @param currentUser 当前用户
     * @return 位置和组件数量的映射
     */
    Map<String, Integer> getLocationCount(User currentUser);

    /**
     * 获取组件图片
     * @param id 组件ID
     * @param currentUser 当前用户
     * @return 组件图片的字节数组
     */
    byte[] getComponentPic(Integer id, User currentUser);

    /**
     * 删除组件
     * @param id 组件ID
     * @param currentUser 当前用户
     * @return 如果删除成功返回true，否则返回false
     */
    boolean deleteComponent(Integer id, User currentUser);
}