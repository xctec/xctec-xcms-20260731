import { http } from './http';
import type { RouteMenuItem } from '@/types/menu';

export type MenuFace = 'admin' | 'portal';

export const menuApi = {
  /** 获取当前用户在某端面下的菜单树（已按权限过滤，前端再二次校验） */
  getUserMenus: (face: MenuFace) =>
    http.post<unknown, RouteMenuItem[]>('/api/menu/user-menus', { face }),
};
