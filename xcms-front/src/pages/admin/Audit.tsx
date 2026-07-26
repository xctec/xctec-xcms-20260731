import { useState } from 'react';
import { Download, FileText, Search, type LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockLogs = [
  { id: 1, module: '租户管理', action: 'CREATE', operator: 'admin', tenant: '集团总部', target: '科技公司', ip: '192.168.1.100', detail: '创建租户: 科技公司', time: '2025-07-25 10:30:15', status: 'SUCCESS' },
  { id: 2, module: '用户管理', action: 'UPDATE', operator: 'admin', tenant: '集团总部', target: '张三', ip: '192.168.1.100', detail: '修改用户角色', time: '2025-07-25 10:15:30', status: 'SUCCESS' },
  { id: 3, module: '权限管理', action: 'ASSIGN', operator: '张三', tenant: '集团总部', target: '部门主管', ip: '192.168.1.101', detail: '为李四分配角色', time: '2025-07-25 09:45:20', status: 'SUCCESS' },
  { id: 4, module: '组织架构', action: 'DELETE', operator: '王五', tenant: '集团总部', target: '废弃部门', ip: '192.168.1.102', detail: '删除部门: 废弃部门', time: '2025-07-25 09:30:00', status: 'SUCCESS' },
  { id: 5, module: '配置管理', action: 'UPDATE', operator: 'admin', tenant: '集团总部', target: 'sys.session.timeout', ip: '192.168.1.100', detail: '修改会话超时为7200秒', time: '2025-07-25 09:00:15', status: 'SUCCESS' },
  { id: 6, module: '租户管理', action: 'MIGRATE', operator: 'admin', tenant: '集团总部', target: '数据中台项目', ip: '192.168.1.100', detail: '租户迁移: 科技公司→财务公司', time: '2025-07-24 16:20:30', status: 'SUCCESS' },
  { id: 7, module: '用户管理', action: 'LOGIN', operator: 'lisi', tenant: '科技公司', target: '-', ip: '10.0.0.50', detail: '用户登录', time: '2025-07-24 14:00:00', status: 'SUCCESS' },
  { id: 8, module: '用户管理', action: 'LOGIN', operator: 'unknown', tenant: '-', target: '-', ip: '10.0.0.99', detail: '登录失败: 密码错误(3次)', time: '2025-07-24 13:55:00', status: 'FAILED' },
  { id: 9, module: '流程管理', action: 'APPROVE', operator: '赵六', tenant: '集团总部', target: '采购审批-001', ip: '192.168.1.103', detail: '审批通过', time: '2025-07-24 11:30:15', status: 'SUCCESS' },
  { id: 10, module: '文件管理', action: 'DOWNLOAD', operator: '钱七', tenant: '集团总部', target: '运营报告.pdf', ip: '192.168.1.104', detail: '下载文件', time: '2025-07-24 10:00:00', status: 'SUCCESS' },
];

const actionLabels: Record<string, string> = {
  CREATE: '创建', UPDATE: '修改', DELETE: '删除', ASSIGN: '分配', MIGRATE: '迁移', LOGIN: '登录', APPROVE: '审批', DOWNLOAD: '下载',
};

export default function AuditPage() {
  const [page, setPage] = useState(1);

  return (
    <div>
      <PageHeader title="审计中心" description="操作日志、安全审计、跨租户审计" actions={
        <Button variant="secondary" icon={Download}>导出日志</Button>
      } />
      <TableCard>
        <FilterBar searchPlaceholder="搜索操作人/目标..." onSearch={() => {}} filters={
          <>
            <select className="h-8 rounded-md border border-gray-300 px-3 text-xs text-gray-600 outline-none">
              <option>全部模块</option>
              <option>租户管理</option><option>用户管理</option><option>权限管理</option><option>组织架构</option>
            </select>
            <select className="h-8 rounded-md border border-gray-300 px-3 text-xs text-gray-600 outline-none">
              <option>全部操作</option><option>创建</option><option>修改</option><option>删除</option><option>登录</option>
            </select>
            {['全部', '成功', '失败'].map((f, i) => (
              <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
            ))}
          </>
        } />
        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead><tr className="border-b border-gray-200 bg-gray-50">
              {['模块', '操作', '操作人', '租户', '操作对象', 'IP地址', '详情', '时间', '状态'].map((h, i) => (
                <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[8].includes(i) ? 'text-center' : 'text-left'}`}>{h}</th>
              ))}
            </tr></thead>
            <tbody>
              {mockLogs.map(l => (
                <tr key={l.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                  <td className="px-4 py-2.5 text-gray-700">{l.module}</td>
                  <td className="px-4"><span className="rounded bg-primary-50 px-1.5 py-0.5 text-[11px] text-primary-600">{actionLabels[l.action]}</span></td>
                  <td className="px-4 font-medium text-gray-900">{l.operator}</td>
                  <td className="px-4 text-gray-500">{l.tenant}</td>
                  <td className="px-4 text-gray-500">{l.target}</td>
                  <td className="px-4 text-[11px] font-mono text-gray-400">{l.ip}</td>
                  <td className="px-4 text-gray-500 text-[11px] max-w-[200px] truncate" title={l.detail}>{l.detail}</td>
                  <td className="px-4 text-[11px] text-gray-400">{l.time}</td>
                  <td className="px-4 text-center"><StatusBadge status={l.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} total={10} size={10} onChange={setPage} />
      </TableCard>
    </div>
  );
}
