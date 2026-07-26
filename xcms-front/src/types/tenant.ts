/** 租户相关类型——从 OpenAPI 生成类型重导出，避免手写漂移 */
import type { Schemas } from './api-helpers';

type S = Schemas;

export type TenantDTO = S['TenantDTO'];
export type TenantTreeDTO = S['TenantTreeDTO'];
export type TenantCreateRequest = S['TenantCreateRequest'];
export type TenantListRequest = S['TenantListChildrenRequest'];

// 枚举从 DTO 字段派生（生成类型里为内联 enum union，去 undefined）
export type TenantStatus = NonNullable<S['TenantDTO']['status']>;
export type TenantType = NonNullable<S['TenantDTO']['tenantType']>;
