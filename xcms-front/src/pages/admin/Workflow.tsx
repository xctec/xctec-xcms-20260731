import { useState } from 'react';
import { Plus, Workflow, FileText, CheckSquare } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockProcessDefs = [
  { id: 1, key: 'PURCHASE_APPROVAL', name: '采购审批流程', version: 3, deployTime: '2025-07-01', status: 'ACTIVE', instanceCount: 45 },
  { id: 2, key: 'LEAVE_REQUEST', name: '请假申请流程', version: 5, deployTime: '2025-06-15', status: 'ACTIVE', instanceCount: 128 },
  { id: 3, key: 'REIMBURSEMENT', name: '报销审批流程', version: 2, deployTime: '2025-06-20', status: 'ACTIVE', instanceCount: 67 },
  { id: 4, key: 'EMPLOYEE_ONBOARDING', name: '员工入职流程', version: 1, deployTime: '2025-07-10', status: 'ACTIVE', instanceCount: 12 },
  { id: 5, key: 'CONTRACT_REVIEW', name: '合同审批流程', version: 4, deployTime: '2025-05-28', status: 'SUSPENDED', instanceCount: 89 },
  { id: 6, key: 'PROJECT_KICKOFF', name: '项目启动流程', version: 2, deployTime: '2025-07-15', status: 'ACTIVE', instanceCount: 8 },
];

const mockTasks = [
  { id: 'T001', processName: '采购审批流程', taskName: '部门主管审批', assignee: '张三', createTime: '2025-07-25 10:30', status: 'PENDING' },
  { id: 'T002', processName: '请假申请流程', taskName: 'HR审批', assignee: '李四', createTime: '2025-07-25 09:15', status: 'PENDING' },
  { id: 'T003', processName: '报销审批流程', taskName: '财务审核', assignee: '赵六', createTime: '2025-07-24 16:00', status: 'PENDING' },
  { id: 'T004', processName: '合同审批流程', taskName: '法务审核', assignee: '王五', createTime: '2025-07-24 14:20', status: 'PENDING' },
  { id: 'T005', processName: '员工入职流程', taskName: 'IT配置', assignee: '钱七', createTime: '2025-07-23 11:00', status: 'PENDING' },
];

export default function WorkflowPage() {
  const [tab, setTab] = useState<'process' | 'task'>('process');
  const [page, setPage] = useState(1);

  return (
    <div>
      <PageHeader title="流程管理" description="管理流程定义、审批任务、流程实例" actions={
        <>
          <Button variant="secondary" icon={FileText}>流程分类</Button>
          <Button variant="primary" icon={Plus}>部署流程</Button>
        </>
      } />
      <TableCard>
        {/* Tabs */}
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          <button onClick={() => setTab('process')} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === 'process' ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
            <Workflow size={15} /> 流程定义
          </button>
          <button onClick={() => setTab('task')} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === 'task' ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
            <CheckSquare size={15} /> 待办任务 <span className="ml-1 rounded-full bg-danger-500 px-1.5 text-[10px] text-white">5</span>
          </button>
        </div>

        {tab === 'process' ? (
          <>
            <FilterBar searchPlaceholder="搜索流程名称..." onSearch={() => {}} filters={
              <>{['全部', '已部署', '已暂停'].map((f, i) => (
                <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
              ))}</>
            } />
            <div className="overflow-x-auto">
              <table className="w-full text-[13px]">
                <thead><tr className="border-b border-gray-200 bg-gray-50">
                  {['流程名称', '流程Key', '版本', '部署时间', '实例数', '状态', '操作'].map((h, i) => (
                    <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[4, 5].includes(i) ? 'text-center' : i === 6 ? 'text-right' : 'text-left'}`}>{h}</th>
                  ))}
                </tr></thead>
                <tbody>
                  {mockProcessDefs.map(p => (
                    <tr key={p.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                      <td className="px-4 py-2.5 font-medium text-gray-900">{p.name}</td>
                      <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{p.key}</code></td>
                      <td className="px-4 text-gray-500">v{p.version}</td>
                      <td className="px-4 text-[11px] text-gray-400">{p.deployTime}</td>
                      <td className="px-4 text-center text-gray-500">{p.instanceCount}</td>
                      <td className="px-4 text-center"><StatusBadge status={p.status} /></td>
                      <td className="px-4 text-right">
                        <button className="text-xs font-medium text-primary-500">发起</button>
                        <button className="ml-3 text-xs text-gray-400">流程图</button>
                        <button className="ml-2 text-xs text-gray-400">停用</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['任务ID', '流程名称', '任务节点', '办理人', '创建时间', '状态', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[5].includes(i) ? 'text-center' : i === 6 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {mockTasks.map(t => (
                  <tr key={t.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{t.id}</code></td>
                    <td className="px-4 font-medium text-gray-900">{t.processName}</td>
                    <td className="px-4 text-gray-500">{t.taskName}</td>
                    <td className="px-4 text-gray-500">{t.assignee}</td>
                    <td className="px-4 text-[11px] text-gray-400">{t.createTime}</td>
                    <td className="px-4 text-center"><StatusBadge status={t.status} /></td>
                    <td className="px-4 text-right">
                      <button className="text-xs font-medium text-primary-500">办理</button>
                      <button className="ml-3 text-xs text-gray-400">详情</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination page={page} total={tab === 'process' ? 6 : 5} size={10} onChange={setPage} />
      </TableCard>
    </div>
  );
}
