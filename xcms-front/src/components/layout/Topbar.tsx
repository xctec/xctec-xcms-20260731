import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Home, ChevronRight, Bell, Moon, Sun, Palette,
  ChevronDown, User, Settings, Power, CheckCircle,
} from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import { NotificationPanel } from './NotificationPanel';
import { PreferenceDrawer } from './PreferenceDrawer';
import clsx from 'clsx';

type DropdownKey = 'theme' | 'profile' | null;

export default function Topbar() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { realName, tenantName, clearAuth } = useAuthStore();
  const { themeMode, setThemeMode, face, setFace } = useAppStore();
  const [open, setOpen] = useState<DropdownKey>(null);
  const [notifOpen, setNotifOpen] = useState(false);
  const [prefOpen, setPrefOpen] = useState(false);

  const handleLogout = () => { clearAuth(); navigate('/login'); };

  const IconBtn = ({ icon: Icon, onClick, title, badge, active }: { icon: typeof Bell; onClick: () => void; title: string; badge?: boolean; active?: boolean }) => (
    <button
      onClick={onClick}
      title={title}
      className={clsx('relative flex h-8 w-8 items-center justify-center rounded-md border transition-colors',
        active ? 'border-primary-500 text-primary-500' : 'border-gray-200 text-gray-500 hover:border-primary-500 hover:text-primary-500'
      )}
      style={{ borderColor: active ? 'var(--c-primary)' : 'var(--c-border)', color: active ? 'var(--c-primary)' : 'var(--c-text-sec)' }}
    >
      <Icon size={16} />
      {badge && <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-danger-500" />}
    </button>
  );

  const dropdownCls = 'absolute right-0 top-10 z-50 min-w-[200px] rounded-lg border p-1 shadow-lg animate-fade-in';

  // 面包屑
  const pathSegs = location.pathname.split('/').filter(Boolean);
  const current = pathSegs[pathSegs.length - 1] || 'workbench';

  return (
    <>
      <header className="flex h-14 items-center justify-between border-b px-6" style={{ backgroundColor: 'var(--c-card)', borderColor: 'var(--c-border)' }}>
        {/* Left: Face switch + Breadcrumb */}
        <div className="flex items-center gap-4">
          <div className="flex rounded-md p-0.5" style={{ backgroundColor: 'var(--c-header-bg)' }}>
            <button onClick={() => { setFace('admin'); navigate('/admin'); }} className={clsx('rounded px-3 py-1 text-xs font-medium transition-colors', face === 'admin' ? 'text-white' : 'text-gray-400 hover:text-gray-600')} style={{ backgroundColor: face === 'admin' ? 'var(--c-primary)' : 'transparent' }}>
              {t('topbar.admin')}
            </button>
            <button onClick={() => { setFace('portal'); navigate('/portal'); }} className={clsx('rounded px-3 py-1 text-xs font-medium transition-colors', face === 'portal' ? 'text-white' : 'text-gray-400 hover:text-gray-600')} style={{ backgroundColor: face === 'portal' ? 'var(--c-primary)' : 'transparent' }}>
              {t('topbar.portal')}
            </button>
          </div>
          <div className="h-4 w-px" style={{ backgroundColor: 'var(--c-border)' }} />
          <div className="flex items-center gap-1.5 text-xs">
            <Home size={14} style={{ color: 'var(--c-text-muted)' }} />
            <span style={{ color: 'var(--c-text-muted)' }}>{face === 'admin' ? t('topbar.admin') : t('topbar.portal')}</span>
            <ChevronRight size={12} style={{ color: 'var(--c-text-muted)' }} />
            <span className="font-medium" style={{ color: 'var(--c-text)' }}>{t(`menu.${current}`) || current}</span>
          </div>
        </div>

        {/* Right: Theme toggle → Notifications → Preferences → Profile */}
        <div className="flex items-center gap-2">
          {/* Theme toggle */}
          <IconBtn icon={themeMode === 'light' ? Moon : Sun} onClick={() => setThemeMode(themeMode === 'light' ? 'dark' : 'light')} title={themeMode === 'light' ? t('topbar.dark') : t('topbar.light')} />

          {/* Notifications */}
          <IconBtn icon={Bell} onClick={() => setNotifOpen(!notifOpen)} title={t('topbar.notifications')} badge />

          {/* Preferences */}
          <IconBtn icon={Palette} onClick={() => setPrefOpen(true)} title="偏好设置" />

          {/* Profile */}
          <div className="relative">
            <button onClick={() => setOpen(open === 'profile' ? null : 'profile')} className="flex items-center gap-2 rounded-full border py-0.5 pl-0.5 pr-3 transition-colors hover:border-primary-500" style={{ borderColor: 'var(--c-border)' }}>
              <div className="flex h-7 w-7 items-center justify-center rounded-full" style={{ backgroundColor: 'var(--c-primary-100)' }}>
                <User size={14} style={{ color: 'var(--c-primary-700)' }} />
              </div>
              <span className="text-xs font-medium" style={{ color: 'var(--c-text)' }}>{realName || '管理员'}</span>
              <ChevronDown size={12} style={{ color: 'var(--c-text-muted)' }} />
            </button>
            {open === 'profile' && (
              <div className={dropdownCls} style={{ backgroundColor: 'var(--c-card)', borderColor: 'var(--c-border)' }}>
                <div className="flex items-center gap-2 border-b px-3 py-2.5" style={{ borderColor: 'var(--c-border-light)' }}>
                  <div className="flex h-9 w-9 items-center justify-center rounded-full" style={{ backgroundColor: 'var(--c-primary-100)' }}>
                    <User size={16} style={{ color: 'var(--c-primary-700)' }} />
                  </div>
                  <div>
                    <div className="text-xs font-semibold" style={{ color: 'var(--c-text)' }}>{realName || '超级管理员'}</div>
                    <div className="text-[10px]" style={{ color: 'var(--c-text-muted)' }}>{tenantName || '集团总部'} · L0</div>
                  </div>
                </div>
                <div className="p-1">
                  <button onClick={() => { navigate('/portal/profile'); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs hover:bg-gray-50" style={{ color: 'var(--c-text)' }}>
                    <User size={13} /><span className="flex-1 text-left">{t('topbar.profile')}</span>
                  </button>
                  <button onClick={() => { setPrefOpen(true); setOpen(null); }} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs hover:bg-gray-50" style={{ color: 'var(--c-text)' }}>
                    <Settings size={13} /><span className="flex-1 text-left">{t('topbar.settings')}</span>
                  </button>
                  <div className="my-1 h-px" style={{ backgroundColor: 'var(--c-border-light)' }} />
                  <button onClick={handleLogout} className="flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs hover:bg-danger-50" style={{ color: 'var(--c-danger)' }}>
                    <Power size={13} /><span className="flex-1 text-left">{t('topbar.logout')}</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </header>

      <NotificationPanel open={notifOpen} onClose={() => setNotifOpen(false)} />
      <PreferenceDrawer open={prefOpen} onClose={() => setPrefOpen(false)} />
    </>
  );
}
