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
public class FileController {

    private final FileStorageService fileStorageService;

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
