package com.example.software_management.Service;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.Model.Data;
import com.example.software_management.Model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DataService {

    /**
     * 上传数据
     * @param file 数据文件
     * @param name 数据名称
     * @param componentId 组件ID
     * @param currentUser 当前用户
     * @return 创建的数据对象
     * @throws IOException 如果文件处理出错
     */
    Data createData(MultipartFile file, String name, Integer componentId, User currentUser) throws IOException;

    /**
     * 获取用户组件下的所有数据
     * @param componentId 组件ID
     * @param currentUser 当前用户
     * @return 数据列表（不包含文件内容）
     */
    List<DataDTO> getUserComponentData(Integer componentId, User currentUser);

    /**
     * 下载数据
     * @param id 数据ID
     * @param currentUser 当前用户
     * @return 数据对象（包含文件内容）
     */
    Optional<Data> downloadData(Integer id, User currentUser);

    /**
     * 获取当前用户的所有数据
     * @param currentUser 当前用户
     * @return 数据列表（Map形式，包含id、name、time、result、component_id和component_name）
     */
    List<Map<String, Object>> getAllUserData(User currentUser);

    /**
     * 删除数据
     * @param id 数据ID
     * @param currentUser 当前用户
     * @return 如果删除成功返回true，否则返回false
     */
    boolean deleteData(Integer id, User currentUser);
}