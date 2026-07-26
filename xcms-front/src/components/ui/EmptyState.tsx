import type { LucideIcon } from 'lucide-react';
import { Inbox } from 'lucide-react';

interface EmptyStateProps {
  icon?: LucideIcon;
  title?: string;
  description?: string;
  action?: React.ReactNode;
}

/**
 * 空状态：居中图标 + 标题 + 引导文案 + 操作按钮（见设计系统 §11.2）
 */
export function EmptyState({ icon: Icon = Inbox, title = '暂无数据', description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <Icon size={56} strokeWidth={1.5} className="text-gray-400 mb-3" />
      <p className="text-[length:var(--fs)] font-medium text-gray-700">{title}</p>
      {description && <p className="text-[length:var(--fs-sm)] text-gray-400 mt-1 max-w-xs">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
