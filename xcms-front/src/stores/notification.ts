import { create } from 'zustand';

/** 后端 SSE `notification` 事件载荷（对应 NotificationPayload，AT-21） */
export interface NotificationPayload {
  messageId?: number;
  msgCode?: string;
  title?: string;
  content?: string;
  senderName?: string;
  msgType?: string; // NOTICE/EMAIL/SMS
  priority?: string; // LOW/NORMAL/HIGH/URGENT
}

/** 通知中心条目（实时接收，会话内存态；历史消息以消息中心页为准） */
export interface NotificationItem {
  /** 本地唯一键（messageId 或时间戳生成） */
  key: string;
  messageId?: number;
  title: string;
  content: string;
  senderName?: string;
  msgType?: string;
  priority?: string;
  /** 收到时间戳（ms） */
  receivedAt: number;
  read: boolean;
}

/** SSE 连接状态：connecting 建连中 / open 已连接 / reconnecting 退避重连 / polling 降级轮询 / idle 未启动 */
export type NotificationConnectionState =
  | 'idle'
  | 'connecting'
  | 'open'
  | 'reconnecting'
  | 'polling';

/** 内存中最多保留的通知条数（防止长会话膨胀） */
const MAX_ITEMS = 100;

interface NotificationState {
  notifications: NotificationItem[];
  connectionState: NotificationConnectionState;
  /** 追加一条实时通知（按 messageId 去重，新的在前） */
  push: (payload: NotificationPayload) => void;
  markRead: (key: string) => void;
  markAllRead: () => void;
  remove: (key: string) => void;
  clear: () => void;
  setConnectionState: (state: NotificationConnectionState) => void;
}

export const useNotificationStore = create<NotificationState>()((set) => ({
  notifications: [],
  connectionState: 'idle',
  push: (payload) =>
    set((s) => {
      if (
        payload.messageId != null &&
        s.notifications.some((n) => n.messageId === payload.messageId)
      ) {
        return s; // 重连补发等场景按 messageId 去重
      }
      const item: NotificationItem = {
        key: payload.messageId != null ? `m-${payload.messageId}` : `t-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        messageId: payload.messageId,
        title: payload.title ?? '新通知',
        content: payload.content ?? '',
        senderName: payload.senderName,
        msgType: payload.msgType,
        priority: payload.priority,
        receivedAt: Date.now(),
        read: false,
      };
      return { notifications: [item, ...s.notifications].slice(0, MAX_ITEMS) };
    }),
  markRead: (key) =>
    set((s) => ({
      notifications: s.notifications.map((n) => (n.key === key ? { ...n, read: true } : n)),
    })),
  markAllRead: () =>
    set((s) => ({ notifications: s.notifications.map((n) => ({ ...n, read: true })) })),
  remove: (key) =>
    set((s) => ({ notifications: s.notifications.filter((n) => n.key !== key) })),
  clear: () => set({ notifications: [] }),
  setConnectionState: (connectionState) => set({ connectionState }),
}));
