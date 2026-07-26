import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Workflow, Bell, FolderOpen, User,
  Search, ChevronLeft,
} from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import clsx from 'clsx';

const iconMap: Record<string, typeof LayoutDashboard> = {
  LayoutDashboard, Workflow, Bell, FolderOpen, User,
};

export default function PortalSidebar() {
  const { portalMenus, hasPermission } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar } = useAppStore();
  const collapsed = sidebarCollapsed;

  const visibleGroups = portalMenus
    .filter((g) => g.visible !== false)
    .map((g) => ({
      ...g,
      children: (g.children ?? []).filter(
        (item) => item.type !== 'BUTTON' && item.visible !== false && hasPermission(item.permission)
      ),
    }))
    .filter((g) => g.children.length > 0);

  return (
    <aside
      className={clsx('flex flex-col border-r border-gray-200 bg-white transition-all duration-200', collapsed ? 'w-16' : 'w-60')}
      style={{ position: 'sticky', top: 0, height: '100vh', flexShrink: 0 }}
    >
      <div className="flex h-14 items-center gap-2.5 border-b border-gray-200 px-4">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary-500 to-primary-600 shadow-md shadow-primary-500/30">
          <span className="text-sm font-bold text-white">X</span>
        </div>
        {!collapsed && (
          <div>
            <div className="text-sm font-bold leading-none text-gray-900">XCMS</div>
            <div className="mt-1 text-[10px] text-gray-400">业务面</div>
          </div>
        )}
      </div>

      {!collapsed && (
        <div className="p-3">
          <div className="relative">
            <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
            <input placeholder="搜索菜单..." className="h-7 w-full rounded-md border border-gray-200 bg-gray-50 pl-8 pr-12 text-xs text-gray-700 outline-none focus:border-primary-500" />
            <kbd className="absolute right-2 top-1/2 -translate-y-1/2 rounded border border-gray-200 bg-white px-1 py-0.5 text-[10px] text-gray-400">⌘K</kbd>
          </div>
        </div>
      )}

      <nav className="flex-1 overflow-y-auto py-2">
        {visibleGroups.map((group) => (
          <div key={group.key} className="mb-1">
            {!collapsed && <div className="px-4 pb-1 pt-2 text-[10px] font-semibold uppercase tracking-wide text-gray-400">{group.label}</div>}
            {group.children.map((item) => {
              const Icon = iconMap[item.icon || ''] || LayoutDashboard;
              return (
                <NavLink key={item.key} to={item.path || '#'} className={({ isActive }) => clsx('flex h-9 items-center gap-3 border-l-2 px-4 text-[13px] transition-colors duration-150', isActive ? 'border-primary-500 bg-primary-50 font-medium text-primary-700' : 'border-transparent text-gray-500 hover:bg-gray-50 hover:text-gray-700')}>
                  <Icon size={16} className="flex-shrink-0" />
                  {!collapsed && <span className="flex-1 text-left">{item.label}</span>}
                </NavLink>
              );
            })}
          </div>
        ))}
      </nav>

      {/* Collapse */}
      <button onClick={toggleSidebar} className="flex h-9 items-center justify-center border-t border-gray-200 text-gray-400 hover:bg-gray-50 hover:text-gray-600">
        <ChevronLeft size={16} className={clsx('transition-transform', collapsed && 'rotate-180')} />
      </button>
    </aside>
  );
}
