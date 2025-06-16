package com.example.software_management.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class SimulationApiBlackBoxTests {

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

    // 通用方法：检查错误消息或响应内容
    private void checkErrorMessageOrContent(MvcResult result, String... expectedSubstrings) throws Exception {
        // 首先检查错误消息
        String errorMessage = result.getResponse().getErrorMessage();
        String responseContent = result.getResponse().getContentAsString();

        boolean hasMatch = false;

        // 检查错误消息
        if (errorMessage != null && !errorMessage.isEmpty()) {
            String lowerCaseErrorMessage = errorMessage.toLowerCase();
            for (String expectedSubstring : expectedSubstrings) {
                if (lowerCaseErrorMessage.contains(expectedSubstring.toLowerCase())) {
                    hasMatch = true;
                    break;
                }
            }
        }

        // 如果错误消息中没有找到预期字符串，尝试检查响应内容
        if (!hasMatch && !responseContent.isEmpty()) {
            try {
                // 尝试作为JSON解析
                JsonNode responseJson = objectMapper.readTree(responseContent);
                String jsonStr = responseJson.toString().toLowerCase();
                for (String expectedSubstring : expectedSubstrings) {
                    if (jsonStr.contains(expectedSubstring.toLowerCase())) {
                        hasMatch = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // 如果不是JSON，直接检查文本
                String lowerCaseContent = responseContent.toLowerCase();
                for (String expectedSubstring : expectedSubstrings) {
                    if (lowerCaseContent.contains(expectedSubstring.toLowerCase())) {
                        hasMatch = true;
                        break;
                    }
                }
            }
        }

        if (!hasMatch) {
            fail("错误消息或响应内容中没有找到预期的子字符串: " + String.join(", ", expectedSubstrings) +
                    "\n错误消息: " + errorMessage +
                    "\n响应内容: " + responseContent);
        }
    }

    @Nested
    @Order(1)
    @DisplayName("A. 参数验证测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ParameterValidationTests {

        // 构建带有文件的multipart请求
        private MockMultipartHttpServletRequestBuilder createMultipartRequest() {
            return (MockMultipartHttpServletRequestBuilder) multipart("/api/simulation/getSimulationResult")
                    .header("Authorization", "Bearer " + token);
        }

        // 创建CSV格式的测试文件
        private MockMultipartFile createCsvFile(String filename, String content) {
            return new MockMultipartFile(
                    "file",
                    filename,
                    "text/csv",
                    content.getBytes(StandardCharsets.UTF_8)
            );
        }

        private static final String VALID_ONE_ROW_CSV_CONTENT =
                "hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.95,1000,0.1,520,50,1200,2000,0.2\n";

        @Test
        @Order(1)
        @DisplayName("TC_SIM_API_001_01: 有效标准数据文件ID + 有效deviceId + 有效CSV文件(一行数据)")
        public void testValidParameters() throws Exception {
            MockMultipartFile file = createCsvFile("valid_one_row.csv", VALID_ONE_ROW_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isOk())  // 明确验证状态码为200 OK
                    .andReturn();

            // 验证状态码
            assertEquals(200, result.getResponse().getStatus(), "应返回状态码200 OK");

            JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

            assertTrue(responseJson.has("success"));
            assertTrue(responseJson.get("success").asBoolean());
            assertTrue(responseJson.has("imageUrl"));
            assertTrue(responseJson.has("damageLocation"));
            assertTrue(responseJson.has("lifespan"));
            assertTrue(responseJson.has("healthIndex"));

            // 验证数值范围合理性
            assertTrue(responseJson.get("lifespan").asInt() > 0);
            assertTrue(responseJson.get("healthIndex").asInt() >= 0 &&
                    responseJson.get("healthIndex").asInt() <= 100);
        }

        @Test
        @Order(2)
        @DisplayName("TC_SIM_API_001_02: 未提供标准数据文件ID + 有效deviceId + 有效CSV文件(一行数据)")
        public void testMissingStandardDataId() throws Exception {
            MockMultipartFile file = createCsvFile("valid_one_row.csv", VALID_ONE_ROW_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "standardDataId", "standard data", "parameter");
        }

        @Test
        @Order(3)
        @DisplayName("TC_SIM_API_001_03: 无效标准数据文件ID + 有效deviceId + 有效CSV文件(一行数据)")
        public void testInvalidStandardDataId() throws Exception {
            MockMultipartFile file = createCsvFile("valid_one_row.csv", VALID_ONE_ROW_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "999")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为404 Not Found
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "not found", "not exist", "standard data");
        }

        @Test
        @Order(4)
        @DisplayName("TC_SIM_API_001_04: 有效标准数据文件ID + 未提供deviceId + 有效CSV文件(一行数据)")
        public void testMissingDeviceId() throws Exception {
            MockMultipartFile file = createCsvFile("valid_one_row.csv", VALID_ONE_ROW_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "deviceId", "device id", "parameter");
        }

        @Test
        @Order(5)
        @DisplayName("TC_SIM_API_001_05: 有效标准数据文件ID + 无效deviceId + 有效CSV文件(一行数据)")
        public void testInvalidDeviceId() throws Exception {
            MockMultipartFile file = createCsvFile("valid_one_row.csv", VALID_ONE_ROW_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "999"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为404 Not Found
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "not found", "not exist", "device");
        }

        @Test
        @Order(6)
        @DisplayName("TC_SIM_API_001_06: 有效标准数据文件ID + 有效deviceId + 未提供文件")
        public void testMissingFile() throws Exception {
            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "file", "missing");
        }
    }

    @Nested
    @Order(2)
    @DisplayName("B. 文件格式测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FileFormatTests {

        // 构建带有文件的multipart请求
        private MockMultipartHttpServletRequestBuilder createMultipartRequest() {
            return (MockMultipartHttpServletRequestBuilder) multipart("/api/simulation/getSimulationResult")
                    .header("Authorization", "Bearer " + token);
        }

        // 创建CSV格式的测试文件
        private MockMultipartFile createCsvFile(String filename, String content) {
            return new MockMultipartFile(
                    "file",
                    filename,
                    "text/csv",
                    content.getBytes(StandardCharsets.UTF_8)
            );
        }

        // 创建非CSV格式的测试文件
        private MockMultipartFile createNonCsvFile(String extension, String content) {
            return new MockMultipartFile(
                    "file",
                    "invalid_format" + "." + extension,
                    "application/" + extension,
                    content.getBytes(StandardCharsets.UTF_8)
            );
        }

        // 创建空文件
        private MockMultipartFile createEmptyFile() {
            return new MockMultipartFile(
                    "file",
                    "empty.csv",
                    "text/csv",
                    new byte[0]
            );
        }

        private static final String MISSING_COLUMN_CSV_CONTENT =
                "hpt_eff_mod,nf,smfan,t24,wf,nc,smhpc\n" +  // 缺少t48列
                        "0.95,1000,0.1,520,50,2000,0.2\n";

        private static final String INVALID_FORMAT_CSV_CONTENT =
                "hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.95,not_a_number,0.1,520,50,1200,2000,0.2\n";

        private static final String HEADER_ONLY_CSV_CONTENT =
                "hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n";

        @Test
        @Order(1)
        @DisplayName("TC_SIM_API_001_07: 有效标准数据文件ID + 有效deviceId + 无效格式文件(txt)")
        public void testInvalidFileFormatTxt() throws Exception {
            MockMultipartFile file = createNonCsvFile("txt",
                    "This is a text file, not a CSV file.");

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "format", "csv", "invalid");
        }

        @Test
        @Order(2)
        @DisplayName("TC_SIM_API_001_08: 有效标准数据文件ID + 有效deviceId + 无效格式文件(xlsx)")
        public void testInvalidFileFormatXlsx() throws Exception {
            MockMultipartFile file = createNonCsvFile("xlsx",
                    "This content simulates an Excel file");

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "format", "csv", "invalid");
        }

        @Test
        @Order(3)
        @DisplayName("TC_SIM_API_001_09: 有效标准数据文件ID + 有效deviceId + 内容不符文件(缺列)")
        public void testMissingColumns() throws Exception {
            MockMultipartFile file = createCsvFile("missing_column.csv", MISSING_COLUMN_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "column", "missing", "t48");
        }

        @Test
        @Order(4)
        @DisplayName("TC_SIM_API_001_10: 有效标准数据文件ID + 有效deviceId + 内容不符文件(格式错)")
        public void testInvalidDataFormat() throws Exception {
            MockMultipartFile file = createCsvFile("invalid_data_format.csv", INVALID_FORMAT_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "format", "invalid", "data");
        }

        @Test
        @Order(5)
        @DisplayName("TC_SIM_API_001_13: 有效标准数据文件ID + 有效deviceId + 空文件(0字节)")
        public void testEmptyFile() throws Exception {
            MockMultipartFile file = createEmptyFile();

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "empty", "file");
        }

        @Test
        @Order(6)
        @DisplayName("TC_SIM_API_001_14: 有效标准数据文件ID + 有效deviceId + 空文件(只有表头)")
        public void testHeaderOnlyFile() throws Exception {
            MockMultipartFile file = createCsvFile("header_only.csv", HEADER_ONLY_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "insufficient", "data", "empty", "header");
        }
    }

    @Nested
    @Order(3)
    @DisplayName("C. 文件内容测试")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FileContentTests {

        // 构建带有文件的multipart请求
        private MockMultipartHttpServletRequestBuilder createMultipartRequest() {
            return (MockMultipartHttpServletRequestBuilder) multipart("/api/simulation/getSimulationResult")
                    .header("Authorization", "Bearer " + token);
        }

        // 创建CSV格式的测试文件
        private MockMultipartFile createCsvFile(String filename, String content) {
            return new MockMultipartFile(
                    "file",
                    filename,
                    "text/csv",
                    content.getBytes(StandardCharsets.UTF_8)
            );
        }

        // 创建超大文件
        private MockMultipartFile createLargeFile() {
            // 创建一个17MB的文件内容
            StringBuilder sb = new StringBuilder("hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n");
            // 添加一行长数据，通过重复填充使文件变大
            StringBuilder dataLine = new StringBuilder();
            for (int i = 0; i < 2000000; i++) {
                dataLine.append("0.95,1000,0.1,520,50,1200,2000,0.2,");
            }
            dataLine.append("0.95,1000,0.1,520,50,1200,2000,0.2\n");
            sb.append(dataLine);

            return new MockMultipartFile(
                    "file",
                    "large_file.csv",
                    "text/csv",
                    sb.toString().getBytes(StandardCharsets.UTF_8)
            );
        }

        private static final String MULTIPLE_ROWS_CSV_CONTENT =
                "hpt_eff_mod,nf,smfan,t24,wf,t48,nc,smhpc\n" +
                        "0.95,1000,0.1,520,50,1200,2000,0.2\n" +
                        "0.96,1010,0.11,525,51,1210,2010,0.21\n" +
                        "0.97,1020,0.12,530,52,1220,2020,0.22\n";

        @Test
        @Order(1)
        @DisplayName("TC_SIM_API_001_11: 有效标准数据文件ID + 有效deviceId + 包含多行数据的CSV")
        public void testMultipleRowsData() throws Exception {
            MockMultipartFile file = createCsvFile("multiple_rows.csv", MULTIPLE_ROWS_CSV_CONTENT);

            MvcResult result = mockMvc.perform(createMultipartRequest()
                            .file(file)
                            .param("standardDataId", "1")
                            .param("deviceId", "1"))
                    .andExpect(status().isBadRequest())  // 明确验证状态码为400 Bad Request
                    .andReturn();

            // 验证状态码
            assertEquals(400, result.getResponse().getStatus(), "应返回状态码400 Bad Request");

            // 检查错误消息是否包含预期的关键词
            checkErrorMessageOrContent(result, "one", "multiple", "row");
        }

        @Test
        @Order(2)
        @DisplayName("TC_SIM_API_001_12: 有效标准数据文件ID + 有效deviceId + 超大文件")
        public void testOversizedFile() throws Exception {
            MockMultipartFile file = createLargeFile();

            try {
                MvcResult result = mockMvc.perform(createMultipartRequest()
                                .file(file)
                                .param("standardDataId", "1")
                                .param("deviceId", "1"))
                        .andExpect(status().isPayloadTooLarge())  // 明确验证状态码为413 Payload Too Large
                        .andReturn();

                // 验证状态码
                assertEquals(413, result.getResponse().getStatus(), "应返回状态码413 Payload Too Large");

                // 检查错误消息是否包含预期的关键词
                checkErrorMessageOrContent(result, "large", "size", "exceed");
            } catch (Exception e) {
                // 某些Spring配置可能会在客户端发送前就拒绝超大请求
                // 如果出现异常，我们认为测试通过，因为它正确地拒绝了超大文件
                assertTrue(e.getMessage().contains("413") ||
                        e.getMessage().contains("large") ||
                        e.getMessage().contains("size"));
            }
        }
    }
}