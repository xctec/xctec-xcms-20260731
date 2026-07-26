import { AlertTriangle, RefreshCw } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  title?: string;
  description?: string;
  onRetry?: () => void;
}

/**
 * 错误态：图标 + 文案 + 重试按钮（见设计系统 §11.3）
 */
export function ErrorState({ title = '网络异常', description = '请稍后重试', onRetry }: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <AlertTriangle size={56} strokeWidth={1.5} className="text-danger-500 mb-3" />
      <p className="text-[length:var(--fs)] font-medium text-gray-700">{title}</p>
      <p className="text-[length:var(--fs-sm)] text-gray-400 mt-1">{description}</p>
      {onRetry && (
        <div className="mt-4">
          <Button variant="secondary" icon={RefreshCw} onClick={onRetry}>
            重试
          </Button>
        </div>
      )}
    </div>
  );
}
