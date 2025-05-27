package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Model;
import com.example.software_management.Model.User;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModelServiceImpl implements ModelService {

    private final ModelRepository modelRepository;

    @Autowired
    public ModelServiceImpl(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Override
    public List<Map<String, Object>> getAllModels() {
        return modelRepository.findAllModelsWithoutFile();
    }

    @Override
    @Transactional
    public Model createModel(MultipartFile file, String name, String style, String status,
                             String description, User user) throws IOException {

        System.out.println("here!!!!!!!!!!!!!");
        // 检查模型名是否重复
        if (modelRepository.existsByName(name)) {
            throw new IllegalArgumentException("模型名已存在");
        }

        byte[] fileContent = file.getBytes();
        String md5 = getFileMd5(fileContent);

        // 检查MD5是否重复
        if (modelRepository.existsByMd5(md5)) {
            throw new IllegalArgumentException("模型文件已存在");
        }

        // 创建模型对象
        Model model = new Model();
        model.setName(name);
        model.setStyle(style);
        model.setStatus(status);
        model.setDescription(description);
        model.setModelfile(fileContent);
        model.setMd5(md5);
        model.setUser(user);

        // 保存模型到数据库
        Model savedModel = modelRepository.save(model);

        // 将模型文件保存到本地文件系统
        saveModelFile(fileContent, md5);

        return savedModel;
    }

    @Override
    public Map<String, Double> getStylePercentage() {
        List<String> styles = modelRepository.findAllStyles();
        Map<String, Double> stylePercentage = new HashMap<>();

        if (styles.isEmpty()) {
            return stylePercentage;
        }

        // 计算每种风格的数量
        Map<String, Long> styleCounts = styles.stream()
                .collect(Collectors.groupingBy(style -> style, Collectors.counting()));

        // 计算总数
        double total = styles.size();

        // 计算百分比
        styleCounts.forEach((style, count) ->
                stylePercentage.put(style, count / total)
        );

        return stylePercentage;
    }

    @Override
    public Map<String, Double> getStatusPercentage() {
        List<String> statuses = modelRepository.findAllStatuses();
        Map<String, Double> statusPercentage = new HashMap<>();

        if (statuses.isEmpty()) {
            return statusPercentage;
        }

        // 计算每种状态的数量
        Map<String, Long> statusCounts = statuses.stream()
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));

        // 计算总数
        double total = statuses.size();

        // 计算百分比
        statusCounts.forEach((status, count) ->
                statusPercentage.put(status, count / total)
        );

        return statusPercentage;
    }

    @Override
    public Optional<Model> getModelById(Integer id) {
        Optional<Model> modelOpt = modelRepository.findById(id);

        modelOpt.ifPresent(model -> {
            // 确保模型文件存在于本地文件系统中
            String md5 = model.getMd5();
            Path filePath = getModelFilePath(md5);

            if (!Files.exists(filePath)) {
                try {
                    saveModelFile(model.getModelfile(), md5);
                } catch (IOException e) {
                    // 处理异常
                    e.printStackTrace();
                }
            }
        });

        return modelOpt;
    }

    @Override
    @Transactional
    public boolean deleteModel(Integer id, String username) {
        Optional<Model> modelOpt = modelRepository.findById(id);

        if (modelOpt.isPresent()) {
            Model model = modelOpt.get();

            // 检查用户权限
            if (model.getUser() != null && !model.getUser().getUsername().equals(username)) {
                throw new SecurityException("无权删除其它用户的模型");
            }

            modelRepository.delete(model);
            return true;
        }

        return false;
    }

    @Override
    public String getFileMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            BigInteger bigInt = new BigInteger(1, digest);
            String hashText = bigInt.toString(16);
            // 手动填充前导零
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 将模型文件保存到本地文件系统
     */
    private void saveModelFile(byte[] fileContent, String md5) throws IOException {
        // 创建目录（如果不存在）
        Path dirPath = Paths.get("./model_files");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 保存文件
        Path filePath = getModelFilePath(md5);
        Files.write(filePath, fileContent);
    }

    /**
     * 获取模型文件的路径
     */
    private Path getModelFilePath(String md5) {
        return Paths.get("./model_files", md5);
    }

    // 提供padStart方法（类似于JavaScript中的功能）
    private static String padStart(String str, int length, char padChar) {
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
}