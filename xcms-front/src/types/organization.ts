import type { Schemas } from '@/types/api-helpers';

/**
 * 组织架构相关 DTO：直接复用 OpenAPI 生成的契约类型（components.schemas.*）。
 * 0.2 已统一 Unwrap 模式，以下均为有效类型，无需手写，避免契约漂移。
 */
export type DepartmentTreeDTO = Schemas['DepartmentTreeDTO'];
export type UserPositionDTO = Schemas['UserPositionDTO'];
export type UserGroupDTO = Schemas['UserGroupDTO'];
