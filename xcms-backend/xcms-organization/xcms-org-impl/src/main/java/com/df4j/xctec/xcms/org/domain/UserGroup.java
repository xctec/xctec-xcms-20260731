package com.df4j.xctec.xcms.org.domain;

import com.df4j.xctec.xcms.kernel.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "org_user_group", indexes = {
        @Index(name = "uk_org_user_group_name", columnList = "tenant_id,group_name", unique = true)
})
public class UserGroup extends TenantEntity {

    @Column(name = "group_name", nullable = false, length = 128)
    private String groupName;

    @Column(name = "type", length = 32)
    private String type;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
