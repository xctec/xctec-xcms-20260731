/**
 * OpenAPI 生成类型的快捷访问与辅助类型。
 * 由 `pnpm gen:api` 从 openapi.json 生成（src/types/api-generated.ts）。
 */
import type { components, paths, operations } from './api-generated';

export type { components, paths, operations };

/** components.schemas 的快捷别名 */
export type Schemas = components['schemas'];

/**
 * 去掉后端统一响应 `ApiResponse<T>` 的外壳，取 `data` 类型。
 *
 * 后端统一返回 `ApiResponse<T> = { errorCode, errorMsg, data: T }`，
 * http 拦截器（api/http.ts）已把 `data` 解包返回，故 api 层的返回类型
 * 用 `Unwrap<ApiResponseXxxDTO>` 取内层 T，调用方直接拿到 T。
 *
 * 例：`Unwrap<Schemas['ApiResponseTenantDTO']>` = `TenantDTO`
 */
export type Unwrap<T> = T extends { data?: infer D } ? D : never;
