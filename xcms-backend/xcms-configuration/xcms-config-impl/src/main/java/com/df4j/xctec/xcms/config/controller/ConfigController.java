package com.df4j.xctec.xcms.config.controller;

import com.df4j.xctec.xcms.config.api.ConfigService;
import com.df4j.xctec.xcms.config.api.dto.CodeRuleDTO;
import com.df4j.xctec.xcms.config.api.dto.ConfigSwitchDTO;
import com.df4j.xctec.xcms.config.api.dto.DictItemDTO;
import com.df4j.xctec.xcms.config.api.dto.DictTypeDTO;
import com.df4j.xctec.xcms.config.api.dto.request.CodeRuleCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigSwitchCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictItemQueryRequest;
import com.df4j.xctec.xcms.config.api.dto.request.DictTypeCreateRequest;
import com.df4j.xctec.xcms.config.api.dto.request.NextCodeRequest;
import com.df4j.xctec.xcms.config.api.dto.request.ConfigValueRequest;
import com.df4j.xctec.xcms.config.api.dto.request.SetSwitchRequest;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 配置中心（管理面）。遵循全 POST 风格。
 */
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @PostMapping("/dict-items")
    public ApiResponse<List<DictItemDTO>> dictItems(@RequestBody DictItemQueryRequest request) {
        // typeCode 必填时返回该类型下所有项；同时传 itemCode 时返回单个
        if (request.getItemCode() != null && !request.getItemCode().isBlank()) {
            return ApiResponse.success(List.of(configService.getDictItem(request.getTypeCode(), request.getItemCode())));
        }
        return ApiResponse.success(configService.getDictItems(request.getTypeCode()));
    }

    @PostMapping("/switch-on")
    public ApiResponse<Boolean> switchOn(@RequestBody ConfigValueRequest request) {
        return ApiResponse.success(configService.isSwitchOn(request.getKey()));
    }

    @PostMapping("/config-value")
    public ApiResponse<String> configValue(@RequestBody ConfigValueRequest request) {
        return ApiResponse.success(configService.getConfigValue(request.getKey()));
    }

    @PostMapping("/next-code")
    public ApiResponse<String> nextCode(@RequestBody NextCodeRequest request) {
        return ApiResponse.success(configService.nextCode(request.getRuleCode()));
    }

    @PostMapping("/create-dict-type")
    public ApiResponse<DictTypeDTO> createDictType(@RequestBody DictTypeCreateRequest request) {
        return ApiResponse.success(configService.createDictType(request));
    }

    @PostMapping("/create-dict-item")
    public ApiResponse<DictItemDTO> createDictItem(@RequestBody DictItemCreateRequest request) {
        return ApiResponse.success(configService.createDictItem(request));
    }

    @PostMapping("/create-switch")
    public ApiResponse<ConfigSwitchDTO> createSwitch(@RequestBody ConfigSwitchCreateRequest request) {
        return ApiResponse.success(configService.createSwitch(request));
    }

    @PostMapping("/create-code-rule")
    public ApiResponse<CodeRuleDTO> createCodeRule(@RequestBody CodeRuleCreateRequest request) {
        return ApiResponse.success(configService.createCodeRule(request));
    }

    @PostMapping("/set-switch")
    public ApiResponse<Void> setSwitch(@RequestBody SetSwitchRequest request) {
        configService.setSwitch(request.getSwitchKey(), request.isEnabled());
        return ApiResponse.success();
    }
}
