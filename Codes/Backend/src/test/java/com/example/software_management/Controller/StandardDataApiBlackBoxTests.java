package com.example.software_management.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class StandardDataApiBlackBoxTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void setup() throws Exception {
        // 登录获取token
        if (token == null) {
            String username = "wuhuairline";
            String password = "123456";

            MvcResult loginResult = mockMvc.perform(post("/api/user/account/token/")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=" + username + "&password=" + password))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode tokenResponse = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            token = tokenResponse.get("token").asText();
        }
    }

    // 封装获取请求构建器的方法，自动添加token
    private MockHttpServletRequestBuilder getRequestWithToken() {
        return get("/api/standard/getDatas")
                .header("Authorization", "Bearer " + token);
    }

    @Nested
    @Order(1)
    @DisplayName("A. 页码参数测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PaginationTests {

        @Test
        @Order(1)
        @DisplayName("TC_PROCESS_DATA_API_001_01: 默认分页参数")
        public void testDefaultPagination() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken())
                    .andExpect(status().isOk())
                    .andReturn();

            // 验证响应格式符合API文档
            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 根据API文档验证响应结构
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.get("models").isArray());

            // 验证默认分页 (page=1, pageSize=10)
            assertTrue(responseJson.get("models").size() <= 10, "默认每页应返回不超过10条记录");
        }

        @Test
        @Order(2)
        @DisplayName("TC_PROCESS_DATA_API_001_02: 自定义分页 - page=1, pageSize=1")
        public void testCustomPaginationSingleItem() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "1")
                            .param("pageSize", "1"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证响应格式符合API文档
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));
            assertTrue(responseJson.get("success").asBoolean());

            // 验证自定义分页效果
            assertEquals(1, responseJson.get("models").size(), "应返回1条记录");

            // 验证返回的数据项结构符合API文档
            JsonNode firstModel = responseJson.get("models").get(0);
            assertTrue(firstModel.has("id"), "模型数据应包含id字段");
            assertTrue(firstModel.has("name"), "模型数据应包含name字段");
        }

        @Test
        @Order(3)
        @DisplayName("TC_PROCESS_DATA_API_001_03: 自定义分页 - page=2, pageSize=50")
        public void testCustomPaginationMaxPageSize() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "2")
                            .param("pageSize", "50"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证响应格式符合API文档
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));
            assertTrue(responseJson.get("success").asBoolean());

            // 验证页大小限制
            assertTrue(responseJson.get("models").size() <= 50, "每页应返回不超过50条记录");
        }

        @Test
        @Order(4)
        @DisplayName("TC_PROCESS_DATA_API_001_04: 边界值 - page=0")
        public void testPageZero() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "0"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理无效页码
            // 应返回第1页数据(自动纠正)或明确的错误信息
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为第1页
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 应使用默认页大小
                assertTrue(responseJson.get("models").size() <= 10);
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(5)
        @DisplayName("TC_PROCESS_DATA_API_001_05: 边界值 - page=-1")
        public void testPageNegative() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "-1"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理负数页码
            // 应返回第1页数据(自动纠正)或明确的错误信息
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为第1页
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(6)
        @DisplayName("TC_PROCESS_DATA_API_001_06: 边界值 - 超出总页数")
        public void testPageExceedTotal() throws Exception {
            // 先获取总记录数以确定总页数
            MvcResult countResult = mockMvc.perform(getRequestWithToken())
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode countJson = objectMapper.readTree(countResult.getResponse().getContentAsString());
            int total = countJson.get("total").asInt();
            int pageSize = 10; // 默认页大小
            int totalPages = (int) Math.ceil((double) total / pageSize);

            // 请求超出总页数的页码
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", String.valueOf(totalPages + 1)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理超出范围的页码
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("models"));

            // 应返回空数组或最后一页数据
            if (!responseJson.get("models").isEmpty()) {
                // 如果返回最后一页数据
                assertTrue(responseJson.get("models").size() <= pageSize);
            } else {
                // 如果返回空数组
                assertEquals(0, responseJson.get("models").size());
            }
        }

        @Test
        @Order(7)
        @DisplayName("TC_PROCESS_DATA_API_001_07: 边界值 - 极大页码")
        public void testExtremelyLargePage() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "999"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理极大页码
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("models"));

            // 应返回空数组
            assertEquals(0, responseJson.get("models").size(), "超大页码应返回空数组");
        }

        @Test
        @Order(8)
        @DisplayName("TC_PROCESS_DATA_API_001_08: 边界值 - 小数页码")
        public void testDecimalPage() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "1.5"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理小数页码
            assertTrue(responseJson.has("success"));

            System.out.println(responseJson);

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为整数页码
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(9)
        @DisplayName("TC_PROCESS_DATA_API_001_09: 边界值 - 非数字页码")
        public void testNonNumericPage() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("page", "abc"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理非数字页码
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为默认页码
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 应使用默认页大小
                assertTrue(responseJson.get("models").size() <= 10);
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("B. 分页大小参数测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PageSizeTests {

        @Test
        @Order(10)
        @DisplayName("TC_PROCESS_DATA_API_001_10: 边界值 - pageSize=0")
        public void testPageSizeZero() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "0"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理无效页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为默认页大小
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 应使用默认页大小
                assertTrue(responseJson.get("models").size() <= 10);
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(11)
        @DisplayName("TC_PROCESS_DATA_API_001_11: 边界值 - pageSize=-1")
        public void testPageSizeNegative() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "-1"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理负数页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为默认页大小
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 应使用默认页大小
                assertTrue(responseJson.get("models").size() <= 10);
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(12)
        @DisplayName("TC_PROCESS_DATA_API_001_12: 边界值 - pageSize=51")
        public void testPageSizeExceedMax() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "51"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理超过最大值的页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为最大允许值
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 返回记录数应不超过最大允许值
                assertTrue(responseJson.get("models").size() <= 50, "超出最大页大小时应返回不超过50条记录");
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(13)
        @DisplayName("TC_PROCESS_DATA_API_001_13: 边界值 - pageSize=1000")
        public void testExtremelyLargePageSize() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "1000"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理极大页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为最大允许值
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 返回记录数应不超过最大允许值
                assertTrue(responseJson.get("models").size() <= 50, "极大页大小应限制为不超过50条记录");
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(14)
        @DisplayName("TC_PROCESS_DATA_API_001_14: 边界值 - 小数页大小")
        public void testDecimalPageSize() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "10.5"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理小数页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为整数值
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }

        @Test
        @Order(15)
        @DisplayName("TC_PROCESS_DATA_API_001_15: 边界值 - 非数字页大小")
        public void testNonNumericPageSize() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("pageSize", "abc"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否能正确处理非数字页大小
            assertTrue(responseJson.has("success"));

            if (responseJson.get("success").asBoolean()) {
                // 如果自动纠正为默认值
                assertTrue(responseJson.has("models"));
                assertTrue(responseJson.get("models").isArray());
                // 应使用默认页大小
                assertTrue(responseJson.get("models").size() <= 10);
            } else {
                // 如果返回错误
                assertTrue(responseJson.has("message") || responseJson.has("error"));
            }
        }
    }

    @Nested
    @Order(3)
    @DisplayName("C. 搜索参数测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SearchQueryTests {

        @Test
        @Order(16)
        @DisplayName("TC_PROCESS_DATA_API_001_16: 搜索 - 英文关键词")
        public void testSearchEnglish() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "fan"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API响应格式
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));

            // 验证搜索结果包含关键词
            JsonNode models = responseJson.get("models");
            if (!models.isEmpty()) {
                boolean anyMatch = false;
                for (JsonNode model : models) {
                    if (model.get("name").asText().toLowerCase().contains("fan")) {
                        anyMatch = true;
                        break;
                    }
                }
                assertTrue(anyMatch, "搜索结果应包含关键词'fan'");
            }
        }

        @Test
        @Order(17)
        @DisplayName("TC_PROCESS_DATA_API_001_17: 搜索 - 中文关键词")
        public void testSearchChinese() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "涡轮"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API响应格式
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));

            // 验证搜索结果包含关键词
            JsonNode models = responseJson.get("models");
            if (!models.isEmpty()) {
                boolean anyMatch = false;
                for (JsonNode model : models) {
                    if (model.get("name").asText().contains("涡轮")) {
                        anyMatch = true;
                        break;
                    }
                }
                assertTrue(anyMatch, "搜索结果应包含关键词'涡轮'");
            }
        }

        @Test
        @Order(18)
        @DisplayName("TC_PROCESS_DATA_API_001_18: 搜索 - 长字符串")
        public void testSearchLongString() throws Exception {
            String longSearchQuery = "非常长的搜索字符串包含多个词语用于测试系统对长文本的处理能力";

            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", longSearchQuery))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API能够处理长搜索字符串
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));
        }

        @Test
        @Order(19)
        @DisplayName("TC_PROCESS_DATA_API_001_19: 搜索 - 特殊字符")
        public void testSearchSpecialChars() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "特殊字符!@#$%"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API能够处理特殊字符
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));
        }

        @Test
        @Order(20)
        @DisplayName("TC_PROCESS_DATA_API_001_20: 搜索 - 空字符串")
        public void testSearchEmpty() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", ""))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证空搜索返回所有记录
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("total"));
            assertTrue(responseJson.has("models"));

            // 确认空搜索与不提供搜索参数的结果一致
            MvcResult defaultResult = mockMvc.perform(getRequestWithToken())
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode defaultJson = objectMapper.readTree(defaultResult.getResponse().getContentAsString());
            assertEquals(defaultJson.get("total").asInt(), responseJson.get("total").asInt(),
                    "空搜索应返回与默认搜索相同数量的记录");
        }
    }

    @Nested
    @Order(4)
    @DisplayName("D. 安全性测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SecurityTests {

        @Test
        @Order(21)
        @DisplayName("TC_PROCESS_DATA_API_001_21: 安全 - SQL注入")
        public void testSQLInjection() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "' OR 1=1--"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否防御SQL注入
            assertTrue(responseJson.has("success"));

            // 如果SQL注入防御成功，应当不会返回所有记录
            MvcResult defaultResult = mockMvc.perform(getRequestWithToken())
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode defaultJson = objectMapper.readTree(defaultResult.getResponse().getContentAsString());

            // 如果系统有足够的数据记录，注入搜索应返回的记录数应少于总记录数
            if (defaultJson.get("total").asInt() > 10) {
                assertNotEquals(defaultJson.get("total").asInt(), responseJson.get("models").size(),
                        "SQL注入防御应该确保不会返回所有记录");
            }
        }

        @Test
        @Order(22)
        @DisplayName("TC_PROCESS_DATA_API_001_22: 安全 - XSS攻击")
        public void testXSSAttack() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "<script>alert('XSS')</script>"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否防御XSS攻击
            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());

            // 确保响应中不包含未转义的脚本标签
            String responseBody = result.getResponse().getContentAsString();
            assertFalse(responseBody.contains("<script>"), "响应不应包含未转义的脚本标签");
        }

        @Test
        @Order(23)
        @DisplayName("TC_PROCESS_DATA_API_001_23: 安全 - 破坏性SQL注入")
        public void testDestructiveSQLInjection() throws Exception {
            MvcResult result = mockMvc.perform(getRequestWithToken()
                            .param("searchQuery", "1; DROP TABLE users;"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            // 验证API是否防御破坏性SQL注入
            assertTrue(responseJson.has("success"));

            // 如果API成功防御攻击，后续请求应能正常响应
            MvcResult followupResult = mockMvc.perform(getRequestWithToken())
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode followupJson = objectMapper.readTree(followupResult.getResponse().getContentAsString());
            assertTrue(followupJson.get("success").asBoolean(), "破坏性SQL注入后系统应能正常运行");
        }
    }


}