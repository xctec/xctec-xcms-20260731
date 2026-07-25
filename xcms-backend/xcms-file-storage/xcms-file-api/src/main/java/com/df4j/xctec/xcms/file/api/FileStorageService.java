package com.df4j.xctec.xcms.file.api;

import com.df4j.xctec.xcms.file.api.dto.FileDTO;
import com.df4j.xctec.xcms.file.api.dto.FileQuery;
import com.df4j.xctec.xcms.file.api.dto.request.FileUploadCommand;
import com.df4j.xctec.xcms.kernel.common.PageResult;

/**
 * 文件存储服务。实际文件落地本地文件系统，元数据落库；
 * 上传前通过配置中心（configuration）校验租户存储配额。
 */
public interface FileStorageService {

    /** 存储文件，返回元数据（含 fileCode）。 */
    FileDTO upload(FileUploadCommand command);

    /** 获取文件元数据 */
    FileDTO getFileInfo(Long fileId);

    /** 读取文件二进制内容 */
    byte[] download(Long fileId);

    /** 删除文件（软删除 + 清理物理文件） */
    void delete(Long fileId);

    /** 分页列出文件 */
    PageResult<FileDTO> listFiles(FileQuery query);
}
