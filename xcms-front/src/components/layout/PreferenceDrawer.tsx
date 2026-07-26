import { useAppStore } from '@/stores/app';
import { useTranslation } from 'react-i18next';
import { Sun, Moon, Check, X } from 'lucide-react';
import type { ColorScheme, Density, ThemeMode } from '@/stores/app';

const schemes: { key: ColorScheme; name: string; color: string }[] = [
  { key: 'blue', name: '靛蓝', color: '#2563EB' },
  { key: 'violet', name: '紫罗兰', color: '#7C3AED' },
  { key: 'emerald', name: '翡翠', color: '#059669' },
  { key: 'cyan', name: '青碧', color: '#0891B2' },
  { key: 'rose', name: '玫瑰', color: '#E11D48' },
  { key: 'amber', name: '琥珀', color: '#D97706' },
  { key: 'slate', name: '石板', color: '#475569' },
];

const densities: { key: Density; name: string; desc: string }[] = [
  { key: 'compact', name: '紧凑', desc: '信息密度最高' },
  { key: 'standard', name: '标准', desc: '日常管理操作' },
  { key: 'comfortable', name: '宽松', desc: '大屏展示' },
];

export function PreferenceDrawer({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { themeMode, density, colorScheme, lang, setThemeMode, setDensity, setColorScheme, setLang } = useAppStore();
  const { t } = useTranslation();

  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 z-40" style={{ backgroundColor: 'rgba(0,0,0,0.4)' }} onClick={onClose} />
      <div className="fixed right-0 top-0 z-50 h-full w-80 animate-slide-up overflow-y-auto" style={{ backgroundColor: 'var(--c-card)', borderLeft: '1px solid var(--c-border)' }}>
        {/* Header */}
        <div className="sticky top-0 z-10 flex items-center justify-between border-b px-5 py-4" style={{ backgroundColor: 'var(--c-card)', borderColor: 'var(--c-border-light)' }}>
          <h3 className="text-base font-semibold" style={{ color: 'var(--c-text)' }}>偏好设置</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={18} /></button>
        </div>

        <div className="p-5 space-y-6">
          {/* Theme Mode */}
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase text-gray-400">主题模式</label>
            <div className="grid grid-cols-2 gap-2">
              <button onClick={() => setThemeMode('light')} className={`flex items-center gap-2 rounded-lg border p-3 text-sm transition-colors ${themeMode === 'light' ? 'border-primary-500 text-primary-600' : 'border-gray-200 text-gray-600'}`} style={{ borderColor: themeMode === 'light' ? 'var(--c-primary)' : 'var(--c-border)', color: themeMode === 'light' ? 'var(--c-primary)' : 'var(--c-text-sec)' }}>
                <Sun size={16} /> 亮色
                {themeMode === 'light' && <Check size={14} className="ml-auto" style={{ color: 'var(--c-primary)' }} />}
              </button>
              <button onClick={() => setThemeMode('dark')} className={`flex items-center gap-2 rounded-lg border p-3 text-sm transition-colors ${themeMode === 'dark' ? 'border-primary-500 text-primary-600' : 'border-gray-200 text-gray-600'}`} style={{ borderColor: themeMode === 'dark' ? 'var(--c-primary)' : 'var(--c-border)', color: themeMode === 'dark' ? 'var(--c-primary)' : 'var(--c-text-sec)' }}>
                <Moon size={16} /> 暗色
                {themeMode === 'dark' && <Check size={14} className="ml-auto" style={{ color: 'var(--c-primary)' }} />}
              </button>
            </div>
          </div>

          {/* Color Scheme */}
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase text-gray-400">主题色</label>
            <div className="grid grid-cols-4 gap-2">
              {schemes.map(s => (
                <button key={s.key} onClick={() => setColorScheme(s.key)} className="flex flex-col items-center gap-1.5">
                  <span className="h-8 w-8 rounded-full" style={{ backgroundColor: s.color, border: colorScheme === s.key ? `2px solid var(--c-text)` : '2px solid transparent', boxShadow: colorScheme === s.key ? `0 0 0 2px ${s.color}40` : 'none' }} />
                  <span className="text-[10px]" style={{ color: colorScheme === s.key ? 'var(--c-text)' : 'var(--c-text-muted)' }}>{s.name}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Density */}
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase text-gray-400">界面密度</label>
            <div className="space-y-2">
              {densities.map(d => (
                <button key={d.key} onClick={() => setDensity(d.key)} className="flex w-full items-center gap-3 rounded-lg border p-3 text-left" style={{ borderColor: density === d.key ? 'var(--c-primary)' : 'var(--c-border)', backgroundColor: density === d.key ? 'var(--c-primary-50)' : 'transparent' }}>
                  <div className="flex-1">
                    <div className="text-sm font-medium" style={{ color: density === d.key ? 'var(--c-primary)' : 'var(--c-text)' }}>{d.name}</div>
                    <div className="text-xs" style={{ color: 'var(--c-text-muted)' }}>{d.desc}</div>
                  </div>
                  {density === d.key && <Check size={16} style={{ color: 'var(--c-primary)' }} />}
                </button>
              ))}
            </div>
          </div>

          {/* Language */}
          <div>
            <label className="mb-2 block text-xs font-semibold uppercase text-gray-400">语言</label>
            <div className="grid grid-cols-2 gap-2">
              <button onClick={() => setLang('zh-CN')} className="rounded-lg border p-3 text-sm" style={{ borderColor: lang === 'zh-CN' ? 'var(--c-primary)' : 'var(--c-border)', color: lang === 'zh-CN' ? 'var(--c-primary)' : 'var(--c-text-sec)' }}>
                简体中文 {lang === 'zh-CN' && <Check size={14} className="inline" />}
              </button>
              <button onClick={() => setLang('en-US')} className="rounded-lg border p-3 text-sm" style={{ borderColor: lang === 'en-US' ? 'var(--c-primary)' : 'var(--c-border)', color: lang === 'en-US' ? 'var(--c-primary)' : 'var(--c-text-sec)' }}>
                English {lang === 'en-US' && <Check size={14} className="inline" />}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
