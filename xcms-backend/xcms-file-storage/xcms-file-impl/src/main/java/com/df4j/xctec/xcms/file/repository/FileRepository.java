package com.df4j.xctec.xcms.file.repository;

import com.df4j.xctec.xcms.file.domain.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<FileInfo, Long> {

    @Query("select coalesce(sum(f.fileSize),0) from FileInfo f where f.tenantId = :tenantId and f.status = 'NORMAL'")
    long sumSizeByTenant(@Param("tenantId") Long tenantId);

    Page<FileInfo> findByOwnerId(Long ownerId, Pageable pageable);

    Page<FileInfo> findByOwnerIdAndFolderId(Long ownerId, Long folderId, Pageable pageable);
}
