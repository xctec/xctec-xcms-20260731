import clsx from 'clsx';

type Status = 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'MIGRATING' | 'ARCHIVED' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'ENABLED' | 'DISABLED' | 'SUCCESS' | 'FAILED' | 'RUNNING' | 'EXPIRED';

const config: Record<string, { label: string; bg: string; text: string; dot: string }> = {
  ACTIVE: { label: '正常', bg: 'bg-success-50', text: 'text-success-500', dot: 'bg-success-500' },
  ENABLED: { label: '已启用', bg: 'bg-success-50', text: 'text-success-500', dot: 'bg-success-500' },
  APPROVED: { label: '已批准', bg: 'bg-success-50', text: 'text-success-500', dot: 'bg-success-500' },
  SUCCESS: { label: '成功', bg: 'bg-success-50', text: 'text-success-500', dot: 'bg-success-500' },
  RUNNING: { label: '运行中', bg: 'bg-info-50', text: 'text-info-500', dot: 'bg-info-500' },
  PENDING: { label: '待处理', bg: 'bg-warning-50', text: 'text-warning-500', dot: 'bg-warning-500' },
  SUSPENDED: { label: '已暂停', bg: 'bg-warning-50', text: 'text-warning-500', dot: 'bg-warning-500' },
  MIGRATING: { label: '迁移中', bg: 'bg-info-50', text: 'text-info-500', dot: 'bg-info-500' },
  LOCKED: { label: '已锁定', bg: 'bg-danger-50', text: 'text-danger-500', dot: 'bg-danger-500' },
  DISABLED: { label: '已禁用', bg: 'bg-danger-50', text: 'text-danger-500', dot: 'bg-danger-500' },
  REJECTED: { label: '已拒绝', bg: 'bg-danger-50', text: 'text-danger-500', dot: 'bg-danger-500' },
  FAILED: { label: '失败', bg: 'bg-danger-50', text: 'text-danger-500', dot: 'bg-danger-500' },
  ARCHIVED: { label: '已归档', bg: 'bg-gray-100', text: 'text-gray-500', dot: 'bg-gray-400' },
  EXPIRED: { label: '已过期', bg: 'bg-gray-100', text: 'text-gray-500', dot: 'bg-gray-400' },
};

export function StatusBadge({ status, label }: { status: string; label?: string }) {
  const c = config[status] || { label: status, bg: 'bg-gray-100', text: 'text-gray-500', dot: 'bg-gray-400' };
  return (
    <span className={clsx('inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-[11px] font-medium', c.bg, c.text)}>
      <span className={clsx('h-1.5 w-1.5 rounded-full', c.dot)} />
      {label || c.label}
    </span>
  );
}
