package com.df4j.xctec.xcms.file.repository;

import com.df4j.xctec.xcms.file.domain.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByFileCode(String fileCode);

    @Query("select coalesce(sum(f.sizeBytes),0) from FileInfo f where f.tenantId = :tenantId and f.status = 'ACTIVE'")
    long sumSizeByTenant(@Param("tenantId") Long tenantId);

    Page<FileInfo> findByBizModule(String bizModule, Pageable pageable);

    Page<FileInfo> findByBizModuleAndBizId(String bizModule, String bizId, Pageable pageable);
}
