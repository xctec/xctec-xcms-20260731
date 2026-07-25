import type { TenantDTO } from '@/types/tenant';

export const mockTenants: TenantDTO[] = [
  { id: 1, tenantCode: 'GROUP_HQ', tenantName: '集团总部', tenantType: 'ORGANIZATION', parentId: null, level: 1, path: '/1/', status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-01-15' },
  { id: 2, tenantCode: 'SUB_TECH', tenantName: '科技公司', tenantType: 'ORGANIZATION', parentId: 1, level: 2, path: '/1/2/', status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-02-20' },
  { id: 3, tenantCode: 'SUB_FIN', tenantName: '财务公司', tenantType: 'ORGANIZATION', parentId: 1, level: 2, path: '/1/3/', status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-03-10' },
  { id: 4, tenantCode: 'PROJ_X1', tenantName: '智慧园区项目', tenantType: 'PROJECT', parentId: 2, level: 3, path: '/1/2/4/', status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-04-05' },
  { id: 5, tenantCode: 'SUB_LOG', tenantName: '物流公司', tenantType: 'ORGANIZATION', parentId: 1, level: 2, path: '/1/5/', status: 'SUSPENDED', deploymentMode: 'shared', createdAt: '2025-03-22' },
  { id: 6, tenantCode: 'PROJ_Y2', tenantName: '数据中台项目', tenantType: 'PROJECT', parentId: 2, level: 3, path: '/1/2/6/', status: 'MIGRATING', deploymentMode: 'shared', createdAt: '2025-05-18' },
  { id: 7, tenantCode: 'EXT_PARTNER', tenantName: '合作方A', tenantType: 'EXTERNAL', parentId: 1, level: 2, path: '/1/7/', status: 'LOCKED', deploymentMode: 'shared', createdAt: '2025-06-01' },
  { id: 8, tenantCode: 'SUB_HR', tenantName: '人力资源公司', tenantType: 'ORGANIZATION', parentId: 1, level: 2, path: '/1/8/', status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-06-15' },
];
