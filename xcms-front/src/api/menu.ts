import { http } from './http';
import type { RouteMenuItem } from '@/types/menu';

export const menuApi = {
  /** 获取当前用户可见的菜单树 */
  getUserMenus: () => http.post<unknown, RouteMenuItem[]>('/api/menu/user-menus', {}),
  /** 获取所有菜单（管理面菜单管理用） */
  getAllMenus: () => http.post<unknown, RouteMenuItem[]>('/admin/menu/all', {}),
};
