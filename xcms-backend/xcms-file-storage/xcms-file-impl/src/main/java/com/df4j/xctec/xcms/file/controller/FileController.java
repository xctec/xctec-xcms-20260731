package com.df4j.xctec.xcms.file.controller;

import com.df4j.xctec.xcms.file.api.FileStorageService;
import com.df4j.xctec.xcms.file.api.dto.FileDTO;
import com.df4j.xctec.xcms.file.api.dto.FileQuery;
import com.df4j.xctec.xcms.file.api.dto.request.FileUploadCommand;
import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件存储（管理面/业务面）。上传、查询、列表、删除遵循全 POST 风格；
 * 二进制下载为流式场景，单独提供 GET 端点。
 */
@RestController
@RequestMapping("/file")
@Tag(name = "文件存储 File", description = "文件上传/查询/列表/删除/下载")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证或令牌失效", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1201\",\"errorMsg\":\"未认证或令牌已失效，请重新登录\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无访问权限", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1203\",\"errorMsg\":\"无访问该资源的权限\",\"data\":null}")))
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"5000\",\"errorMsg\":\"服务器内部错误，请稍后重试或联系管理员\",\"data\":null}")))
public class FileController {

    private final FileStorageService fileStorageService;

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功，返回文件元信息", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"0\",\"errorMsg\":\"success\",\"data\":{\"id\":1001,\"fileName\":\"report.pdf\",\"size\":20480,\"url\":\"/api/v1/file/download/1001\"}}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "上传失败（文件为空或格式不支持）", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1000\",\"errorMsg\":\"上传失败：文件为空或不支持的格式\",\"data\":null}")))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "文件超过大小限制", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1413\",\"errorMsg\":\"上传文件超过大小限制\",\"data\":null}")))
    @Operation(summary = "上传文件", description = "上传文件并返回文件元信息")
    @PostMapping("/upload")
    public ApiResponse<FileDTO> upload(@Parameter(description = "上传的文件（multipart/form-data）", required = true) @RequestParam("file") MultipartFile multipart) {
        FileUploadCommand command = new FileUploadCommand();
        try {
            command.setContent(multipart.getBytes());
        } catch (IOException e) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "读取上传文件失败");
        }
        command.setFileName(multipart.getOriginalFilename());
        command.setFileType(multipart.getContentType());
        command.setOwnerId(TenantContext.getCurrentUserId());
        return ApiResponse.success(fileStorageService.upload(command));
    }

    @Operation(summary = "查询文件信息")
    @PostMapping("/info")
    public ApiResponse<FileDTO> info(@RequestBody IdRequest request) {
        return ApiResponse.success(fileStorageService.getFileInfo(request.getId()));
    }

    @Operation(summary = "分页查询文件列表")
    @PostMapping("/list")
    public ApiResponse<PageResult<FileDTO>> list(@RequestBody FileQuery query) {
        return ApiResponse.success(fileStorageService.listFiles(query));
    }

    @Operation(summary = "删除文件")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdRequest request) {
        fileStorageService.delete(request.getId());
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功，返回文件二进制流（application/octet-stream）")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"errorCode\":\"1004\",\"errorMsg\":\"文件不存在\",\"data\":null}")))
    @Operation(summary = "下载文件", description = "按 ID 流式下载文件二进制内容")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@Parameter(description = "文件ID") @PathVariable("id") Long id) {
        FileDTO dto = fileStorageService.getFileInfo(id);
        byte[] bytes = fileStorageService.download(id);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        String fileName = dto.getFileName() == null ? ("file-" + id) : dto.getFileName();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : dto.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }
}
