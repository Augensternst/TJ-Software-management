package com.example.software_management.Service.Impl;

import com.example.software_management.Model.Model;
import com.example.software_management.Repository.ModelRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StandardDataServiceImplUnitTests {

    @Mock
    private ModelRepository modelRepository;

    @InjectMocks
    private ModelServiceImpl modelService;

    private List<Model> testStandardDatas;

    @BeforeEach
    public void setup() {
        // 初始化 mocks
        MockitoAnnotations.openMocks(this);

        // 准备测试数据 - 增加到60个
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
    @DisplayName("UT_STANDARD_DATA_SVC_001: 基本功能测试-默认参数")
    public void testGetStandardDatasWithDefaultParameters() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(eq(PageRequest.of(0, 10)))).thenReturn(page);

        // 调用服务方法
        Map<String, Object> result = modelService.getModels(1, 10, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(testStandardDatas.size(), ((Number) result.get("total")).intValue());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) result.get("models");
        assertEquals(10, standardDatas.size());
        assertEquals(1, standardDatas.get(0).get("id"));
        assertEquals("Standard Data 1", standardDatas.get(0).get("name"));

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findAll(eq(PageRequest.of(0, 10)));
        verify(modelRepository, never()).findByNameContaining(any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("UT_STANDARD_DATA_SVC_002: 参数校正测试-页码为0")
    public void testGetStandardDatasWithPageZero() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(eq(PageRequest.of(0, 10)))).thenReturn(page);

        // 调用服务方法，传入page=0
        Map<String, Object> result = modelService.getModels(0, 10, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> standardDatas = (List<Map<String, Object>>) result.get("models");
        assertEquals(10, standardDatas.size());

        // 验证参数校正 - 传入page=0，应该校正为1（对应页索引0）
        verify(modelRepository, times(1)).findAll(eq(PageRequest.of(0, 10)));
    }

    @Test
    @Order(3)
    @DisplayName("UT_STANDARD_DATA_SVC_003: 参数校正测试-pageSize为0")
    public void testGetStandardDatasWithPageSizeZero() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(eq(PageRequest.of(0, 10)))).thenReturn(page);

        // 调用服务方法，传入pageSize=0
        Map<String, Object> result = modelService.getModels(1, 0, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));

        // 验证参数校正 - 传入pageSize=0，应该校正为10
        verify(modelRepository, times(1)).findAll(eq(PageRequest.of(0, 10)));
    }

    @Test
    @Order(4)
    @DisplayName("UT_STANDARD_DATA_SVC_004: 参数校正测试-pageSize超过最大值")
    public void testGetStandardDatasWithPageSizeExceedsMax() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 50), PageRequest.of(0, 50), testStandardDatas.size());
        when(modelRepository.findAll(eq(PageRequest.of(0, 50)))).thenReturn(page);

        // 调用服务方法，传入pageSize=100
        Map<String, Object> result = modelService.getModels(1, 100, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));

        // 验证参数校正 - 传入pageSize=100，应该校正为50
        verify(modelRepository, times(1)).findAll(eq(PageRequest.of(0, 50)));
    }

    @Test
    @Order(5)
    @DisplayName("UT_STANDARD_DATA_SVC_005: 搜索功能测试-有匹配结果")
    public void testGetStandardDatasWithMatchingSearchQuery() {
        // 准备搜索结果
        List<Model> searchResults = new ArrayList<>();
        for (Model model : testStandardDatas) {
            if (model.getName().contains("涡轮")) {
                searchResults.add(model);
            }
        }

        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(searchResults, PageRequest.of(0, 10), searchResults.size());
        when(modelRepository.findByNameContaining(eq("涡轮"), eq(PageRequest.of(0, 10)))).thenReturn(page);

        // 调用服务方法
        Map<String, Object> result = modelService.getModels(1, 10, "涡轮");

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(searchResults.size(), ((Number) result.get("total")).intValue());

        // 验证仓库方法调用
        verify(modelRepository, times(1)).findByNameContaining(eq("涡轮"), eq(PageRequest.of(0, 10)));
        verify(modelRepository, never()).findAll((Pageable) any());
    }

    @Test
    @Order(6)
    @DisplayName("UT_STANDARD_DATA_SVC_006: 搜索功能测试-空白搜索词")
    public void testGetStandardDatasWithWhitespaceSearchQuery() {
        // 模拟仓库响应
        Page<Model> page = new PageImpl<>(testStandardDatas.subList(0, 10), PageRequest.of(0, 10), testStandardDatas.size());
        when(modelRepository.findAll(eq(PageRequest.of(0, 10)))).thenReturn(page);

        // 调用服务方法，传入搜索词为空格
        Map<String, Object> result = modelService.getModels(1, 10, "   ");

        // 验证结果
        assertNotNull(result);
        assertEquals(true, result.get("success"));

        // 验证仓库方法调用 - 空格搜索词应被trim后判断为空
        verify(modelRepository, times(1)).findAll(eq(PageRequest.of(0, 10)));
        verify(modelRepository, never()).findByNameContaining(any(), any());
    }

    @Test
    @Order(7)
    @DisplayName("UT_STANDARD_DATA_SVC_007: 仓库异常测试")
    public void testGetStandardDatasWithRepositoryException() {
        // 模拟仓库抛出异常
        when(modelRepository.findAll((Pageable) any())).thenThrow(new RuntimeException("Database error"));

        // 验证异常被向上传播
        Exception exception = assertThrows(RuntimeException.class, () -> {
            modelService.getModels(1, 10, null);
        });

        assertEquals("Database error", exception.getMessage());

        // 验证仓库方法被调用
        verify(modelRepository, times(1)).findAll((Pageable) any());
    }
}