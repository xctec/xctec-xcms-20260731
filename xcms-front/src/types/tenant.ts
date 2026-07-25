export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'MIGRATING' | 'ARCHIVED';
export type TenantType = 'ORGANIZATION' | 'PROJECT' | 'EXTERNAL' | 'PLATFORM';

export interface TenantDTO {
  id: number;
  tenantCode: string;
  tenantName: string;
  tenantType: TenantType;
  parentId: number | null;
  level: number;
  path: string;
  status: TenantStatus;
  deploymentMode: string;
  createdAt: string;
}

export interface TenantCreateRequest {
  tenantCode: string;
  tenantName: string;
  tenantType: TenantType;
  parentId: number | null;
}

export interface TenantListRequest {
  parentId?: number;
  page: number;
  size: number;
}
