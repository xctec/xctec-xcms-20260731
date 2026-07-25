package com.df4j.xctec.xcms.portal.domain;

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

/**
 * 用户自定义快捷入口（门户工作台）。按用户 + 门户面（ADMIN/BUSINESS）隔离，逻辑删除。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "portal_quick_entry", indexes = {
        @Index(name = "uk_portal_qe_user_surface", columnList = "user_id,surface,deleted_at")
})
public class PortalQuickEntry extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 门户面：ADMIN（管理面） / BUSINESS（业务面） */
    @Column(name = "surface", nullable = false, length = 20)
    private String surface;

    @Column(name = "title", nullable = false, length = 64)
    private String title;

    @Column(name = "icon", length = 64)
    private String icon;

    @Column(name = "url", length = 255)
    private String url;

    /** 打开方式：SELF（当前窗口） / BLANK（新窗口） */
    @Column(name = "target", length = 20)
    @Builder.Default
    private String target = "SELF";

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    /** 逻辑删除时间；为空表示未删除 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
