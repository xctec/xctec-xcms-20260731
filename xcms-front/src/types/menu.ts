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
  children?: RouteMenuItem[];
}
