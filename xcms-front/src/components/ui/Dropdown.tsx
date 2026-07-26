import { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import type { LucideIcon } from 'lucide-react';
import { Check } from 'lucide-react';

export interface DropdownItem {
  key: string;
  label: React.ReactNode;
  icon?: LucideIcon;
  disabled?: boolean;
  danger?: boolean;
  divider?: boolean;
  selected?: boolean;
  onClick?: () => void;
}

interface DropdownProps {
  trigger: React.ReactNode;
  items: DropdownItem[];
  align?: 'left' | 'right';
  minWidth?: number;
}

/**
 * 下拉菜单（见设计系统 §9.10），用于顶栏图标按钮与行内更多操作
 */
export function Dropdown({ trigger, items, align = 'right', minWidth = 180 }: DropdownProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div className="relative inline-block" ref={ref}>
      <div className="inline-flex cursor-pointer" onClick={() => setOpen((o) => !o)}>
        {trigger}
      </div>
      {open && (
        <div
          className={clsx(
            'absolute z-50 mt-1 bg-white border border-gray-200 rounded-md shadow-hover py-1 animate-fade-in',
            align === 'right' ? 'right-0' : 'left-0'
          )}
          style={{ minWidth }}
        >
          {items.map((it) =>
            it.divider ? (
              <div key={it.key} className="my-1 border-t border-gray-200" />
            ) : (
              <button
                key={it.key}
                disabled={it.disabled}
                onClick={() => {
                  it.onClick?.();
                  setOpen(false);
                }}
                className={clsx(
                  'w-full flex items-center gap-2 px-2 py-1.5 text-[length:var(--fs-sm)] text-left rounded-sm transition-colors disabled:opacity-50',
                  it.selected ? 'bg-primary-50 text-primary-500' : 'text-gray-600 hover:bg-gray-100',
                  it.danger && 'text-danger-500 hover:bg-danger-50'
                )}
              >
                {it.icon && <it.icon size={14} />}
                <span className="flex-1">{it.label}</span>
                {it.selected && <Check size={14} />}
              </button>
            )
          )}
        </div>
      )}
    </div>
  );
}
