import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type ThemeMode = 'light' | 'dark';
type Density = 'compact' | 'standard' | 'comfortable';
type ColorScheme = 'blue' | 'violet' | 'emerald' | 'cyan' | 'rose' | 'amber' | 'slate';
type Face = 'admin' | 'portal';
type Lang = 'zh-CN' | 'en-US';

interface AppState {
  themeMode: ThemeMode;
  density: Density;
  colorScheme: ColorScheme;
  face: Face;
  lang: Lang;
  sidebarCollapsed: boolean;
  setThemeMode: (mode: ThemeMode) => void;
  setDensity: (density: Density) => void;
  setColorScheme: (scheme: ColorScheme) => void;
  setFace: (face: Face) => void;
  setLang: (lang: Lang) => void;
  toggleSidebar: () => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      themeMode: 'light',
      density: 'standard',
      colorScheme: 'blue',
      face: 'admin',
      lang: 'zh-CN',
      sidebarCollapsed: false,
      setThemeMode: (themeMode) => set({ themeMode }),
      setDensity: (density) => set({ density }),
      setColorScheme: (colorScheme) => set({ colorScheme }),
      setFace: (face) => set({ face }),
      setLang: (lang) => set({ lang }),
      toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed })),
    }),
    { name: 'xcms-app' }
  )
);
