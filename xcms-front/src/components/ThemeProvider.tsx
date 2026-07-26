import { useEffect } from 'react';
import { useAppStore } from '@/stores/app';

const schemes = {
  blue: { light: { primary: '#2563EB', p50: '#EFF6FF', p100: '#DBEAFE', p600: '#1D4ED8', p700: '#1E40AF' }, dark: { primary: '#3B82F6', p50: '#1E293B', p100: '#334155', p600: '#2563EB', p700: '#1D4ED8' } },
  violet: { light: { primary: '#7C3AED', p50: '#F5F3FF', p100: '#EDE9FE', p600: '#6D28D9', p700: '#5B21B6' }, dark: { primary: '#A78BFA', p50: '#2E1065', p100: '#3B0764', p600: '#8B5CF6', p700: '#7C3AED' } },
  emerald: { light: { primary: '#059669', p50: '#ECFDF5', p100: '#D1FAE5', p600: '#047857', p700: '#065F46' }, dark: { primary: '#34D399', p50: '#052E16', p100: '#064E3B', p600: '#10B981', p700: '#059669' } },
  cyan: { light: { primary: '#0891B2', p50: '#ECFEFF', p100: '#CFFAFE', p600: '#0E7490', p700: '#155E75' }, dark: { primary: '#22D3EE', p50: '#083344', p100: '#155E75', p600: '#06B6D4', p700: '#0891B2' } },
  rose: { light: { primary: '#E11D48', p50: '#FFF1F2', p100: '#FFE4E6', p600: '#BE123C', p700: '#9F1239' }, dark: { primary: '#FB7185', p50: '#4C0519', p100: '#881337', p600: '#E11D48', p700: '#BE123C' } },
  amber: { light: { primary: '#D97706', p50: '#FFFBEB', p100: '#FEF3C7', p600: '#B45309', p700: '#92400E' }, dark: { primary: '#F59E0B', p50: '#422006', p100: '#78350F', p600: '#D97706', p700: '#B45309' } },
  slate: { light: { primary: '#475569', p50: '#F8FAFC', p100: '#F1F5F9', p600: '#334155', p700: '#1E293B' }, dark: { primary: '#94A3B8', p50: '#1E293B', p100: '#334155', p600: '#475569', p700: '#334155' } },
} as const;

const densityVars: Record<string, Record<string, string>> = {
  compact: { '--row-h': '36px', '--btn-h': '28px', '--input-h': '28px', '--card-p': '12px', '--gap': '12px', '--fs': '12px', '--menu-h': '32px', '--bar-h': '48px' },
  standard: { '--row-h': '44px', '--btn-h': '34px', '--input-h': '34px', '--card-p': '16px', '--gap': '16px', '--fs': '13px', '--menu-h': '36px', '--bar-h': '56px' },
  comfortable: { '--row-h': '56px', '--btn-h': '40px', '--input-h': '40px', '--card-p': '24px', '--gap': '20px', '--fs': '14px', '--menu-h': '44px', '--bar-h': '64px' },
};

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const { themeMode, density, colorScheme, lang } = useAppStore();

  useEffect(() => {
    const root = document.documentElement;
    const sc = schemes[colorScheme]?.[themeMode] || schemes.blue.light;
    root.setAttribute('data-theme', themeMode);
    root.classList.toggle('dark', themeMode === 'dark');
    const vars: Record<string, string> = {
      '--c-primary': sc.primary, '--c-primary-50': sc.p50, '--c-primary-100': sc.p100,
      '--c-primary-600': sc.p600, '--c-primary-700': sc.p700,
    };
    if (themeMode === 'dark') {
      Object.assign(vars, {
        '--c-bg': '#0F172A', '--c-card': '#1E293B', '--c-text': '#F1F5F9', '--c-text-sec': '#94A3B8',
        '--c-text-muted': '#64748B', '--c-border': '#334155', '--c-border-light': '#1E293B',
        '--c-hover': '#1E293B', '--c-input-bg': '#0F172A', '--c-header-bg': '#1E293B', '--c-code-bg': '#334155',
        '--c-success': '#4ADE80', '--c-warning': '#FBBF24', '--c-danger': '#F87171', '--c-info': '#22D3EE',
        '--c-success-50': '#052E16', '--c-warning-50': '#422006', '--c-danger-50': '#450A0A', '--c-info-50': '#083344',
        '--c-primary-200': '#334155',
      });
    } else {
      Object.assign(vars, {
        '--c-bg': '#F8FAFC', '--c-card': '#FFFFFF', '--c-text': '#0F172A', '--c-text-sec': '#475569',
        '--c-text-muted': '#94A3B8', '--c-border': '#E2E8F0', '--c-border-light': '#F1F5F9',
        '--c-hover': '#F8FAFC', '--c-input-bg': '#FFFFFF', '--c-header-bg': '#F8FAFC', '--c-code-bg': '#F1F5F9',
        '--c-success': '#16A34A', '--c-warning': '#D97706', '--c-danger': '#DC2626', '--c-info': '#0891B2',
        '--c-success-50': '#F0FDF4', '--c-warning-50': '#FFFBEB', '--c-danger-50': '#FEF2F2', '--c-info-50': '#ECFEFF',
        '--c-primary-200': '#BFDBFE',
      });
    }
    Object.entries(vars).forEach(([k, v]) => root.style.setProperty(k, v));
    Object.entries(densityVars[density]).forEach(([k, v]) => root.style.setProperty(k, v));
    document.body.style.backgroundColor = `var(--c-bg)`;
    document.body.style.color = `var(--c-text)`;
  }, [themeMode, density, colorScheme]);

  useEffect(() => {
    if (lang) document.documentElement.lang = lang;
  }, [lang]);

  return <>{children}</>;
}
