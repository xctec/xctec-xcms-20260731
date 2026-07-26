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
  /** 菜单最大高度（px）；超出出现滚动条 */
  maxHeight?: number;
}

/**
 * 下拉菜单（见设计系统 §9.10），用于顶栏图标按钮与行内更多操作
 */
export function Dropdown({ trigger, items, align = 'right', minWidth = 180, maxHeight }: DropdownProps) {
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const rootRef = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLDivElement>(null);

  // 仅可交互（非分隔符/禁用）项参与键盘导航
  const enabledIdx = items
    .map((it, i) => (it.divider || it.disabled ? -1 : i))
    .filter((i) => i >= 0);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // 打开时聚焦首个可选项
  useEffect(() => {
    if (!open) return;
    const first = enabledIdx[0];
    if (first == null) return;
    setActive(first);
    requestAnimationFrame(() => {
      const pos = enabledIdx.indexOf(first);
      menuRef.current?.querySelectorAll<HTMLButtonElement>('[role="menuitem"]')[pos]?.focus();
    });
  }, [open]); // eslint-disable-line react-hooks/exhaustive-deps

  const focusAt = (idxInItems: number) => {
    const pos = enabledIdx.indexOf(idxInItems);
    if (pos < 0) return;
    requestAnimationFrame(() => {
      menuRef.current?.querySelectorAll<HTMLButtonElement>('[role="menuitem"]')[pos]?.focus();
    });
  };

  const move = (dir: 1 | -1) => {
    if (enabledIdx.length === 0) return;
    const pos = enabledIdx.indexOf(active);
    const nextPos = (pos + dir + enabledIdx.length) % enabledIdx.length;
    const nextIdx = enabledIdx[nextPos];
    setActive(nextIdx);
    focusAt(nextIdx);
  };

  const closeAndFocusTrigger = () => {
    setOpen(false);
    triggerRef.current?.focus();
  };

  const onMenuKeyDown = (e: React.KeyboardEvent) => {
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        move(1);
        break;
      case 'ArrowUp':
        e.preventDefault();
        move(-1);
        break;
      case 'Home':
        e.preventDefault();
        if (enabledIdx[0] != null) {
          setActive(enabledIdx[0]);
          focusAt(enabledIdx[0]);
        }
        break;
      case 'End':
        e.preventDefault();
        if (enabledIdx.length) {
          const last = enabledIdx[enabledIdx.length - 1];
          setActive(last);
          focusAt(last);
        }
        break;
      case 'Escape':
        e.preventDefault();
        closeAndFocusTrigger();
        break;
      case 'Tab':
        setOpen(false);
        break;
    }
  };

  const onTriggerKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      setOpen(true);
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      setOpen(true);
    }
  };

  return (
    <div className="relative inline-block" ref={rootRef}>
      <div
        ref={triggerRef}
        className="inline-flex cursor-pointer"
        role="button"
        tabIndex={0}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
        onKeyDown={onTriggerKeyDown}
      >
        {trigger}
      </div>
      {open && (
        <div
          ref={menuRef}
          role="menu"
          aria-label="菜单"
          className={clsx(
            'absolute z-50 mt-1 bg-white border border-gray-200 rounded-md shadow-hover py-1 animate-fade-in',
            align === 'right' ? 'right-0' : 'left-0'
          )}
          style={{ minWidth, maxHeight, overflowY: maxHeight ? 'auto' : undefined }}
          onKeyDown={onMenuKeyDown}
        >
          {items.map((it, i) =>
            it.divider ? (
              <div key={it.key} className="my-1 border-t border-gray-200" />
            ) : (
              <button
                key={it.key}
                role="menuitem"
                tabIndex={-1}
                disabled={it.disabled}
                aria-disabled={it.disabled}
                onClick={() => {
                  it.onClick?.();
                  setOpen(false);
                }}
                className={clsx(
                  'w-full flex items-center gap-2 px-2 py-1.5 text-[length:var(--fs-sm)] text-left rounded-sm transition-colors disabled:opacity-50',
                  it.selected
                    ? 'bg-primary-50 text-primary-500'
                    : 'text-gray-600 hover:bg-gray-100',
                  !it.selected && active === i && !it.disabled && 'bg-gray-100',
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
