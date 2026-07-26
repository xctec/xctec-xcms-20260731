import { http } from './http';
import type { MenuDTO, RouteMenuItem } from '@/types/menu';

export type MenuFace = 'admin' | 'portal';

/**
 * 后端 MenuDTO（实体模型）-> 前端 RouteMenuItem（路由模型）映射。
 * 契约单一来源为生成的 MenuDTO；页面侧只消费 RouteMenuItem，避免字段错位。
 */
export function toRouteMenu(dto: MenuDTO): RouteMenuItem {
  return {
    key: dto.menuCode ?? String(dto.id ?? ''),
    label: dto.menuName ?? '',
    icon: dto.icon ?? undefined,
    path: dto.path ?? '',
    component: '', // 组件由前端路由表决定，不来自后端
    sort: dto.sortOrder ?? 0,
    visible: dto.menuType !== 'HIDDEN',
    permission: dto.menuCode,
    type: dto.menuType as RouteMenuItem['type'],
    children: dto.children?.map(toRouteMenu),
  };
}

const toRouteMenus = (dtos: MenuDTO[]): RouteMenuItem[] => dtos.map(toRouteMenu);

export const menuApi = {
  /** 获取当前用户在某端面下的菜单树（已按权限过滤，前端再二次校验） */
  getUserMenus: (face: MenuFace) =>
    http.post<unknown, MenuDTO[]>('/api/menu/user-menus', { face }).then(toRouteMenus),
  /** 获取所有菜单（管理面菜单管理用） */
  getAllMenus: () =>
    http.post<unknown, MenuDTO[]>('/admin/menu/all', {}).then(toRouteMenus),
};
