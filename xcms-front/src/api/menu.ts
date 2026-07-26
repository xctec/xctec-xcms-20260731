import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const menuApi = {
  /** 获取当前用户可见的菜单树 */
  getUserMenus: () => http.post<unknown, Unwrap<Schemas['ApiResponseListMenuDTO']>>('/api/menu/user-menus', {}),
  /** 获取所有菜单（管理面菜单管理用） */
  getAllMenus: () => http.post<unknown, Unwrap<Schemas['ApiResponseListMenuDTO']>>('/admin/menu/all', {}),
};
