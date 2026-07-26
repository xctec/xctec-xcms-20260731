import { useState } from 'react';
import { Plus, Play, Pause, Clock, MoreHorizontal } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockTasks = [
  { id: 1, name: '数据备份任务', cron: '0 0 * * *', className: 'com.xcms.task.DataBackupTask', lastRun: '2025-07-25 00:00', nextRun: '2025-07-26 00:00', duration: '2m30s', status: 'ACTIVE', lastStatus: 'SUCCESS' },
  { id: 2, name: '会话清理任务', cron: '0 */6 * * *', className: 'com.xcms.task.SessionCleanupTask', lastRun: '2025-07-25 06:00', nextRun: '2025-07-25 12:00', duration: '15s', status: 'ACTIVE', lastStatus: 'SUCCESS' },
  { id: 3, name: '配额统计任务', cron: '0 1 * * 1', className: 'com.xcms.task.QuotaStatsTask', lastRun: '2025-07-22 01:00', nextRun: '2025-07-29 01:00', duration: '1m20s', status: 'ACTIVE', lastStatus: 'SUCCESS' },
  { id: 4, name: '流程超时检测', cron: '*/30 * * * *', className: 'com.xcms.task.ProcessTimeoutTask', lastRun: '2025-07-25 10:30', nextRun: '2025-07-25 11:00', duration: '3s', status: 'ACTIVE', lastStatus: 'SUCCESS' },
  { id: 5, name: '审计日志归档', cron: '0 2 1 * *', className: 'com.xcms.task.AuditArchiveTask', lastRun: '2025-07-01 02:00', nextRun: '2025-08-01 02:00', duration: '5m10s', status: 'ACTIVE', lastStatus: 'SUCCESS' },
  { id: 6, name: '消息推送重试', cron: '*/5 * * * *', className: 'com.xcms.task.MessageRetryTask', lastRun: '2025-07-25 10:55', nextRun: '2025-07-25 11:00', duration: '8s', status: 'ACTIVE', lastStatus: 'FAILED' },
  { id: 7, name: '租户配额检查', cron: '0 0 * * *', className: 'com.xcms.task.QuotaCheckTask', lastRun: '2025-07-25 00:00', nextRun: '2025-07-26 00:00', duration: '45s', status: 'SUSPENDED', lastStatus: 'SUCCESS' },
];

export default function TaskSchedulePage() {
  const [page, setPage] = useState(1);

  return (
    <div>
      <PageHeader title="任务调度" description="定时任务管理、执行历史、手动触发" actions={<Button variant="primary" icon={Plus}>新建任务</Button>} />
      <TableCard>
        <FilterBar searchPlaceholder="搜索任务名称..." onSearch={() => {}} filters={
          <>{['全部', '运行中', '已暂停'].map((f, i) => (
            <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
          ))}</>
        } />
        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead><tr className="border-b border-gray-200 bg-gray-50">
              {['任务名称', 'Cron表达式', '执行类', '上次执行', '下次执行', '耗时', '上次结果', '状态', '操作'].map((h, i) => (
                <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[6, 7].includes(i) ? 'text-center' : i === 8 ? 'text-right' : 'text-left'}`}>{h}</th>
              ))}
            </tr></thead>
            <tbody>
              {mockTasks.map(t => (
                <tr key={t.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                  <td className="px-4 py-2.5 font-medium text-gray-900">{t.name}</td>
                  <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{t.cron}</code></td>
                  <td className="px-4 text-gray-500 text-[11px]">{t.className}</td>
                  <td className="px-4 text-[11px] text-gray-400">{t.lastRun}</td>
                  <td className="px-4 text-[11px] text-gray-400">{t.nextRun}</td>
                  <td className="px-4 text-gray-500"><span className="flex items-center gap-1"><Clock size={11} />{t.duration}</span></td>
                  <td className="px-4 text-center"><StatusBadge status={t.lastStatus} /></td>
                  <td className="px-4 text-center"><StatusBadge status={t.status} /></td>
                  <td className="px-4 text-right">
                    {t.status === 'ACTIVE' ? (
                      <button className="text-gray-400 hover:text-warning-500"><Pause size={14} /></button>
                    ) : (
                      <button className="text-gray-400 hover:text-success-500"><Play size={14} /></button>
                    )}
                    <button className="ml-3 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} total={7} size={10} onChange={setPage} />
      </TableCard>
    </div>
  );
}
