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

        // 导出 openapi.json 供前端生成类型
        try {
            java.io.File dump = new java.io.File("openapi.json");
            java.nio.file.Files.writeString(dump.toPath(), json);
            System.out.println("[OpenApiExportTest] dumped openapi.json to " + dump.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[OpenApiExportTest] dump failed: " + e);
        }

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

        // @Schema 字段级描述（核心 DTO）
        JsonNode schemas = doc.path("components").path("schemas");
        JsonNode userDto = schemas.path("UserDTO");
        assertFalse(userDto.isMissingNode(), "应存在 UserDTO schema");
        JsonNode usernameDesc = userDto.path("properties").path("username").path("description");
        assertFalse(usernameDesc.isMissingNode(), "UserDTO.username 应有 @Schema 描述");
        System.out.println("[OpenApiExportTest] UserDTO.username desc=" + usernameDesc.asText());
        JsonNode loginReq = schemas.path("LoginRequest");
        assertFalse(loginReq.isMissingNode(), "应存在 LoginRequest schema");
        JsonNode pwdDesc = loginReq.path("properties").path("password").path("description");
        assertFalse(pwdDesc.isMissingNode(), "LoginRequest.password 应有 @Schema 描述");
        System.out.println("[OpenApiExportTest] LoginRequest.password desc=" + pwdDesc.asText());

        // 非核心 DTO 字段级 @Schema 也生效（以 AuditLogDTO 为代表）
        JsonNode auditDto = schemas.path("AuditLogDTO");
        assertFalse(auditDto.isMissingNode(), "应存在 AuditLogDTO schema");
        JsonNode eventIdDesc = auditDto.path("properties").path("eventId").path("description");
        assertFalse(eventIdDesc.isMissingNode(), "AuditLogDTO.eventId 应有 @Schema 描述");
        System.out.println("[OpenApiExportTest] AuditLogDTO.eventId desc=" + eventIdDesc.asText());

        // ErrorCodes 错误码文档化：统一响应 errorCode 字段富描述
        boolean errorCodeDoc = false;
        for (var sit = schemas.fields(); sit.hasNext(); ) {
            var se = sit.next();
            if (se.getKey().startsWith("ApiResponse")) {
                JsonNode ec = se.getValue().path("properties").path("errorCode").path("description");
                if (!ec.isMissingNode() && ec.asText().contains("错误码")) {
                    errorCodeDoc = true;
                    String head = ec.asText().length() > 40 ? ec.asText().substring(0, 40) : ec.asText();
                    System.out.println("[OpenApiExportTest] ApiResponse.errorCode desc head=" + head);
                    break;
                }
            }
        }
        assertTrue(errorCodeDoc, "ApiResponse.errorCode 应文档化 ErrorCodes 错误码体系");

        // 分页/排序参数文档化：PageQuery 基类字段进入查询 DTO schema
        JsonNode userQuery = schemas.path("UserQuery");
        assertFalse(userQuery.isMissingNode(), "应存在 UserQuery schema");
        JsonNode pageDesc = userQuery.path("properties").path("page").path("description");
        assertFalse(pageDesc.isMissingNode(), "UserQuery.page 应有 @Schema 描述（继承自 PageQuery）");
        System.out.println("[OpenApiExportTest] UserQuery.page desc=" + pageDesc.asText());

        // 请求参数级 @Parameter（路径变量 id）
        boolean idParamDoc = false;
        for (var pit = paths.fields(); pit.hasNext(); ) {
            var pe = pit.next();
            if (pe.getKey().contains("/file/download/")) {
                JsonNode params = pe.getValue().path("get").path("parameters");
                for (var paramIt = params.elements(); paramIt.hasNext(); ) {
                    JsonNode p = paramIt.next();
                    if ("id".equals(p.path("name").asText()) && !p.path("description").isMissingNode()) {
                        idParamDoc = true;
                        System.out.println("[OpenApiExportTest] /file/download/{id} id param desc=" + p.path("description").asText());
                        break;
                    }
                }
                break;
            }
        }
        assertTrue(idParamDoc, "FileController.download 的 id 路径变量应文档化");

        // 枚举类文档化：枚举取值与 @Schema 描述应出现在引用属性处
        JsonNode userStatusSchema = findEnumSchema(schemas,
                java.util.Set.of("ACTIVE", "DISABLED", "LOCKED"), "正常");
        System.out.println("[OpenApiExportTest] UserStatus resolved=" + userStatusSchema);
        assertFalse(userStatusSchema == null, "UserStatus 枚举应出现在文档中");
        assertTrue(containsValue(userStatusSchema.path("enum"), "ACTIVE"), "UserStatus 枚举取值应出现在文档中");
        JsonNode usDesc = userStatusSchema.path("description");
        assertTrue(usDesc.asText().contains("正常"), "UserStatus @Schema 描述应包含取值含义（正常）");
        System.out.println("[OpenApiExportTest] UserStatus desc=" + usDesc.asText());

        JsonNode tenantStatusSchema = findEnumSchema(schemas,
                java.util.Set.of("ACTIVE", "SUSPENDED", "LOCKED", "MIGRATING", "ARCHIVED"), "启用");
        System.out.println("[OpenApiExportTest] TenantStatus resolved=" + tenantStatusSchema);
        assertFalse(tenantStatusSchema == null, "TenantStatus 枚举应出现在文档中");
        assertTrue(containsValue(tenantStatusSchema.path("enum"), "SUSPENDED"), "TenantStatus 枚举取值应出现在文档中");
        JsonNode tsDesc = tenantStatusSchema.path("description");
        assertTrue(tsDesc.asText().contains("启用"), "TenantStatus @Schema 描述应包含取值含义（启用）");
        System.out.println("[OpenApiExportTest] TenantStatus desc=" + tsDesc.asText());

        // 其余枚举（TenantType）同样应以其类级 @Schema 描述呈现
        JsonNode tenantTypeSchema = findEnumSchema(schemas,
                java.util.Set.of("ORGANIZATION", "PROJECT", "EXTERNAL", "PLATFORM"), "组织型");
        System.out.println("[OpenApiExportTest] TenantType resolved=" + tenantTypeSchema);
        assertFalse(tenantTypeSchema == null, "TenantType 枚举应出现在文档中");
        assertTrue(tenantTypeSchema.path("description").asText().contains("组织型"), "TenantType @Schema 描述应包含取值含义（组织型）");

        // 关键接口 200 响应类型化：移除 @ApiResponse 注解后由返回类型自动推导
        JsonNode loginOp = paths.path("/api/auth/login").path("post");
        assertFalse(loginOp.isMissingNode(), "应存在登录接口");
        String login200Ref = response200Ref(loginOp);
        assertTrue(login200Ref.endsWith("/ApiResponseLoginResult"),
                "登录 200 应引用 ApiResponseLoginResult（实际=" + login200Ref + "）");
        JsonNode loginData = schemas.path("ApiResponseLoginResult").path("properties").path("data");
        assertTrue(loginData.path("$ref").asText("").endsWith("/LoginResult"),
                "ApiResponseLoginResult.data 应引用 LoginResult");
        assertFalse(schemas.path("LoginResult").path("properties").path("token").isMissingNode(),
                "LoginResult 应含 token 字段");
        assertTrue(response200Ref(paths.path("/admin/user/list").path("post")).endsWith("/ApiResponsePageResultUserDTO"),
                "user/list 200 应引用 ApiResponsePageResultUserDTO");
        assertTrue(response200Ref(paths.path("/admin/tenant/get").path("post")).endsWith("/ApiResponseTenantDTO"),
                "tenant/get 200 应引用 ApiResponseTenantDTO");
        System.out.println("[OpenApiExportTest] login 200 typed=" + login200Ref);

        System.out.println("[OpenApiExportTest] paths=" + paths.size()
                + ", tags=" + tags.size()
                + ", opsWithSummary=" + withSummary + ", publicEndpoints=" + publicEndpoints);
    }

    private static boolean containsValue(JsonNode array, String value) {
        if (!array.isArray()) {
            return false;
        }
        for (JsonNode n : array) {
            if (value.equals(n.asText())) {
                return true;
            }
        }
        return false;
    }

    private static JsonNode resolveSchema(JsonNode schemas, JsonNode property) {
        if (property != null && property.has("$ref")) {
            String ref = property.get("$ref").asText();
            String name = ref.substring(ref.lastIndexOf('/') + 1);
            return schemas.path(name);
        }
        return property;
    }

    private static JsonNode findEnumSchema(JsonNode schemas, java.util.Set<String> values, String descFragment) {
        for (var it = schemas.fields(); it.hasNext(); ) {
            var e = it.next();
            JsonNode props = e.getValue().path("properties");
            if (props.isMissingNode()) {
                continue;
            }
            for (var pit = props.fields(); pit.hasNext(); ) {
                JsonNode resolved = resolveSchema(schemas, pit.next().getValue());
                JsonNode en = resolved.path("enum");
                if (en.isArray() && containsAll(en, values)
                        && !resolved.path("description").isMissingNode()
                        && resolved.path("description").asText().contains(descFragment)) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private static boolean containsAll(JsonNode array, java.util.Set<String> values) {
        if (!array.isArray()) {
            return false;
        }
        java.util.Set<String> present = new java.util.HashSet<>();
        for (JsonNode n : array) {
            present.add(n.asText());
        }
        return present.containsAll(values);
    }

    private static boolean treeContainsText(JsonNode node, String text) {
        if (node == null || node.isMissingNode()) {
            return false;
        }
        if (node.isTextual()) {
            return node.asText().contains(text);
        }
        if (node.isObject()) {
            for (JsonNode v : node) {
                if (treeContainsText(v, text)) {
                    return true;
                }
            }
            return false;
        }
        if (node.isArray()) {
            for (JsonNode v : node) {
                if (treeContainsText(v, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String response200Ref(JsonNode op) {
        JsonNode content = op.path("responses").path("200").path("content");
        for (var it = content.fields(); it.hasNext(); ) {
            JsonNode sch = it.next().getValue().path("schema");
            String ref = sch.path("$ref").asText("");
            if (!ref.isEmpty()) {
                return ref;
            }
        }
        return "";
    }
}
