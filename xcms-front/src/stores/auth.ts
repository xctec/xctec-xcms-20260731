import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { RouteMenuItem } from '@/types/menu';

/** token 过期前多少秒开始主动刷新 */
const REFRESH_THRESHOLD = 60;

interface SetAuthData {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
  realName: string;
  tenantId: number;
  tenantName: string;
  roles: string[];
  expiresIn?: number;
}

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  /** token 签发时间戳（ms），用于主动刷新判断 */
  tokenIssuedAt: number | null;
  /** token 有效期（秒） */
  expiresIn: number | null;
  userId: number | null;
  username: string | null;
  realName: string | null;
  tenantId: number | null;
  tenantName: string | null;
  roles: string[];
  /** 扁平的权限码集合（由菜单树派生，含按钮级权限） */
  permissions: string[];
  /** 管理面菜单树（顶层为分组/目录，children 为菜单项） */
  menus: RouteMenuItem[];
  /** 业务面菜单树 */
  portalMenus: RouteMenuItem[];
  setAuth: (data: SetAuthData) => void;
  /** 登录/刷新后更新令牌，重置签发时间 */
  setTokens: (data: { token: string; refreshToken: string; expiresIn?: number }) => void;
  /** 设置菜单树并派生权限集合 */
  setMenus: (menus: RouteMenuItem[], portalMenus: RouteMenuItem[]) => void;
  clearAuth: () => void;
  isAuthenticated: () => boolean;
  /** token 是否即将过期（用于主动刷新） */
  isAboutToExpire: () => boolean;
  /** 是否拥有指定权限码（空值视为通过） */
  hasPermission: (permission?: string | null) => boolean;
  /** 是否拥有指定角色（空值视为通过） */
  hasRole: (role?: string | null) => boolean;
  /** 是否拥有其中任意一个权限 */
  hasAnyPermission: (permissions: string[]) => boolean;
  /** 是否同时拥有全部权限 */
  hasAllPermissions: (permissions: string[]) => boolean;
}

/** 从菜单树中收集所有非空权限码（含按钮级） */
function collectPermissions(menus: RouteMenuItem[]): string[] {
  const codes = new Set<string>();
  const walk = (items: RouteMenuItem[]) => {
    for (const item of items) {
      if (item.permission) codes.add(item.permission);
      if (item.children?.length) walk(item.children);
    }
  };
  walk(menus);
  return [...codes];
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      tokenIssuedAt: null,
      expiresIn: null,
      userId: null,
      username: null,
      realName: null,
      tenantId: null,
      tenantName: null,
      roles: [],
      permissions: [],
      menus: [],
      portalMenus: [],
      setAuth: (data) =>
        set({
          token: data.token,
          refreshToken: data.refreshToken,
          tokenIssuedAt: Date.now(),
          expiresIn: data.expiresIn ?? null,
          userId: data.userId,
          username: data.username,
          realName: data.realName,
          tenantId: data.tenantId,
          tenantName: data.tenantName,
          roles: data.roles,
        }),
      setTokens: ({ token, refreshToken, expiresIn }) =>
        set({
          token,
          refreshToken,
          tokenIssuedAt: Date.now(),
          expiresIn: expiresIn ?? get().expiresIn,
        }),
      setMenus: (menus, portalMenus) =>
        set({
          menus,
          portalMenus,
          permissions: collectPermissions([...menus, ...portalMenus]),
        }),
      clearAuth: () =>
        set({
          token: null,
          refreshToken: null,
          tokenIssuedAt: null,
          expiresIn: null,
          userId: null,
          username: null,
          realName: null,
          tenantId: null,
          tenantName: null,
          roles: [],
          permissions: [],
          menus: [],
          portalMenus: [],
        }),
      isAuthenticated: () => !!get().token,
      isAboutToExpire: () => {
        const { token, tokenIssuedAt, expiresIn } = get();
        if (!token || !tokenIssuedAt || !expiresIn) return false;
        const elapsed = (Date.now() - tokenIssuedAt) / 1000;
        return elapsed > expiresIn - REFRESH_THRESHOLD;
      },
      hasPermission: (permission) => !permission || get().permissions.includes(permission),
      hasRole: (role) => !role || get().roles.includes(role),
      hasAnyPermission: (permissions) =>
        permissions.length === 0 || permissions.some((p) => get().permissions.includes(p)),
      hasAllPermissions: (permissions) =>
        permissions.length === 0 || permissions.every((p) => get().permissions.includes(p)),
    }),
    { name: 'xcms-auth' }
  )
);
