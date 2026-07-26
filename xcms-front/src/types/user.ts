// 用户实体类型。
// 生成的 components['schemas']['UserDTO'] 在当前生成结果里解析为 never，
// 故此处用干净的本地接口作为页面/ Mock 的契约来源。
export interface UserDTO {
  id?: number;
  tenantId?: number;
  username?: string;
  realName?: string;
  employeeNo?: string;
  email?: string;
  phone?: string;
  avatar?: string | null;
  status?: string;
  lastLoginAt?: string;
  lastLoginIp?: string;
  roles?: { roleCode?: string; roleName?: string }[];
}
