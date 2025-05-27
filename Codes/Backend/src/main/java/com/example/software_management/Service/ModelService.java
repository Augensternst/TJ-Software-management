package com.example.software_management.Service;

import com.example.software_management.Model.Model;
import com.example.software_management.Model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ModelService {

    /**
     * 获取所有模型的简要信息（不包括文件内容）
     * @return 所有模型的列表
     */
    List<Map<String, Object>> getAllModels();

    /**
     * 创建新模型
     * @param file 模型文件
     * @param name 模型名称
     * @param style 模型风格
     * @param status 模型状态
     * @param description 模型描述
     * @param user 上传的用户
     * @return 创建的模型对象
     * @throws IOException 如果文件处理出错
     */
    Model createModel(MultipartFile file, String name, String style, String status,
                      String description, User user) throws IOException;

    /**
     * 获取各模型风格的占比
     * @return 风格名称和对应百分比的映射
     */
    Map<String, Double> getStylePercentage();

    /**
     * 获取各模型状态的占比
     * @return 状态名称和对应百分比的映射
     */
    Map<String, Double> getStatusPercentage();

    /**
     * 获取指定ID的模型
     * @param id 模型ID
     * @return 找到的模型对象
     */
    Optional<Model> getModelById(Integer id);

    /**
     * 删除指定ID的模型
     * @param id 模型ID
     * @param username 当前用户的用户名
     * @return 如果删除成功返回true
     */
    boolean deleteModel(Integer id, String username);

    /**
     * 计算文件的MD5值
     * @param data 文件内容的字节数组
     * @return MD5值的字符串表示
     */
    String getFileMd5(byte[] data);
}