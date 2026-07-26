import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { RouteMenuItem } from '@/types/menu';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  userId: number | null;
  username: string | null;
  realName: string | null;
  tenantId: number | null;
  tenantName: string | null;
  roles: string[];
  menus: RouteMenuItem[];
  setAuth: (data: {
    token: string;
    refreshToken: string;
    userId: number;
    username: string;
    realName: string;
    tenantId: number;
    tenantName: string;
    roles: string[];
  }) => void;
  setMenus: (menus: RouteMenuItem[]) => void;
  clearAuth: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      userId: null,
      username: null,
      realName: null,
      tenantId: null,
      tenantName: null,
      roles: [],
      menus: [],
      setAuth: (data) => set(data),
      setMenus: (menus) => set({ menus }),
      clearAuth: () =>
        set({
          token: null,
          refreshToken: null,
          userId: null,
          username: null,
          realName: null,
          tenantId: null,
          tenantName: null,
          roles: [],
          menus: [],
        }),
      isAuthenticated: () => !!get().token,
    }),
    { name: 'xcms-auth' }
  )
);
