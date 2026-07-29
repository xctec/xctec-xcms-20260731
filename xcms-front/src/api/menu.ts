import { http } from './http';
import type { MenuDTO, RouteMenuItem } from '@/types/menu';
import { useAuthStore } from '@/stores/auth';

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
    // 权限码取自后端 MenuDTO.permission（按钮/菜单对应的真实权限码）；目录级 menuCode 仅作唯一标识
    permission: dto.permission,
    type: dto.menuType as RouteMenuItem['type'],
    children: dto.children?.map(toRouteMenu),
  };
}

const toRouteMenus = (dtos: MenuDTO[]): RouteMenuItem[] => dtos.map(toRouteMenu);

export const menuApi = {
  /**
   * 获取当前用户在某端面下的菜单树（已按权限过滤，前端再二次校验）。
   * 后端按端面分两个端点：
   *  - admin（管理后台）：/admin/permission/menus，需 userId + scope=ADMIN
   *  - portal（业务前台）：/portal/menus，当前用户由 TenantContext 解析，scope=BUSINESS
   */
  getUserMenus: async (face: MenuFace): Promise<RouteMenuItem[]> => {
    if (face === 'admin') {
      const userId = useAuthStore.getState().userId ?? undefined;
      const dtos = await http.post<unknown, MenuDTO[]>('/api/admin/permission/menus', {
        userId,
        scope: 'ADMIN',
      });
      return toRouteMenus(dtos);
    }
    const dtos = await http.post<unknown, MenuDTO[]>('/api/portal/menus', {
      scope: 'BUSINESS',
    });
    return toRouteMenus(dtos);
  },
};
