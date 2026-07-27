package com.df4j.xctec.xcms.datapermission.mask;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * HTTP 出口列级脱敏（AT-18）：替代旧的 ColumnMaskResponseBodyAdvice。
 *
 * <p>差异：不再依赖 Controller 上的 @MaskResource 标注与 ApiResponse 包装类型 ——
 * 由 DTO 字段的 @MaskField 注解驱动，任意返回类型（含裸 DTO / List / ApiResponse）
 * 均生效；无 @MaskField 字段的类型经元数据缓存快速跳过，开销可忽略。</p>
 */
@RestControllerAdvice
public class MaskFieldResponseAdvice implements ResponseBodyAdvice<Object> {

    private final MaskFieldPostProcessor postProcessor;

    public MaskFieldResponseAdvice(MaskFieldPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 兼容 ApiResponse 包装，但不再与之耦合：任意返回类型均处理
        Object payload = body instanceof ApiResponse<?> wrapper ? wrapper.getData() : body;
        if (payload != null && postProcessor.hasMaskFields(payload)) {
            postProcessor.mask(payload);
        }
        return body;
    }
}
