import type { Schemas } from '@/types/api-helpers';

/** 后端菜单实体（与生成类型一致，作为契约单一来源） */
export type MenuDTO = Schemas['MenuDTO'];

/** 旧版菜单项（部分页面仍在使用，保留兼容） */
export interface MenuItem {
  id?: number;
  parentId?: number;
  name?: string;
  icon?: string;
  path?: string;
  sort?: number;
  visible?: boolean;
  type?: 'CATALOG' | 'MENU' | 'BUTTON';
  permission?: string;
  children?: MenuItem[];
}

/** 前端路由菜单模型（由后端 MenuDTO 映射而来，见 api/menu.ts 的 toRouteMenu） */
export interface RouteMenuItem {
  key: string;
  label: string;
  icon?: string;
  path: string;
  component?: string;
  sort: number;
  visible: boolean;
  permission?: string;
  /** 菜单类型：目录 / 菜单 / 按钮（按钮仅用于驱动按钮级权限，不渲染为导航项） */
  type?: 'DIRECTORY' | 'MENU' | 'BUTTON';
  children?: RouteMenuItem[];
}
