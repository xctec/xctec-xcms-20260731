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
@Table(name = "org_user_group_member", indexes = {
        @Index(name = "idx_org_group_member_group", columnList = "group_id"),
        @Index(name = "uk_org_group_member", columnList = "tenant_id,group_id,user_id", unique = true)
})
public class UserGroupMember extends TenantEntity {

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
