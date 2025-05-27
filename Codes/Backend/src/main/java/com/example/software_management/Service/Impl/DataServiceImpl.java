package com.example.software_management.Service.Impl;

import com.example.software_management.DTO.DataDTO;
import com.example.software_management.Model.Component;
import com.example.software_management.Model.Data;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.ComponentRepository;
import com.example.software_management.Repository.DataRepository;
import com.example.software_management.Service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DataServiceImpl implements DataService {

    private final DataRepository dataRepository;
    private final ComponentRepository componentRepository;

    @Autowired
    public DataServiceImpl(DataRepository dataRepository, ComponentRepository componentRepository) {
        this.dataRepository = dataRepository;
        this.componentRepository = componentRepository;
    }

    @Override
    @Transactional
    public Data createData(MultipartFile file, String name, Integer componentId, User currentUser) throws IOException {
        // 检查组件是否存在
        Optional<Component> componentOpt = componentRepository.findById(componentId);
        if (componentOpt.isEmpty()) {
            throw new IllegalArgumentException("组件不存在");
        }

        Component component = componentOpt.get();

        // 检查是否是当前用户的组件
        if (!component.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权访问其它用户的组件信息");
        }

        // 创建数据对象
        Data data = new Data();
        data.setFile(file.getBytes());
        data.setName(name);
        data.setComponent(component);

        // 保存数据到数据库
        return dataRepository.save(data);
    }

    @Override
    public List<DataDTO> getUserComponentData(Integer componentId, User currentUser) {
        // 检查组件是否存在
        Optional<Component> componentOpt = componentRepository.findById(componentId);
        if (componentOpt.isEmpty()) {
            throw new IllegalArgumentException("组件不存在");
        }

        Component component = componentOpt.get();

        // 检查是否是当前用户的组件
        if (!component.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权访问其它用户的组件信息");
        }

        // 获取组件下所有数据
        List<Data> dataList = dataRepository.findByComponentId(componentId);
        List<DataDTO> dataDTOList = new ArrayList<>();

        // 清除文件内容以减少传输大小
        for (Data data : dataList) {
            dataDTOList.add(new DataDTO(data));
        }

        return dataDTOList;
    }

    @Override
    public Optional<Data> downloadData(Integer id, User currentUser) {
        Optional<Data> dataOpt = dataRepository.findById(id);

        if (dataOpt.isEmpty()) {
            return Optional.empty();
        }

        Data data = dataOpt.get();

        // 检查是否是当前用户的数据
        if (!data.getComponent().getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权下载其它用户的数据");
        }

        return Optional.of(data);
    }

    @Override
    public List<Map<String, Object>> getAllUserData(User currentUser) {
        return dataRepository.findByUserWithoutFile(currentUser);
    }

    @Override
    @Transactional
    public boolean deleteData(Integer id, User currentUser) {
        Optional<Data> dataOpt = dataRepository.findById(id);

        if (dataOpt.isEmpty()) {
            return false;
        }

        Data data = dataOpt.get();

        // 检查是否是当前用户的数据
        if (!data.getComponent().getUser().getUsername().equals(currentUser.getUsername())) {
            throw new SecurityException("无权删除其它用户的数据");
        }

        dataRepository.delete(data);
        return true;
    }
}