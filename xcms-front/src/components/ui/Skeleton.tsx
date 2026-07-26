import clsx from 'clsx';

interface SkeletonProps {
  className?: string;
  style?: React.CSSProperties;
}

/** 基础骨架块 */
export function Skeleton({ className, style }: SkeletonProps) {
  return <div className={clsx('animate-pulse rounded bg-gray-200', className)} style={style} />;
}

interface TableSkeletonProps {
  rows?: number;
  cols?: number;
}

/** 表格加载骨架（保留表头视觉，行高消费 --row-h） */
export function TableSkeleton({ rows = 5, cols = 5 }: TableSkeletonProps) {
  return (
    <div className="p-[var(--card-p)]">
      {Array.from({ length: rows }).map((_, r) => (
        <div
          key={r}
          className="flex items-center border-b border-gray-200"
          style={{ height: 'var(--row-h)', gap: 'var(--gap-sm)' }}
        >
          {Array.from({ length: cols }).map((__, c) => (
            <Skeleton key={c} className="h-4 flex-1" />
          ))}
        </div>
      ))}
    </div>
  );
}

interface PageSkeletonProps {
  lines?: number;
}

/** 页面级骨架屏 */
export function PageSkeleton({ lines = 6 }: PageSkeletonProps) {
  return (
    <div className="p-[var(--card-p)] space-y-[var(--gap-sm)]">
      <Skeleton className="h-6 w-48" />
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton key={i} className="h-4" style={{ width: `${100 - (i % 3) * 12}%` }} />
      ))}
    </div>
  );
}
