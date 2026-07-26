// 权限/角色相关实体类型。
// 与 organization.ts 同理，生成的 RoleDTO / PermissionDTO 在当前生成结果里解析为 never，
// 故此处用干净的本地接口作为页面/ Mock 的契约来源。
export interface PermissionDTO {
  id?: number;
  permCode?: string;
  permName?: string;
  permType?: string;
  module?: string;
  action?: string;
}

export interface RoleDTO {
  id?: number;
  roleCode?: string;
  roleName?: string;
  roleType?: string;
  description?: string;
  parentId?: number;
  status?: string;
}

export interface DataScope {
  scopeType?: string;
  scopeValues?: string[];
}
