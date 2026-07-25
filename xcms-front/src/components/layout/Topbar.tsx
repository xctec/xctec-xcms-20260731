import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Home, ChevronRight, Bell, Moon, Sun, Palette, Type, Globe,
  ChevronDown, User, Settings, Power, CheckCircle,
} from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import clsx from 'clsx';

type DropdownKey = 'theme' | 'density' | 'lang' | 'profile' | null;

const schemes = [
  { key: 'blue', name: '靛蓝', color: '#2563EB' },
  { key: 'violet', name: '紫罗兰', color: '#7C3AED' },
  { key: 'emerald', name: '翡翠', color: '#059669' },
  { key: 'cyan', name: '青碧', color: '#0891B2' },
  { key: 'rose', name: '玫瑰', color: '#E11D48' },
  { key: 'amber', name: '琥珀', color: '#D97706' },
  { key: 'slate', name: '石板', color: '#475569' },
] as const;

export default function Topbar() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { realName, clearAuth } = useAuthStore();
  const { themeMode, density, colorScheme, lang, face, setThemeMode, setDensity, setColorScheme, setFace, setLang } = useAppStore();
  const [open, setOpen] = useState<DropdownKey>(null);

  const toggleDropdown = (key: DropdownKey) => setOpen(open === key ? null : key);

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  const IconBtn = ({ icon: Icon, onClick, title, badge }: { icon: typeof Bell; onClick: () => void; title: string; badge?: boolean }) => (
    <button
      onClick={onClick}
      title={title}
      className="relative flex h-8 w-8 items-center justify-center rounded-md border border-gray-200 text-gray-500 transition-colors hover:border-primary-500 hover:text-primary-500"
    >
      <Icon size={16} />
      {badge && <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-danger-500" />}
    </button>
  );

  const dropdownCls = 'absolute right-0 top-10 z-50 min-w-[180px] rounded-lg border border-gray-200 bg-white p-1 shadow-hover animate-fade-in';

  return (
    <header className="flex h-14 items-center justify-between border-b border-gray-200 bg-white px-6">
      {/* Left: Face switch + Breadcrumb */}
      <div className="flex items-center gap-4">
        <div className="flex rounded-md bg-gray-100 p-0.5">
          <button
            onClick={() => setFace('admin')}
            className={clsx('rounded px-3 py-1 text-xs font-medium transition-colors', face === 'admin' ? 'bg-primary-500 text-white' : 'text-gray-400 hover:text-gray-600')}
          >
            {t('topbar.admin')}
          </button>
          <button
            onClick={() => setFace('portal')}
            className={clsx('rounded px-3 py-1 text-xs font-medium transition-colors', face === 'portal' ? 'bg-primary-500 text-white' : 'text-gray-400 hover:text-gray-600')}
          >
            {t('topbar.portal')}
          </button>
        </div>
        <div className="h-4 w-px bg-gray-200" />
        <div className="flex items-center gap-1.5 text-xs">
          <Home size={14} className="text-gray-400" />
          <span className="text-gray-400">{t('menu.workbench')}</span>
          <ChevronRight size={12} className="text-gray-300" />
          <span className="font-medium text-gray-700">{face === 'admin' ? t('menu.tenant') : t('menu.workbench')}</span>
        </div>
      </div>

      {/* Right: Theme → Density → Language → Notification → Profile */}
      <div className="flex items-center gap-2">
        {/* Theme */}
        <div className="relative">
          <button onClick={() => toggleDropdown('theme')} className="relative flex h-8 w-8 items-center justify-center rounded-md border border-gray-200 text-gray-500 hover:border-primary-500 hover:text-primary-500" title={t('topbar.theme')}>
            <Palette size={16} />
            <span className="absolute bottom-1 right-1 h-2 w-2 rounded-full border border-white" style={{ backgroundColor: schemes.find((s) => s.key === colorScheme)?.color }} />
          </button>
          {open === 'theme' && (
            <div className={dropdownCls}>
              <div className="px-2 py-1 text-[10px] font-semibold text-gray-400">{t('topbar.theme')}</div>
              {schemes.map((s) => (
                <button key={s.key} onClick={() => { setColorScheme(s.key as never); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs transition-colors hover:bg-gray-50" style={{ color: colorScheme === s.key ? '#2563EB' : '#374151' }}>
                  <span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: s.color }} />
                  <span className="flex-1 text-left">{s.name}</span>
                  {colorScheme === s.key && <CheckCircle size={13} />}
                </button>
              ))}
              <div className="my-1 h-px bg-gray-100" />
              <button onClick={() => { setThemeMode(themeMode === 'light' ? 'dark' : 'light'); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs text-gray-700 hover:bg-gray-50">
                {themeMode === 'light' ? <Moon size={13} /> : <Sun size={13} />}
                <span className="flex-1 text-left">{themeMode === 'light' ? t('topbar.dark') : t('topbar.light')}</span>
              </button>
            </div>
          )}
        </div>

        {/* Density */}
        <div className="relative">
          <IconBtn icon={Type} onClick={() => toggleDropdown('density')} title={t('topbar.density')} />
          {open === 'density' && (
            <div className={dropdownCls}>
              <div className="px-2 py-1 text-[10px] font-semibold text-gray-400">{t('topbar.density')}</div>
              {(['compact', 'standard', 'comfortable'] as const).map((dn) => (
                <button key={dn} onClick={() => { setDensity(dn); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs hover:bg-gray-50" style={{ color: density === dn ? '#2563EB' : '#374151' }}>
                  <span className="flex-1 text-left">{t(`topbar.${dn}`)}</span>
                  {density === dn && <CheckCircle size={13} />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Language */}
        <div className="relative">
          <IconBtn icon={Globe} onClick={() => toggleDropdown('lang')} title={t('topbar.language')} />
          {open === 'lang' && (
            <div className={dropdownCls}>
              <div className="px-2 py-1 text-[10px] font-semibold text-gray-400">{t('topbar.language')}</div>
              {[
                { key: 'zh-CN', label: '简体中文' },
                { key: 'en-US', label: 'English' },
              ].map((l) => (
                <button key={l.key} onClick={() => { setLang(l.key as never); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs hover:bg-gray-50" style={{ color: lang === l.key ? '#2563EB' : '#374151' }}>
                  <span className="flex-1 text-left">{l.label}</span>
                  {lang === l.key && <CheckCircle size={13} />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Notifications */}
        <IconBtn icon={Bell} onClick={() => {}} title={t('topbar.notifications')} badge />

        {/* Profile */}
        <div className="relative">
          <button onClick={() => toggleDropdown('profile')} className="flex items-center gap-2 rounded-full border border-gray-200 py-0.5 pl-0.5 pr-3 transition-colors hover:border-primary-500">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary-100">
              <User size={14} className="text-primary-700" />
            </div>
            <span className="text-xs font-medium text-gray-700">{realName || '管理员'}</span>
            <ChevronDown size={12} className="text-gray-400" />
          </button>
          {open === 'profile' && (
            <div className={dropdownCls}>
              <div className="flex items-center gap-2 border-b border-gray-100 px-3 py-2">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100">
                  <User size={16} className="text-primary-700" />
                </div>
                <div>
                  <div className="text-xs font-semibold text-gray-800">{realName || '超级管理员'}</div>
                  <div className="text-[10px] text-gray-400">集团总部 · L0</div>
                </div>
              </div>
              <div className="p-1">
                <button className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs text-gray-700 hover:bg-gray-50">
                  <User size={13} /><span className="flex-1 text-left">{t('topbar.profile')}</span>
                </button>
                <button className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs text-gray-700 hover:bg-gray-50">
                  <Settings size={13} /><span className="flex-1 text-left">{t('topbar.settings')}</span>
                </button>
                <div className="my-1 h-px bg-gray-100" />
                <button onClick={handleLogout} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs text-danger-500 hover:bg-danger-50">
                  <Power size={13} /><span className="flex-1 text-left">{t('topbar.logout')}</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
