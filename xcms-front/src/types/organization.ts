// 组织架构相关实体类型。
// 注：openapi 生成的 components['schemas'] 中部分实体 DTO（DepartmentTreeDTO 等）
// 在当前生成结果里被解析为 never，故此处用干净的本地接口作为页面/ Mock 的契约来源。
export interface DepartmentTreeDTO {
  id?: number;
  deptName?: string;
  deptCode?: string;
  managerId?: number;
  children?: DepartmentTreeDTO[];
}

export interface UserPositionDTO {
  id?: number;
  userId?: number;
  deptId?: number;
  deptName?: string;
  positionId?: number;
  positionName?: string;
}

export interface UserGroupDTO {
  id?: number;
  groupName?: string;
  description?: string;
  type?: string;
  memberCount?: number;
}
