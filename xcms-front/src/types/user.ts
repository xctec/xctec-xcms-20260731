import type { Schemas } from '@/types/api-helpers';

/**
 * 用户 DTO：直接复用 OpenAPI 生成的契约类型（components.schemas.UserDTO）。
 * 0.2 已统一 Unwrap 模式，Schemas['UserDTO'] 为有效类型（非 never），无需手写，避免契约漂移。
 */
export type UserDTO = Schemas['UserDTO'];
