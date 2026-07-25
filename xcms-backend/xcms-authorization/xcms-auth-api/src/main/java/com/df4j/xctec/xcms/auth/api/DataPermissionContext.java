package com.df4j.xctec.xcms.auth.api;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据权限上下文，包含用户在各维度的数据范围
 */
@Data
public class DataPermissionContext {

    private Long userId;
    private Long tenantId;
    private String resourceType;

    private List<String> orgPaths;
    private List<Long> businessLineIds;
    private List<String> regions;
    private List<String> tags;
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;
    private boolean ownerOnly;
    private String ownerField;

    public boolean hasOrgScope() {
        return orgPaths != null && !orgPaths.isEmpty();
    }

    public boolean hasBusinessLineScope() {
        return businessLineIds != null && !businessLineIds.isEmpty();
    }

    public boolean hasRegionScope() {
        return regions != null && !regions.isEmpty();
    }

    public boolean hasTagScope() {
        return tags != null && !tags.isEmpty();
    }

    public boolean hasTimeScope() {
        return timeFrom != null || timeTo != null;
    }

    public boolean hasOwnerScope() {
        return ownerOnly;
    }

    public static DataPermissionContext empty(Long userId, Long tenantId, String resourceType) {
        DataPermissionContext ctx = new DataPermissionContext();
        ctx.setUserId(userId);
        ctx.setTenantId(tenantId);
        ctx.setResourceType(resourceType);
        return ctx;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DataPermissionContext ctx = new DataPermissionContext();

        public Builder userId(Long userId) {
            ctx.setUserId(userId);
            return this;
        }

        public Builder tenantId(Long tenantId) {
            ctx.setTenantId(tenantId);
            return this;
        }

        public Builder resourceType(String resourceType) {
            ctx.setResourceType(resourceType);
            return this;
        }

        public Builder orgPaths(List<String> orgPaths) {
            ctx.setOrgPaths(orgPaths);
            return this;
        }

        public Builder businessLineIds(List<Long> businessLineIds) {
            ctx.setBusinessLineIds(businessLineIds);
            return this;
        }

        public Builder regions(List<String> regions) {
            ctx.setRegions(regions);
            return this;
        }

        public Builder tags(List<String> tags) {
            ctx.setTags(tags);
            return this;
        }

        public Builder timeFrom(LocalDateTime timeFrom) {
            ctx.setTimeFrom(timeFrom);
            return this;
        }

        public Builder timeTo(LocalDateTime timeTo) {
            ctx.setTimeTo(timeTo);
            return this;
        }

        public Builder ownerOnly(boolean ownerOnly) {
            ctx.setOwnerOnly(ownerOnly);
            return this;
        }

        public Builder ownerField(String ownerField) {
            ctx.setOwnerField(ownerField);
            return this;
        }

        public DataPermissionContext build() {
            return ctx;
        }
    }
}
