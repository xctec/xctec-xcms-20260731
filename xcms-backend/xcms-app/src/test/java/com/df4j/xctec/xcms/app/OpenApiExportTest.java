package com.df4j.xctec.xcms.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 springdoc 注解已生效：实际导出的 /v3/api-docs 端点包含 tags、operation summary、
 * Bearer 安全方案，且公开端点（登录/刷新/SSO 回调）豁免全局 Bearer 要求。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiExportTest {

    @LocalServerPort
    private int port;

    @Test
    void openApiReflectsAnnotations() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject("http://localhost:" + port + "/v3/api-docs", String.class);
        ObjectMapper om = new ObjectMapper();
        JsonNode doc = om.readValue(json, JsonNode.class);

        // 安全方案
        JsonNode schemes = doc.path("components").path("securitySchemes");
        assertFalse(schemes.isEmpty(), "应声明 Bearer 安全方案");
        assertTrue(schemes.has("Bearer"));

        // @Tag 分组
        JsonNode tags = doc.path("tags");
        assertFalse(tags.isEmpty(), "应至少存在一个 @Tag 分组");

        // operation summary + 公开端点豁免
        JsonNode paths = doc.path("paths");
        int total = 0, withSummary = 0, publicEndpoints = 0;
        for (var it = paths.fields(); it.hasNext(); ) {
            var pe = it.next();
            for (var mit = pe.getValue().fields(); mit.hasNext(); ) {
                var me = mit.next();
                String m = me.getKey();
                if (!(m.equals("get") || m.equals("post") || m.equals("put") || m.equals("delete") || m.equals("patch"))) {
                    continue;
                }
                JsonNode op = me.getValue();
                total++;
                if (!op.path("summary").isMissingNode() || !op.path("description").isMissingNode()) {
                    withSummary++;
                }
                JsonNode sec = op.get("security");
                if (sec != null && sec.isArray() && sec.isEmpty()) {
                    publicEndpoints++;
                }
            }
        }
        assertTrue(withSummary > 0, "应至少存在一个带 summary 的 operation");
        assertTrue(publicEndpoints >= 3, "登录/刷新/SSO 回调等公开端点应豁免 Bearer（实际=" + publicEndpoints + "）");
        System.out.println("[OpenApiExportTest] paths=" + paths.size()
                + ", tags=" + tags.size()
                + ", opsWithSummary=" + withSummary + ", publicEndpoints=" + publicEndpoints);
    }
}
