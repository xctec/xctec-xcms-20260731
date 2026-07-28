import { useEffect } from 'react';
import { useQueryClient, type QueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/auth';
import {
  useNotificationStore,
  type NotificationPayload,
} from '@/stores/notification';

/**
 * 实时通知 hook（AT-23）。
 *
 * 订阅后端 SSE `/api/notifications/stream`（AT-20/21），收到 `notification`
 * 事件后写入通知 store（驱动铃铛角标/通知面板）并失效消息中心查询缓存
 * （queryKey `['messages']`，与 TanStack Query 协同刷新）。
 *
 * - 传输：后端鉴权仅认 `Authorization: Bearer` 请求头，原生 EventSource 无法
 *   携带请求头，故用 `fetch` + ReadableStream 手动解析 SSE 帧（浏览器原生能力，
 *   零依赖）；心跳注释行（`:`开头）自动忽略。
 * - 自动重连：连接断开/失败后指数退避重建（1s 起倍增，上限 30s）。
 * - 降级轮询：连续失败达 {@link MAX_FAILURES_BEFORE_POLLING} 次进入轮询兜底
 *   （每 {@link POLLING_INTERVAL_MS} 失效一次消息查询缓存），同时以低频继续
 *   尝试恢复 SSE，成功后自动退出轮询。
 * - 单连接守卫：模块级引用计数，多处调用/StrictMode 双挂载也只维持一条连接。
 * - 单体/拆分行为一致：前端不感知后端实例数（多实例广播由 AT-22 负责）。
 *
 * 用法：在常驻布局组件（如 Topbar）调用一次；任意组件可直接读
 * `useNotificationStore` 获取通知与未读数。
 */
export function useNotification() {
  const token = useAuthStore((s) => s.token);
  const queryClient = useQueryClient();
  const notifications = useNotificationStore((s) => s.notifications);
  const connectionState = useNotificationStore((s) => s.connectionState);
  const markRead = useNotificationStore((s) => s.markRead);
  const markAllRead = useNotificationStore((s) => s.markAllRead);
  const remove = useNotificationStore((s) => s.remove);

  useEffect(() => {
    if (!token) return;
    return acquireStream(queryClient);
  }, [token, queryClient]);

  return {
    notifications,
    unreadCount: notifications.filter((n) => !n.read).length,
    connectionState,
    markRead,
    markAllRead,
    remove,
  };
}

// ---------------- 模块级单例连接管理 ----------------

const STREAM_URL = '/api/notifications/stream';
/** 消息中心查询缓存 key（消息页接入 API 时使用同一 key 即可协同刷新） */
const MESSAGES_QUERY_KEY = ['messages'] as const;
/** 退避基数 / 上限 */
const BACKOFF_BASE_MS = 1_000;
const BACKOFF_MAX_MS = 30_000;
/** 连续失败多少次后进入降级轮询 */
const MAX_FAILURES_BEFORE_POLLING = 5;
/** 轮询兜底间隔 */
const POLLING_INTERVAL_MS = 30_000;
/** 轮询模式下重试 SSE 的间隔 */
const POLLING_RETRY_SSE_MS = 60_000;

let refCount = 0;
let abortController: AbortController | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let pollingTimer: ReturnType<typeof setInterval> | null = null;
let failureCount = 0;

/** 引用计数获取连接：首个调用方建连，返回释放函数（末个释放方断连） */
function acquireStream(queryClient: QueryClient): () => void {
  refCount += 1;
  if (refCount === 1) {
    failureCount = 0;
    connect(queryClient);
  }
  return () => {
    refCount -= 1;
    if (refCount === 0) {
      teardown();
      useNotificationStore.getState().setConnectionState('idle');
    }
  };
}

function teardown() {
  abortController?.abort();
  abortController = null;
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  stopPolling();
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
}

/** 建立 SSE 连接（fetch 流式解析），断开后按策略重连/降级 */
async function connect(queryClient: QueryClient) {
  const { setConnectionState } = useNotificationStore.getState();
  const { token, tenantId } = useAuthStore.getState();
  if (!token || refCount === 0) return;

  setConnectionState(failureCount > 0 ? 'reconnecting' : 'connecting');
  abortController = new AbortController();

  try {
    const res = await fetch(STREAM_URL, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${token}`,
        ...(tenantId ? { 'X-Tenant-Id': String(tenantId) } : {}),
      },
      credentials: 'include',
      signal: abortController.signal,
    });
    if (!res.ok || !res.body) throw new Error(`SSE HTTP ${res.status}`);

    // 建连成功：重置退避、退出轮询兜底
    failureCount = 0;
    stopPolling();
    setConnectionState('open');

    await readSseStream(res.body, (eventName, data) => {
      if (eventName !== 'notification') return; // connected 握手、心跳等忽略
      try {
        const payload = JSON.parse(data) as NotificationPayload;
        useNotificationStore.getState().push(payload);
        // 与消息中心/待办协同：失效查询缓存触发重取
        queryClient.invalidateQueries({ queryKey: MESSAGES_QUERY_KEY });
      } catch {
        // 载荷解析失败忽略该帧
      }
    });
    // 流正常结束（如服务端超时关闭）→ 走重连
    throw new Error('SSE stream closed');
  } catch (err) {
    if (abortController?.signal.aborted || refCount === 0) return; // 主动关闭
    failureCount += 1;
    scheduleReconnect(queryClient);
  }
}

/** 指数退避重连；连续失败过多则降级轮询 + 低频重试 SSE */
function scheduleReconnect(queryClient: QueryClient) {
  const { setConnectionState } = useNotificationStore.getState();

  if (failureCount >= MAX_FAILURES_BEFORE_POLLING) {
    setConnectionState('polling');
    if (!pollingTimer) {
      pollingTimer = setInterval(() => {
        // 降级兜底：周期性失效消息查询缓存，靠 HTTP 拉取保持基本可用
        queryClient.invalidateQueries({ queryKey: MESSAGES_QUERY_KEY });
      }, POLLING_INTERVAL_MS);
    }
    reconnectTimer = setTimeout(() => connect(queryClient), POLLING_RETRY_SSE_MS);
    return;
  }

  setConnectionState('reconnecting');
  const delay = Math.min(BACKOFF_BASE_MS * 2 ** (failureCount - 1), BACKOFF_MAX_MS);
  reconnectTimer = setTimeout(() => connect(queryClient), delay);
}

/** 按 SSE 协议解析字节流：`event:`/`data:` 行累积，空行分发一帧，`:` 注释忽略 */
async function readSseStream(
  body: ReadableStream<Uint8Array>,
  onEvent: (eventName: string, data: string) => void
) {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let eventName = 'message';
  let data = '';

  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    let idx: number;
    while ((idx = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, idx).replace(/\r$/, '');
      buffer = buffer.slice(idx + 1);

      if (line === '') {
        // 空行 = 一帧结束
        if (data) onEvent(eventName, data);
        eventName = 'message';
        data = '';
      } else if (line.startsWith('event:')) {
        eventName = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        data += (data ? '\n' : '') + line.slice(5).trimStart();
      }
      // 其余（`:` 心跳注释、id: 等）忽略
    }
  }
}
