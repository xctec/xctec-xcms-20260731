import { useState } from 'react';
import { Bell, Check, Trash2, X } from 'lucide-react';

interface NotificationItem {
  id: number;
  title: string;
  content: string;
  time: string;
  type: 'todo' | 'notice' | 'alert';
  read: boolean;
}

const mockNotifications: NotificationItem[] = [
  { id: 1, title: '采购审批待处理', content: '您有一条采购审批任务待处理', time: '10分钟前', type: 'todo', read: false },
  { id: 2, title: '系统维护通知', content: '系统将于今晚22:00-23:00维护', time: '1小时前', type: 'notice', read: false },
  { id: 3, title: '密码即将过期', content: '您的密码将在7天后过期', time: '3小时前', type: 'alert', read: false },
  { id: 4, title: '新同事入职', content: '欢迎新同事加入团队', time: '5小时前', type: 'notice', read: true },
  { id: 5, title: '报销审批超时', content: '报销审批已超时24小时', time: '昨天', type: 'alert', read: true },
];

const typeConfig = {
  todo: { color: 'var(--c-primary)', bg: 'var(--c-primary-50)' },
  notice: { color: 'var(--c-info)', bg: 'rgba(8,145,178,0.1)' },
  alert: { color: 'var(--c-warning)', bg: 'rgba(217,119,6,0.1)' },
};

export function NotificationPanel({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [notifications, setNotifications] = useState(mockNotifications);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const visible = filter === 'all' ? notifications : notifications.filter(n => !n.read);
  const unreadCount = notifications.filter(n => !n.read).length;

  const markRead = (id: number) => setNotifications(ns => ns.map(n => n.id === id ? { ...n, read: true } : n));
  const markAllRead = () => setNotifications(ns => ns.map(n => ({ ...n, read: true })));
  const removeNotif = (id: number) => setNotifications(ns => ns.filter(n => n.id !== id));

  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 z-40" onClick={onClose} />
      <div className="fixed right-4 top-14 z-50 w-96 animate-slide-up overflow-hidden rounded-lg shadow-2xl" style={{ backgroundColor: 'var(--c-card)', border: '1px solid var(--c-border)' }}>
        {/* Header */}
        <div className="flex items-center justify-between border-b px-4 py-3" style={{ borderColor: 'var(--c-border-light)' }}>
          <div className="flex items-center gap-2">
            <Bell size={16} style={{ color: 'var(--c-primary)' }} />
            <span className="text-sm font-semibold" style={{ color: 'var(--c-text)' }}>通知</span>
            {unreadCount > 0 && <span className="rounded-full px-1.5 text-[10px] font-semibold text-white" style={{ backgroundColor: 'var(--c-danger)' }}>{unreadCount}</span>}
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
        </div>

        {/* Filter */}
        <div className="flex gap-2 border-b px-4 py-2" style={{ borderColor: 'var(--c-border-light)' }}>
          <button onClick={() => setFilter('all')} className={`rounded-full px-3 py-1 text-xs font-medium ${filter === 'all' ? 'text-white' : 'text-gray-500'}`} style={{ backgroundColor: filter === 'all' ? 'var(--c-primary)' : 'var(--c-code-bg)' }}>全部</button>
          <button onClick={() => setFilter('unread')} className={`rounded-full px-3 py-1 text-xs font-medium ${filter === 'unread' ? 'text-white' : 'text-gray-500'}`} style={{ backgroundColor: filter === 'unread' ? 'var(--c-primary)' : 'var(--c-code-bg)' }}>未读 ({unreadCount})</button>
          <button onClick={markAllRead} className="ml-auto flex items-center gap-1 text-xs text-gray-400 hover:text-primary-500"><Check size={12} />全部已读</button>
        </div>

        {/* List */}
        <div className="max-h-96 overflow-y-auto">
          {visible.length === 0 ? (
            <div className="py-12 text-center text-sm text-gray-400">暂无通知</div>
          ) : (
            visible.map(n => {
              const tc = typeConfig[n.type];
              return (
                <div key={n.id} className="flex gap-3 border-b px-4 py-3 hover:bg-gray-50" style={{ borderColor: 'var(--c-border-light)', backgroundColor: !n.read ? 'var(--c-primary-50)' : undefined, opacity: 0.5 }}>
                  <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg" style={{ backgroundColor: tc.bg }}>
                    <Bell size={14} style={{ color: tc.color }} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      {!n.read && <span className="h-2 w-2 rounded-full bg-danger-500" />}
                      <span className={`text-sm ${n.read ? 'font-normal text-gray-500' : 'font-medium text-gray-900'}`} style={{ color: n.read ? 'var(--c-text-muted)' : 'var(--c-text)' }}>{n.title}</span>
                    </div>
                    <p className="mt-0.5 truncate text-xs" style={{ color: 'var(--c-text-muted)' }}>{n.content}</p>
                    <span className="text-[11px] text-gray-400" style={{ color: 'var(--c-text-muted)' }}>{n.time}</span>
                  </div>
                  <div className="flex flex-col gap-1">
                    {!n.read && <button onClick={() => markRead(n.id)} className="text-gray-300 hover:text-primary-500" title="标记已读"><Check size={13} /></button>}
                    <button onClick={() => removeNotif(n.id)} className="text-gray-300 hover:text-danger-500" title="删除"><Trash2 size={13} /></button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="border-t px-4 py-2.5 text-center" style={{ borderColor: 'var(--c-border-light)' }}>
          <button className="text-xs text-primary-500 hover:underline">查看全部消息</button>
        </div>
      </div>
    </>
  );
}
