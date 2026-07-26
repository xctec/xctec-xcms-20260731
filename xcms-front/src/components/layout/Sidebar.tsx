import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Building2, Network, Users, Shield, Bell, Settings,
  Search, ChevronLeft, User, BarChart3, FileText, FolderOpen,
  Workflow, Clock, Network as NetIcon,
} from 'lucide-react';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import clsx from 'clsx';

const iconMap: Record<string, typeof Building2> = {
  Building2, Network, Users, Shield, Bell, Settings, BarChart3, FileText, FolderOpen, Workflow, Clock,
};

// 菜单数据（后续由 menuApi.getUserMenus() 动态获取）
const mockMenus = [
  {
    titleKey: '租户与组织',
    items: [
      { icon: 'Building2', labelKey: '租户管理', path: '/admin/tenant', badge: '9' },
      { icon: 'Network', labelKey: '组织架构', path: '/admin/organization' },
      { icon: 'Users', labelKey: '用户管理', path: '/admin/user' },
    ],
  },
  {
    titleKey: '安全与权限',
    items: [
      { icon: 'Shield', labelKey: '权限管理', path: '/admin/permission' },
    ],
  },
  {
    titleKey: '业务流程',
    items: [
      { icon: 'Workflow', labelKey: '流程管理', path: '/admin/workflow' },
    ],
  },
  {
    titleKey: '运营',
    items: [
      { icon: 'BarChart3', labelKey: '运营看板', path: '/admin/operation' },
      { icon: 'Bell', labelKey: '消息中心', path: '/admin/message', badge: '3' },
      { icon: 'FolderOpen', labelKey: '文件管理', path: '/admin/file' },
    ],
  },
  {
    titleKey: '系统',
    items: [
      { icon: 'Settings', labelKey: '配置管理', path: '/admin/config' },
      { icon: 'Clock', labelKey: '任务调度', path: '/admin/task' },
      { icon: 'FileText', labelKey: '审计日志', path: '/admin/audit' },
    ],
  },
];

export default function Sidebar() {
  const { realName, tenantName } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar } = useAppStore();
  const collapsed = sidebarCollapsed;

  return (
    <aside
      className={clsx(
        'flex flex-col border-r border-gray-200 bg-white transition-all duration-200',
        collapsed ? 'w-16' : 'w-60'
      )}
      style={{ position: 'sticky', top: 0, height: '100vh', flexShrink: 0 }}
    >
      {/* Logo */}
      <div className="flex h-14 items-center gap-2.5 border-b border-gray-200 px-4">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary-500 to-primary-600 shadow-md shadow-primary-500/30">
          <span className="text-sm font-bold text-white">X</span>
        </div>
        {!collapsed && (
          <div>
            <div className="text-sm font-bold leading-none text-gray-900">XCMS</div>
            <div className="mt-1 text-[10px] text-gray-400">集团中台</div>
          </div>
        )}
      </div>

      {/* Search */}
      {!collapsed && (
        <div className="p-3">
          <div className="relative">
            <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              placeholder="搜索菜单..."
              className="h-7 w-full rounded-md border border-gray-200 bg-gray-50 pl-8 pr-12 text-xs text-gray-700 outline-none focus:border-primary-500"
            />
            <kbd className="absolute right-2 top-1/2 -translate-y-1/2 rounded border border-gray-200 bg-white px-1 py-0.5 text-[10px] text-gray-400">⌘K</kbd>
          </div>
        </div>
      )}

      {/* Menu */}
      <nav className="flex-1 overflow-y-auto py-2">
        {mockMenus.map((group, gi) => (
          <div key={gi} className="mb-1">
            {!collapsed && (
              <div className="px-4 pb-1 pt-2 text-[10px] font-semibold uppercase tracking-wide text-gray-400">{group.titleKey}</div>
            )}
            {group.items.map((item, i) => {
              const Icon = iconMap[item.icon] || Building2;
              return (
                <NavLink
                  key={i}
                  to={item.path}
                  className={({ isActive }) =>
                    clsx(
                      'flex h-9 items-center gap-3 border-l-2 px-4 text-[13px] transition-colors duration-150',
                      isActive
                        ? 'border-primary-500 bg-primary-50 font-medium text-primary-700'
                        : 'border-transparent text-gray-500 hover:bg-gray-50 hover:text-gray-700'
                    )
                  }
                >
                  <Icon size={16} className="flex-shrink-0" />
                  {!collapsed && (
                    <>
                      <span className="flex-1 text-left">{item.labelKey}</span>
                      {item.badge && (
                        <span className="rounded-full bg-gray-100 px-1.5 py-0.5 text-[10px] font-semibold text-gray-500">{item.badge}</span>
                      )}
                    </>
                  )}
                </NavLink>
              );
            })}
          </div>
        ))}
      </nav>

      {/* Collapse */}
      <button
        onClick={toggleSidebar}
        className="flex h-9 items-center justify-center border-t border-gray-200 text-gray-400 hover:bg-gray-50 hover:text-gray-600"
      >
        <ChevronLeft size={16} className={clsx('transition-transform', collapsed && 'rotate-180')} />
      </button>
    </aside>
  );
}
