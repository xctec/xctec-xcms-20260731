import { X } from 'lucide-react';

interface BatchActionBarProps {
  selectedCount: number;
  onClear: () => void;
  children?: React.ReactNode;
}

/**
 * 批量操作栏（见设计系统 §9.11）：选中行后出现，左侧已选数量 + 清除，右侧批量操作
 */
export function BatchActionBar({ selectedCount, onClear, children }: BatchActionBarProps) {
  if (selectedCount <= 0) return null;
  return (
    <div className="flex items-center justify-between bg-primary-50 border border-primary-100 rounded-md px-[var(--card-p)] py-[var(--gap-sm)] animate-fade-in">
      <div className="flex items-center gap-2 text-[length:var(--fs-sm)] text-primary-700">
        <span>
          已选 <b className="text-primary-500">{selectedCount}</b> 项
        </span>
        <button
          onClick={onClear}
          className="inline-flex items-center gap-0.5 text-primary-500 hover:underline"
        >
          <X size={12} />
          清除
        </button>
      </div>
      <div className="flex items-center gap-[var(--gap-sm)]">{children}</div>
    </div>
  );
}
