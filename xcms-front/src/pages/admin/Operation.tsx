import { useState } from 'react';
import { Building2, Users, TrendingUp, AlertTriangle, Server, Database, Activity, Cpu } from 'lucide-react';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

function MiniChart({ data, color }: { data: number[]; color: string }) {
  const max = Math.max(...data), min = Math.min(...data), range = max - min || 1;
  const pts = data.map((v, i) => `${(i / (data.length - 1)) * 100},${24 - ((v - min) / range) * 20 - 2}`).join(' ');
  return <svg width="100%" height="24" viewBox="0 0 100 24" preserveAspectRatio="none"><polyline points={pts} fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" /></svg>;
}

export default function OperationPage() {
  const stats = [
    { label: '租户总数', value: '9', change: '+2', trend: 'up', spark: [3, 4, 5, 6, 7, 8, 9], color: '#2563EB', icon: Building2 },
    { label: '活跃用户', value: '2,011', change: '+12%', trend: 'up', spark: [1200, 1400, 1600, 1800, 1900, 2000, 2011], color: '#16A34A', icon: Users },
    { label: 'API调用量(日)', value: '48.2K', change: '+8%', trend: 'up', spark: [30, 35, 40, 38, 42, 45, 48], color: '#0891B2', icon: Activity },
    { label: '告警数', value: '2', change: '-3', trend: 'down', spark: [8, 6, 5, 5, 4, 3, 2], color: '#DC2626', icon: AlertTriangle },
  ];

  const systemMetrics = [
    { label: 'CPU 使用率', value: '34%', spark: [20, 25, 30, 28, 35, 32, 34], color: '#2563EB', icon: Cpu },
    { label: '内存使用率', value: '62%', spark: [50, 55, 58, 60, 62, 61, 62], color: '#7C3AED', icon: Server },
    { label: '数据库连接', value: '45/100', spark: [30, 35, 40, 42, 45, 44, 45], color: '#059669', icon: Database },
    { label: '磁盘使用率', value: '28%', spark: [20, 22, 24, 25, 26, 27, 28], color: '#D97706', icon: Server },
  ];

  const quotaReport = [
    { tenant: '集团总部', userQuota: '1280/5000', storageQuota: '128GB/500GB', apiQuota: '8.2K/50K', status: 'ACTIVE' },
    { tenant: '科技公司', userQuota: '320/1000', storageQuota: '32GB/100GB', apiQuota: '3.5K/10K', status: 'ACTIVE' },
    { tenant: '财务公司', userQuota: '156/800', storageQuota: '15GB/80GB', apiQuota: '1.2K/8K', status: 'ACTIVE' },
    { tenant: '物流公司', userQuota: '89/500', storageQuota: '9GB/50GB', apiQuota: '0.8K/5K', status: 'SUSPENDED' },
    { tenant: '人力资源', userQuota: '420/500', storageQuota: '42GB/50GB', apiQuota: '4.5K/5K', status: 'ACTIVE' },
  ];

  return (
    <div>
      <PageHeader title="运营看板" description="系统概览、资源监控、配额报告" />

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {stats.map((s, i) => (
          <div key={i} className="rounded-lg border border-gray-200 bg-white p-4 shadow-card relative overflow-hidden">
            <div className="absolute left-0 top-0 bottom-0 w-1" style={{ backgroundColor: s.color }} />
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg" style={{ backgroundColor: s.color + '15' }}>
                  <s.icon size={16} style={{ color: s.color }} />
                </div>
                <span className="text-xs text-gray-500">{s.label}</span>
              </div>
              <span className={`text-[11px] font-semibold flex items-center gap-0.5 ${s.trend === 'up' ? 'text-success-500' : 'text-success-500'}`}>
                <TrendingUp size={11} />{s.change}
              </span>
            </div>
            <div className="text-2xl font-bold text-gray-900">{s.value}</div>
            <div className="mt-2"><MiniChart data={s.spark} color={s.color} /></div>
          </div>
        ))}
      </div>

      {/* System Metrics */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {systemMetrics.map((m, i) => (
          <div key={i} className="rounded-lg border border-gray-200 bg-white p-4 shadow-card">
            <div className="flex items-center gap-2 mb-2">
              <m.icon size={16} className="text-gray-400" />
              <span className="text-xs text-gray-500">{m.label}</span>
            </div>
            <div className="text-xl font-bold text-gray-900 mb-2">{m.value}</div>
            <MiniChart data={m.spark} color={m.color} />
          </div>
        ))}
      </div>

      {/* Quota Report */}
      <TableCard>
        <div className="border-b border-gray-100 px-4 py-3"><h3 className="text-sm font-semibold text-gray-800">租户配额报告</h3></div>
        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead><tr className="border-b border-gray-200 bg-gray-50">
              {['租户', '用户配额', '存储配额', 'API配额', '状态'].map((h, i) => (
                <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${i === 4 ? 'text-center' : 'text-left'}`}>{h}</th>
              ))}
            </tr></thead>
            <tbody>
              {quotaReport.map((q, i) => (
                <tr key={i} className="border-b border-gray-100 hover:bg-primary-50/50">
                  <td className="px-4 py-2.5 font-medium text-gray-900">{q.tenant}</td>
                  <td className="px-4 text-gray-500">{q.userQuota}</td>
                  <td className="px-4 text-gray-500">{q.storageQuota}</td>
                  <td className="px-4 text-gray-500">{q.apiQuota}</td>
                  <td className="px-4 text-center"><StatusBadge status={q.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </TableCard>
    </div>
  );
}
