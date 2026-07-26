import clsx from 'clsx';
import type { LucideIcon } from 'lucide-react';
import { TrendingDown, TrendingUp } from 'lucide-react';

type StatColor = 'primary' | 'success' | 'warning' | 'danger' | 'info';

const colorMap: Record<StatColor, string> = {
  primary: 'var(--c-primary)',
  success: 'var(--c-success)',
  warning: 'var(--c-warning)',
  danger: 'var(--c-danger)',
  info: 'var(--c-info)',
};

interface SparklineProps {
  data: number[];
  color?: string;
  width?: number;
  height?: number;
}

/** 轻量 SVG 趋势图（无 echarts 依赖），带渐变填充 */
export function Sparkline({ data, color = 'var(--c-primary)', width = 88, height = 30 }: SparklineProps) {
  if (!data || data.length < 2) return null;
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = max - min || 1;
  const pts = data
    .map((v, i) => `${(i / (data.length - 1)) * width},${height - ((v - min) / range) * height}`)
    .join(' ');
  const areaPts = `0,${height} ${pts} ${width},${height}`;
  const gid = `spark-${Math.random().toString(36).slice(2, 8)}`;
  return (
    <svg width={width} height={height} className="overflow-visible">
      <defs>
        <linearGradient id={gid} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.25" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <polygon points={areaPts} fill={`url(#${gid})`} />
      <polyline
        points={pts}
        fill="none"
        stroke={color}
        strokeWidth="1.5"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </svg>
  );
}

interface StatCardProps {
  title: string;
  value: React.ReactNode;
  icon?: LucideIcon;
  color?: StatColor;
  trend?: number;
  /** 预警类指标反向（下降为好） */
  invertTrend?: boolean;
  sparkData?: number[];
}

/**
 * 统计卡片（见设计系统 §9.3）：左侧色条 + 图标 + 数值 + 趋势 + Sparkline
 */
export function StatCard({ title, value, icon: Icon, color = 'primary', trend, invertTrend, sparkData }: StatCardProps) {
  const c = colorMap[color];
  const up = (trend ?? 0) >= 0;
  const good = invertTrend ? !up : up;
  return (
    <div className="relative bg-white rounded-[var(--card-radius)] border border-gray-200 shadow-card hover:shadow-hover transition-shadow p-[var(--card-p)] overflow-hidden">
      <div className="absolute left-0 top-0 bottom-0 w-[3px]" style={{ backgroundColor: c }} />
      <div className="flex items-start justify-between">
        <div>
          <p className="text-[length:var(--fs-sm)] text-gray-500">{title}</p>
          <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
        </div>
        {Icon && (
          <div className="rounded-md p-2" style={{ backgroundColor: `color-mix(in srgb, ${c} 15%, transparent)` }}>
            <Icon size={18} style={{ color: c }} />
          </div>
        )}
      </div>
      {(trend !== undefined || sparkData) && (
        <div className="flex items-center justify-between mt-2">
          {trend !== undefined ? (
            <span
              className={clsx(
                'inline-flex items-center gap-0.5 text-[length:var(--fs-xs)] font-medium',
                good ? 'text-success-500' : 'text-danger-500'
              )}
            >
              {up ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
              {Math.abs(trend)}%
            </span>
          ) : (
            <span />
          )}
          {sparkData && <Sparkline data={sparkData} color={c} />}
        </div>
      )}
    </div>
  );
}
