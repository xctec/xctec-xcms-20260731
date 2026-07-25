package com.df4j.xctec.xcms.file.service;

import com.df4j.xctec.xcms.config.api.ConfigService;
import com.df4j.xctec.xcms.file.api.FileStorageService;
import com.df4j.xctec.xcms.file.api.dto.FileDTO;
import com.df4j.xctec.xcms.file.api.dto.FileQuery;
import com.df4j.xctec.xcms.file.api.dto.request.FileUploadCommand;
import com.df4j.xctec.xcms.file.domain.FileInfo;
import com.df4j.xctec.xcms.file.mapper.FileMapper;
import com.df4j.xctec.xcms.file.repository.FileRepository;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final long DEFAULT_QUOTA_BYTES = 0L; // 0 表示不限配额，由配置中心开关覆盖

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final ConfigService configService;

    @Value("${file.storage.local-root:}")
    private String storageRootConfig;

    @Override
    @Transactional
    public FileDTO upload(FileUploadCommand command) {
        Long tenantId = TenantContext.getTenantId();
        long newSize = command.getContent() == null ? 0 : command.getContent().length;
        long quota = configService.getLongValue("file.storage.quota.bytes", DEFAULT_QUOTA_BYTES);
        if (quota > 0) {
            long used = fileRepository.sumSizeByTenant(tenantId);
            if (used + newSize > quota) {
                throw new BusinessException(ErrorCodes.QUOTA_EXCEEDED, "存储配额不足");
            }
        }

        String code = UUID.randomUUID().toString().replace("-", "");
        String relPath = buildRelativePath(tenantId, code, command.getOriginalName());
        Path full = Paths.get(resolveRoot(), relPath);
        try {
            Files.createDirectories(full.getParent());
            Files.write(full, command.getContent());
        } catch (IOException e) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "写入文件失败: " + e.getMessage());
        }

        FileInfo info = new FileInfo();
        info.setFileCode(code);
        info.setOriginalName(command.getOriginalName());
        info.setContentType(command.getContentType());
        info.setSizeBytes(newSize);
        info.setStorageType("LOCAL");
        info.setStoragePath(relPath);
        info.setMd5(DigestUtils.md5DigestAsHex(command.getContent()));
        info.setBizModule(command.getBizModule());
        info.setBizId(command.getBizId());
        info.setUploaderId(command.getUploaderId());
        info.setStatus("ACTIVE");
        return fileMapper.toDto(fileRepository.save(info));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO getFileInfo(Long fileId) {
        return fileRepository.findById(fileId)
                .map(fileMapper::toDto)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "文件不存在: " + fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(Long fileId) {
        FileInfo info = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "文件不存在: " + fileId));
        Path full = Paths.get(resolveRoot(), info.getStoragePath());
        try {
            return Files.readAllBytes(full);
        } catch (IOException e) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "读取文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void delete(Long fileId) {
        FileInfo info = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "文件不存在: " + fileId));
        Path full = Paths.get(resolveRoot(), info.getStoragePath());
        try {
            Files.deleteIfExists(full);
        } catch (IOException ignored) {
            // 物理文件缺失不影响逻辑删除
        }
        info.setStatus("DELETED");
        fileRepository.save(info);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<FileDTO> listFiles(FileQuery query) {
        int page = query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FileInfo> result;
        if (query.getBizId() != null && !query.getBizId().isBlank()) {
            result = fileRepository.findByBizModuleAndBizId(query.getBizModule(), query.getBizId(), pageable);
        } else if (query.getBizModule() != null && !query.getBizModule().isBlank()) {
            result = fileRepository.findByBizModule(query.getBizModule(), pageable);
        } else {
            result = fileRepository.findAll(pageable);
        }
        return PageResult.of(result.stream().map(fileMapper::toDto).toList(), result.getTotalElements());
    }

    private String resolveRoot() {
        if (storageRootConfig != null && !storageRootConfig.isBlank()) {
            return storageRootConfig;
        }
        return System.getProperty("user.home") + "/xcms-files";
    }

    private String buildRelativePath(Long tenantId, String code, String originalName) {
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        return tenantId + "/" + code.substring(0, 2) + "/" + code + ext;
    }
}
