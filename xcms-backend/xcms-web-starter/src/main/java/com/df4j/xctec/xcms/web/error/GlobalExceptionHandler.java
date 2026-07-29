package com.df4j.xctec.xcms.web.error;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.kernel.exception.NotFoundException;
import com.df4j.xctec.xcms.kernel.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理（复盘 H1 + 评审 P0-3）。
 *
 * <p>此前仅有 {@code ColumnMaskResponseBodyAdvice} 做脱敏，无统一异常处理：
 * {@link BusinessException}/{@link MethodArgumentNotValidException}/未捕获异常会落到
 * Spring 默认错误页（Whitelabel），前端按 {@code errorCode} 解析失败。</p>
 *
 * <p>本处理器将异常统一包装为 {@link ApiResponse} 结构，与
 * {@link com.df4j.xctec.xcms.web.security.RestSecurityHandlers}（401/403）保持一致的输出契约。</p>
 *
 * <ul>
 *   <li>{@link PermissionException} → 403</li>
 *   <li>{@link NotFoundException} → 404</li>
 *   <li>{@link BusinessException} → 按 errorCode 映射 HTTP 状态（3 位码直接映射，业务码默认 400）</li>
 *   <li>{@link MethodArgumentNotValidException} → 422（携带字段级错误明细）</li>
 *   <li>{@link HttpMessageNotReadableException}（请求体解析失败）→ 400</li>
 *   <li>未捕获异常 → 500（记录错误日志）</li>
 * </ul>
 *
 * <p>注意：Filter 层的 401/403 由 SecurityFilterChain 的 {@code AuthenticationEntryPoint}/
 * {@code AccessDeniedHandler} 在 Servlet Filter 层处理（早于 @Controller 执行）；
 * 而声明式鉴权 {@code @PreAuthorize} 抛出的 {@link AccessDeniedException} 发生于
 * @Controller 执行期（晚于 Filter 链），未被上述 Handler 拦截，故此处单独处理为 403，
 * 避免冒泡到未捕获异常 → 500。</p>
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handlePermission(PermissionException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * 声明式鉴权（@PreAuthorize）在 Controller 执行期抛出 AccessDeniedException，
     * 该异常晚于 SecurityFilterChain 的 AccessDeniedHandler（仅处理 Filter 层拒绝），
     * 会冒泡到此处，需统一映射为 403，否则落入未捕获异常 → 500。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(ErrorCodes.PERMISSION_DENIED, "无访问权限", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), httpStatusFor(ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        String msg = "参数校验失败" + (detail.isEmpty() ? "" : " - " + detail);
        return build(ErrorCodes.VALIDATION_ERROR, msg, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(ErrorCodes.BAD_REQUEST, "请求体格式错误", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("未处理异常", ex);
        return build(ErrorCodes.INTERNAL_ERROR, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> build(String code, String msg, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.error(code, msg));
    }

    /**
     * 将 errorCode 映射为 HTTP 状态：3 位客户端/服务端码（400~599）直接映射，
     * 业务扩展码（如 1001/1203/4000）无对应标准状态，统一回退 400。
     */
    private HttpStatus httpStatusFor(String errorCode) {
        if (errorCode != null && errorCode.matches("\\d{3}")) {
            int code = Integer.parseInt(errorCode);
            if (code >= 400 && code < 600) {
                return HttpStatus.valueOf(code);
            }
        }
        return HttpStatus.BAD_REQUEST;
    }
}
