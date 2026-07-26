import type { ComponentType } from 'react';

export interface MenuItem {
  id: number;
  parentId: number | null;
  menuName: string;
  menuType: 'DIRECTORY' | 'MENU' | 'BUTTON';
  path: string | null;
  component: string | null;
  icon: string | null;
  sort: number;
  visible: boolean;
  permission: string | null;
  children?: MenuItem[];
}

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
