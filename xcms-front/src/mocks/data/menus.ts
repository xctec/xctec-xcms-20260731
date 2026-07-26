import type { MenuDTO } from '@/types/menu';

/**
 * 演示数据：使用后端实体模型 MenuDTO（与后端契约一致），由 api/menu.ts 的 toRouteMenu 映射为前端路由模型。
 * 树形结构：顶层为分组（DIRECTORY），children 为菜单项（MENU）与按钮项（BUTTON，仅驱动按钮级权限）。
 * 按钮项的 menuCode 填权限码（如 user:create），菜单项的 menuCode 填菜单码（如 tenant）。
 * TODO: 后端 MenuDTO 补 permission 字段后，toRouteMenu 改为 permission: dto.permission。
 */
const adminGroup = (id: number, code: string, name: string, icon: string, sort: number, children: MenuDTO[]): MenuDTO => ({
  id, menuCode: code, menuName: name, menuType: 'DIRECTORY', path: '', icon, sortOrder: sort, children,
});

const menu = (id: number, code: string, name: string, icon: string, path: string, sort: number): MenuDTO => ({
  id, menuCode: code, menuName: name, menuType: 'MENU', path, icon, sortOrder: sort, children: [],
});

const btn = (code: string, name: string): MenuDTO => ({
  id: 0, menuCode: code, menuName: name, menuType: 'BUTTON', path: '', icon: '', sortOrder: 0, children: [],
});

export const mockAdminMenus: MenuDTO[] = [
  adminGroup(10, 'group-tenant', '租户与组织', 'Building2', 1, [
    menu(11, 'tenant', '租户管理', 'Building2', '/admin/tenant', 1),
    menu(12, 'organization', '组织架构', 'Network', '/admin/organization', 2),
    menu(13, 'user', '用户管理', 'Users', '/admin/user', 3),
    btn('user:create', '新建用户'),
    btn('user:export', '导出'),
    btn('user:reset-pwd', '重置密码'),
  ]),
  adminGroup(20, 'group-security', '安全与权限', 'Shield', 2, [
    menu(21, 'permission', '权限管理', 'Shield', '/admin/permission', 1),
  ]),
  adminGroup(30, 'group-biz', '业务流程', 'Workflow', 3, [
    menu(31, 'workflow', '流程管理', 'Workflow', '/admin/workflow', 1),
  ]),
  adminGroup(40, 'group-ops', '运营', 'BarChart3', 4, [
    menu(41, 'operation', '运营看板', 'BarChart3', '/admin/operation', 1),
    menu(42, 'message', '消息中心', 'Bell', '/admin/message', 2),
    menu(43, 'file', '文件管理', 'FolderOpen', '/admin/file', 3),
  ]),
  adminGroup(50, 'group-system', '系统', 'Settings', 5, [
    menu(51, 'config', '配置管理', 'Settings', '/admin/config', 1),
    menu(52, 'task', '任务调度', 'Clock', '/admin/task', 2),
    menu(53, 'audit', '审计日志', 'FileText', '/admin/audit', 3),
  ]),
];

export const mockPortalMenus: MenuDTO[] = [
  adminGroup(60, 'group-workbench', '工作台', 'LayoutDashboard', 1, [
    menu(61, 'workbench', '我的工作台', 'LayoutDashboard', '/portal/workbench', 1),
  ]),
  adminGroup(70, 'group-portal-biz', '业务流程', 'Workflow', 2, [
    menu(71, 'portal-workflow', '流程办理', 'Workflow', '/portal/workflow', 1),
  ]),
  adminGroup(80, 'group-portal-ops', '信息中心', 'Bell', 3, [
    menu(81, 'portal-message', '消息中心', 'Bell', '/portal/message', 1),
    menu(82, 'portal-file', '文件管理', 'FolderOpen', '/portal/file', 2),
  ]),
  adminGroup(90, 'group-profile', '个人中心', 'User', 4, [
    menu(91, 'profile', '个人资料', 'User', '/portal/profile', 1),
  ]),
];
