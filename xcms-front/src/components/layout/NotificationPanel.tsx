import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Check, Trash2, X, Wifi, WifiOff } from 'lucide-react';
import { useNotificationStore, type NotificationItem } from '@/stores/notification';

// AT-23：面板数据来自 SSE 实时通知 store（useNotification 在 Topbar 订阅写入），
// 历史消息以消息中心页（/portal/message）为准。

type VisualType = 'todo' | 'notice' | 'alert';

const typeConfig: Record<VisualType, { color: string; bg: string }> = {
  todo: { color: 'var(--c-primary)', bg: 'var(--c-primary-50)' },
  notice: { color: 'var(--c-info)', bg: 'rgba(8,145,178,0.1)' },
  alert: { color: 'var(--c-warning)', bg: 'rgba(217,119,6,0.1)' },
};

/** 视觉分类：高优先级→alert；待办类编码→todo；其余→notice */
function visualType(n: NotificationItem): VisualType {
  if (n.priority === 'HIGH' || n.priority === 'URGENT') return 'alert';
  if (n.msgType === 'TODO') return 'todo';
  return 'notice';
}

/** 相对时间展示 */
function relativeTime(ts: number): string {
  const diff = Date.now() - ts;
  const minutes = Math.floor(diff / 60_000);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days === 1) return '昨天';
  return `${days}天前`;
}

export function NotificationPanel({ open, onClose }: { open: boolean; onClose: () => void }) {
  const navigate = useNavigate();
  const notifications = useNotificationStore((s) => s.notifications);
  const connectionState = useNotificationStore((s) => s.connectionState);
  const markRead = useNotificationStore((s) => s.markRead);
  const markAllRead = useNotificationStore((s) => s.markAllRead);
  const removeNotif = useNotificationStore((s) => s.remove);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const visible = filter === 'all' ? notifications : notifications.filter(n => !n.read);
  const unreadCount = notifications.filter(n => !n.read).length;
  const realtimeOn = connectionState === 'open';

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
            {/* 实时连接状态：open=实时；其余（重连/轮询降级）提示离线 */}
            <span title={realtimeOn ? '实时推送已连接' : '实时推送未连接（自动重连中）'}>
              {realtimeOn
                ? <Wifi size={12} style={{ color: 'var(--c-success, #16a34a)' }} />
                : <WifiOff size={12} style={{ color: 'var(--c-text-muted)' }} />}
            </span>
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
              const tc = typeConfig[visualType(n)];
              return (
                <div key={n.key} className="flex gap-3 border-b px-4 py-3 hover:bg-gray-50" style={{ borderColor: 'var(--c-border-light)', backgroundColor: !n.read ? 'var(--c-primary-50)' : undefined, opacity: n.read ? 0.5 : 1 }}>
                  <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg" style={{ backgroundColor: tc.bg }}>
                    <Bell size={14} style={{ color: tc.color }} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      {!n.read && <span className="h-2 w-2 rounded-full bg-danger-500" />}
                      <span className={`text-sm ${n.read ? 'font-normal text-gray-500' : 'font-medium text-gray-900'}`} style={{ color: n.read ? 'var(--c-text-muted)' : 'var(--c-text)' }}>{n.title}</span>
                    </div>
                    <p className="mt-0.5 truncate text-xs" style={{ color: 'var(--c-text-muted)' }}>{n.content}</p>
                    <span className="text-[11px] text-gray-400" style={{ color: 'var(--c-text-muted)' }}>
                      {relativeTime(n.receivedAt)}{n.senderName ? ` · ${n.senderName}` : ''}
                    </span>
                  </div>
                  <div className="flex flex-col gap-1">
                    {!n.read && <button onClick={() => markRead(n.key)} className="text-gray-300 hover:text-primary-500" title="标记已读"><Check size={13} /></button>}
                    <button onClick={() => removeNotif(n.key)} className="text-gray-300 hover:text-danger-500" title="删除"><Trash2 size={13} /></button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="border-t px-4 py-2.5 text-center" style={{ borderColor: 'var(--c-border-light)' }}>
          <button onClick={() => { onClose(); navigate('/portal/message'); }} className="text-xs text-primary-500 hover:underline">查看全部消息</button>
        </div>
      </div>
    </>
  );
}
