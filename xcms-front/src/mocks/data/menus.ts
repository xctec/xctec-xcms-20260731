import type { MenuDTO } from '@/types/menu';

/** 演示数据：使用后端实体模型 MenuDTO（与后端契约一致），由 api/menu.ts 映射为前端路由模型 */
export const mockMenus: MenuDTO[] = [
  { id: 1, menuCode: 'tenant', menuName: '租户管理', menuType: 'MENU', path: '/admin/tenant', icon: 'Building2', sortOrder: 1, children: [] },
  { id: 2, menuCode: 'organization', menuName: '组织架构', menuType: 'MENU', path: '/admin/organization', icon: 'Network', sortOrder: 2, children: [] },
  { id: 3, menuCode: 'user', menuName: '用户管理', menuType: 'MENU', path: '/admin/user', icon: 'Users', sortOrder: 3, children: [] },
  { id: 4, menuCode: 'permission', menuName: '权限管理', menuType: 'MENU', path: '/admin/permission', icon: 'Shield', sortOrder: 4, children: [] },
  { id: 5, menuCode: 'operation', menuName: '运营看板', menuType: 'MENU', path: '/admin/operation', icon: 'BarChart3', sortOrder: 5, children: [] },
  { id: 6, menuCode: 'message', menuName: '消息中心', menuType: 'MENU', path: '/admin/message', icon: 'Bell', sortOrder: 6, children: [] },
  { id: 7, menuCode: 'config', menuName: '配置管理', menuType: 'MENU', path: '/admin/config', icon: 'Settings', sortOrder: 7, children: [] },
];
