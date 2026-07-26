import { X } from 'lucide-react';

interface DrawerProps {
  open: boolean;
  onClose: () => void;
  title?: React.ReactNode;
  width?: number;
  children: React.ReactNode;
  footer?: React.ReactNode;
}

/**
 * 右侧滑出抽屉（见设计系统 §9.8），宽度默认 400 / lg 600
 */
export function Drawer({ open, onClose, title, width = 400, children, footer }: DrawerProps) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div className="absolute inset-0 bg-black/40 animate-fade-in" onClick={onClose} />
      <div
        className="relative h-full bg-white shadow-hover flex flex-col animate-slide-up"
        style={{ width }}
        role="dialog"
        aria-modal="true"
      >
        <div className="flex items-center justify-between px-[var(--card-p)] border-b border-gray-200" style={{ height: 'var(--bar-h)' }}>
          <h3 className="text-[length:var(--fs)] font-semibold text-gray-900">{title}</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-700 transition-colors p-1 rounded"
            aria-label="关闭"
          >
            <X size={18} />
          </button>
        </div>
        <div className="flex-1 overflow-auto p-[var(--card-p)]">{children}</div>
        {footer && (
          <div className="flex justify-end gap-[var(--gap-sm)] px-[var(--card-p)] py-[var(--gap-sm)] border-t border-gray-200">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
