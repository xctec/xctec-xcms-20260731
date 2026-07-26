import { create } from 'zustand';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'warning' | 'info';
interface ToastItem { id: number; type: ToastType; message: string; }

interface ToastStore {
  toasts: ToastItem[];
  show: (type: ToastType, message: string) => void;
  remove: (id: number) => void;
}

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  show: (type, message) => {
    const id = Date.now() + Math.random();
    set((s) => ({ toasts: [...s.toasts, { id, type, message }] }));
    setTimeout(() => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })), type === 'error' ? 5000 : 3000);
  },
  remove: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}));

const config: Record<ToastType, { icon: typeof CheckCircle; color: string }> = {
  success: { icon: CheckCircle, color: 'var(--c-success)' },
  error: { icon: XCircle, color: 'var(--c-danger)' },
  warning: { icon: AlertTriangle, color: 'var(--c-warning)' },
  info: { icon: Info, color: 'var(--c-info)' },
};

export const toast = {
  success: (msg: string) => useToastStore.getState().show('success', msg),
  error: (msg: string) => useToastStore.getState().show('error', msg),
  warning: (msg: string) => useToastStore.getState().show('warning', msg),
  info: (msg: string) => useToastStore.getState().show('info', msg),
};

export function ToastContainer() {
  const { toasts, remove } = useToastStore();
  return (
    <div className="fixed top-4 left-1/2 z-[100] flex -translate-x-1/2 flex-col gap-2">
      {toasts.map((t) => {
        const c = config[t.type];
        return (
          <div key={t.id} className="animate-slide-up flex items-center gap-3 rounded-lg px-4 py-3 shadow-lg" style={{ backgroundColor: 'var(--c-card)', border: `1px solid var(--c-border)`, minWidth: 320 }}>
            <c.icon size={18} style={{ color: c.color }} />
            <span className="flex-1 text-sm" style={{ color: 'var(--c-text)' }}>{t.message}</span>
            <button onClick={() => remove(t.id)} className="text-gray-400 hover:text-gray-600"><X size={15} /></button>
          </div>
        );
      })}
    </div>
  );
}
