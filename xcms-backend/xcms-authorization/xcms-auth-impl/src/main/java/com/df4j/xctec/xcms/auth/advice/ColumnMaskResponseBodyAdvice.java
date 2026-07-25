package com.df4j.xctec.xcms.auth.advice;

import com.df4j.xctec.xcms.auth.api.DataPermissionService;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.MaskResource;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

/**
 * 列级脱敏自动执行链路（修复问题 3）。
 * 当 Controller 方法或类标注了 {@link MaskResource} 时，在响应写出前对返回数据执行脱敏。
 * resourceType 取自注解的 value。
 */
@Slf4j
@ControllerAdvice
public class ColumnMaskResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired(required = false)
    private DataPermissionService dataPermissionService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (dataPermissionService == null) {
            return false;
        }
        return returnType.getExecutable().isAnnotationPresent(MaskResource.class)
                || returnType.getDeclaringClass().isAnnotationPresent(MaskResource.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return body;
        }
        MaskResource mask = returnType.getExecutable().getAnnotation(MaskResource.class);
        if (mask == null) {
            mask = returnType.getDeclaringClass().getAnnotation(MaskResource.class);
        }
        if (mask == null) {
            return body;
        }
        Long userId = TenantContext.getCurrentUserId();
        try {
            if (body instanceof ApiResponse<?> apiResponse) {
                Object data = apiResponse.getData();
                if (data instanceof List<?> list) {
                    dataPermissionService.applyColumnMask((List<Object>) list, userId, mask.value());
                } else if (data != null) {
                    dataPermissionService.applyColumnMask(data, userId, mask.value());
                }
            }
        } catch (Exception e) {
            log.warn("列级脱敏执行失败，resourceType={}", mask.value(), e);
        }
        return body;
    }
}
