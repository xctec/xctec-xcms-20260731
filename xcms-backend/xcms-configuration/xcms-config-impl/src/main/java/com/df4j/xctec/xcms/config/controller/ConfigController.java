package com.df4j.xctec.xcms.config.controller;

import com.df4j.xctec.xcms.config.api.ConfigService;
import com.df4j.xctec.xcms.config.api.dto.CodeRuleDTO;
import com.df4j.xctec.xcms.config.api.dto.ConfigSwitchDTO;
import com.df4j.xctec.xcms.config.api.dto.DictItemDTO;
import com.df4j.xctec.xcms.config.api.dto.DictTypeDTO;
import com.df4j.xctec.xcms.config.api.dto.request.CodeRuleCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigSwitchCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigValueRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemQueryRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictTypeCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.NextCodeRequest;
import com.df4j.xctec.xcms.config.api.dto.request.SetSwitchRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/config")
@Tag(name = "配置 Config", description = "管理面：字典/编码规则/配置开关")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "查询字典项列表")
    @PostMapping("/dict-items")
    public ApiResponse<List<DictItemDTO>> dictItems(@RequestBody DictItemQueryRequest request) {
        List<DictItemDTO> items = configService.getDictItems(request.getDictCode());
        return ApiResponse.success(items);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回字典项", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"itemCode\":\"male\",\"itemName\":\"男\",\"itemValue\":\"1\",\"dictType\":\"gender\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "字典项不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"字典项不存在\",\"data\":null}")))
    @Operation(summary = "查询单个字典项")
    @PostMapping("/dict-item")
    public ApiResponse<DictItemDTO> dictItem(@RequestBody DictItemQueryRequest request) {
        return ApiResponse.success(configService.getDictItem(request.getDictCode(), request.getItemCode()));
    }

    @Operation(summary = "查询配置开关是否开启")
    @PostMapping("/switch-on")
    public ApiResponse<Boolean> switchOn(@RequestBody ConfigValueRequest request) {
        return ApiResponse.success(configService.isSwitchOn(request.getKey()));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回配置值", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":\"XCMS\"}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "配置项不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"配置项不存在\",\"data\":null}")))
    @Operation(summary = "查询配置值")
    @PostMapping("/config-value")
    public ApiResponse<String> configValue(@RequestBody ConfigValueRequest request) {
        return ApiResponse.success(configService.getConfigValue(request.getKey()));
    }

    @Operation(summary = "生成下一个编码", description = "按编码规则生成下一个业务编码")
    @PostMapping("/next-code")
    public ApiResponse<String> nextCode(@RequestBody NextCodeRequest request) {
        return ApiResponse.success(configService.nextCode(request));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功，返回新字典类型", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1,\"dictType\":\"gender\",\"dictName\":\"性别\",\"status\":\"ENABLED\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败（字典类型重复）", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1000\",\"errorMsg\":\"参数校验失败：字典类型已存在\",\"data\":null}")))
    @Operation(summary = "创建字典类型")
    @PostMapping("/create-dict-type")
    public ApiResponse<DictTypeDTO> createDictType(@RequestBody DictTypeCreateRequest request) {
        return ApiResponse.success(configService.createDictType(request));
    }

    @Operation(summary = "创建字典项")
    @PostMapping("/create-dict-item")
    public ApiResponse<DictItemDTO> createDictItem(@RequestBody DictItemCreateRequest request) {
        return ApiResponse.success(configService.createDictItem(request));
    }

    @Operation(summary = "创建配置开关")
    @PostMapping("/create-switch")
    public ApiResponse<ConfigSwitchDTO> createSwitch(@RequestBody ConfigSwitchCreateRequest request) {
        return ApiResponse.success(configService.createConfigSwitch(request));
    }

    @Operation(summary = "创建编码规则")
    @PostMapping("/create-code-rule")
    public ApiResponse<CodeRuleDTO> createCodeRule(@RequestBody CodeRuleCreateRequest request) {
        return ApiResponse.success(configService.createCodeRule(request));
    }

    @Operation(summary = "设置配置开关")
    @PostMapping("/set-switch")
    public ApiResponse<Void> setSwitch(@RequestBody SetSwitchRequest request) {
        configService.setSwitch(request);
        return ApiResponse.success(null);
    }
}
