package com.example.software_management.Controller;

import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ModelRepository;
import com.example.software_management.Service.Impl.ModelServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StandardDataControllerIntegrationTests {

    @Mock
    private ModelRepository modelRepository;

    @InjectMocks
    private ModelServiceImpl modelService;

    private ModelController modelController;

    private List<Model> testStandardDatas;

    @BeforeEach
    public void setup() {
        // 初始化 mocks
        MockitoAnnotations.openMocks(this);

        // 将服务注入到控制器
        modelController = new ModelController(modelService);

        // 准备测试数据
        testStandardDatas = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            Model model = new Model();
            model.setId(i);
            model.setName("Standard Data " + i);
            model.setType("Type " + (i % 3 + 1));
            testStandardDatas.add(model);
        }

        // 添加一些特殊测试数据
        Model turbineModel = new Model();
        turbineModel.setId(61);
        turbineModel.setName("涡轮发动机");
        turbineModel.setType("Type 2");
        testStandardDatas.add(turbineModel);

        Model engineModel = new Model();
        engineModel.setId(62);
        engineModel.setName("高性能发动机");
        engineModel.setType("Type 1");
        testStandardDatas.add(engineModel);
    }

    @Test
    @Order(1)
    @DisplayName("IT_STANDARD_DATA_CTRL_001: 基本功能测试-默认参数")
    public void testGetStandardDatasWithDefaultParameters() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(any(Pageable.class))).thenReturn(page);

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 10, null);

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(testStandardDatas.size(), ((Number) responseBody.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(10, standardDatas.size());
        assertEquals(1, standardDatas.get(0).get("id"));
        assertEquals("Standard Data 1", standardDatas.get(0).get("name"));

        // 验证仓库方法被正确调用
        verify(modelRepository, times(1)).findAll(PageRequest.of(0, 10));
        verify(modelRepository, never()).findByNameContaining(any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("IT_STANDARD_DATA_CTRL_002: 分页参数测试-有效参数")
    public void testGetStandardDatasWithValidPaginationParameters() {
        // 模拟仓库响应 - 第2页，每页15条
        int page = 1; // 0-based page index
        int size = 15;
        int startIdx = page * size;
        int endIdx = Math.min(startIdx + size, testStandardDatas.size());

        Page<Model> pageResult = new PageImpl<>(
                testStandardDatas.subList(startIdx, endIdx),
                PageRequest.of(page, size),
                testStandardDatas.size()
        );

        when(modelRepository.findAll(PageRequest.of(page, size))).thenReturn(pageResult);

        // 调用控制器方法 (注意：API中的页码是1-based)
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(2, 15, null);

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(testStandardDatas.size(), ((Number) responseBody.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(endIdx - startIdx, standardDatas.size());
        assertEquals(16, standardDatas.get(0).get("id"));

        // 验证仓库方法被正确调用，并且参数正确
        verify(modelRepository, times(1)).findAll(PageRequest.of(page, size));
    }

    @Test
    @Order(3)
    @DisplayName("IT_STANDARD_DATA_CTRL_003: 参数校正测试-页码为0")
    public void testGetStandardDatasWithPageZero() {
        // 模拟仓库响应 - 应该校正为第1页
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // 调用控制器方法，传入page=0
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(0, 10, null);

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(1, standardDatas.get(0).get("id"));

        // 验证参数校正 - 即使传入page=0，也应该调用findAll时使用page=0 (0-based索引)
        verify(modelRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    @Order(4)
    @DisplayName("IT_STANDARD_DATA_CTRL_004: 参数校正测试-pageSize为0")
    public void testGetStandardDatasWithPageSizeZero() {
        // 模拟仓库响应 - 应该校正为每页10条
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        // 调用控制器方法，传入pageSize=0
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 0, null);

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(10, standardDatas.size());

        // 验证参数校正 - 传入pageSize=0，应该校正为10
        verify(modelRepository, times(1)).findAll(PageRequest.of(0, 10));
    }

    @Test
    @Order(5)
    @DisplayName("IT_STANDARD_DATA_CTRL_005: 参数校正测试-pageSize超过最大值")
    public void testGetStandardDatasWithPageSizeExceedsMax() {
        // 模拟仓库响应 - 应该校正为每页50条
        List<Model> subList = testStandardDatas.subList(0, Math.min(50, testStandardDatas.size()));
        Page<Model> page = new PageImpl<>(subList, PageRequest.of(0, 50), testStandardDatas.size());
        when(modelRepository.findAll(PageRequest.of(0, 50))).thenReturn(page);

        // 调用控制器方法，传入pageSize=100
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 100, null);

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(subList.size(), standardDatas.size());

        // 验证参数校正 - 传入pageSize=100，应该校正为50
        verify(modelRepository, times(1)).findAll(PageRequest.of(0, 50));
    }

    @Test
    @Order(6)
    @DisplayName("IT_STANDARD_DATA_CTRL_006: 搜索功能测试-有匹配结果")
    public void testGetStandardDatasWithMatchingSearchQuery() {
        // 准备搜索结果
        List<Model> searchResults = new ArrayList<>();
        for (Model model : testStandardDatas) {
            if (model.getName().contains("涡轮")) {
                searchResults.add(model);
            }
        }

        Page<Model> page = new PageImpl<>(searchResults, PageRequest.of(0, 10), searchResults.size());
        when(modelRepository.findByNameContaining(eq("涡轮"), any(Pageable.class))).thenReturn(page);

        // 调用控制器方法，传入searchQuery="涡轮"
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 10, "涡轮");

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(searchResults.size(), ((Number) responseBody.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertTrue(standardDatas.get(0).get("name").toString().contains("涡轮"));

        // 验证正确调用搜索方法而不是findAll
        verify(modelRepository, times(1)).findByNameContaining(eq("涡轮"), any(Pageable.class));
        verify(modelRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @Order(7)
    @DisplayName("IT_STANDARD_DATA_CTRL_007: 搜索功能测试-无匹配结果")
    public void testGetStandardDatasWithNoMatchingSearchQuery() {
        // 模拟仓库响应 - 无匹配结果
        Page<Model> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        when(modelRepository.findByNameContaining(eq("不存在的标准数据"), any(Pageable.class))).thenReturn(emptyPage);

        // 调用控制器方法，传入searchQuery="不存在的标准数据"
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 10, "不存在的标准数据");

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(0, ((Number) responseBody.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertEquals(0, standardDatas.size());

        // 验证即使没有结果，也正确调用了搜索方法
        verify(modelRepository, times(1)).findByNameContaining(eq("不存在的标准数据"), any(Pageable.class));
    }

    @Test
    @Order(8)
    @DisplayName("IT_STANDARD_DATA_CTRL_008: 搜索边界测试-空搜索词")
    public void testGetStandardDatasWithEmptySearchQuery() {
        // 模拟仓库响应 - 空搜索词应返回所有标准数据
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(any(Pageable.class))).thenReturn(page);

        // 调用控制器方法，传入searchQuery=""
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 10, "");

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(testStandardDatas.size(), ((Number) responseBody.get("total")).intValue());

        // 验证空搜索词应调用findAll而不是findByNameContaining
        verify(modelRepository, times(1)).findAll(any(Pageable.class));
        verify(modelRepository, never()).findByNameContaining(any(), any());
    }

    @Test
    @Order(9)
    @DisplayName("IT_STANDARD_DATA_CTRL_009: 异常测试-Repository异常")
    public void testGetStandardDatasWithRepositoryException() {
        // 模拟仓库抛出异常
        when(modelRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Database error"));

        // 调用控制器方法
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(1, 10, null);

        // 验证响应状态
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(false, responseBody.get("success"));
        assertEquals("Database error", responseBody.get("message"));

        // 验证方法被调用且异常被捕获
        verify(modelRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Order(10)
    @DisplayName("IT_STANDARD_DATA_CTRL_010: 组合测试-多参数校正")
    public void testGetStandardDatasWithMultipleParameterCorrections() {
        // 准备搜索结果
        List<Model> searchResults = new ArrayList<>();
        for (Model model : testStandardDatas) {
            if (model.getName().contains("发动机")) {
                searchResults.add(model);
            }
        }

        Page<Model> page = new PageImpl<>(searchResults, PageRequest.of(0, 50), searchResults.size());
        when(modelRepository.findByNameContaining(eq("发动机"), any(Pageable.class))).thenReturn(page);

        // 调用控制器方法，传入page=0, pageSize=100, searchQuery="发动机"
        ResponseEntity<Map<String, Object>> responseEntity = modelController.getModels(0, 100, "发动机");

        // 验证响应状态
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // 验证响应内容
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(true, responseBody.get("success"));
        assertEquals(searchResults.size(), ((Number) responseBody.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) responseBody.get("models");
        assertTrue(standardDatas.get(0).get("name").toString().contains("发动机"));

        // 验证多个参数同时校正 - page从0到1，pageSize从100到50
        verify(modelRepository, times(1)).findByNameContaining(eq("发动机"), eq(PageRequest.of(0, 50)));
    }
}