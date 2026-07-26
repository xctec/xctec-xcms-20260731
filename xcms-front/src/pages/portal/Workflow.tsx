import { useState } from 'react';
import { Plus, Clock, CheckCircle, FileText, Send, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockProcessCategories = [
  { id: 1, name: '行政审批', icon: '📋', count: 8 },
  { id: 2, name: '财务审批', icon: '💰', count: 5 },
  { id: 3, name: '人事审批', icon: '👤', count: 6 },
  { id: 4, name: '采购审批', icon: '🛒', count: 4 },
  { id: 5, name: '合同审批', icon: '📄', count: 3 },
];

const mockStartableProcesses = [
  { id: 1, name: '请假申请', category: '行政审批', desc: '提交请假申请，等待主管审批', icon: '📅' },
  { id: 2, name: '报销申请', category: '财务审批', desc: '提交费用报销单，财务审核', icon: '💵' },
  { id: 3, name: '采购申请', category: '采购审批', desc: '提交采购需求，多级审批', icon: '🛍️' },
  { id: 4, name: '出差申请', category: '行政审批', desc: '出差审批流程', icon: '✈️' },
  { id: 5, name: '入职申请', category: '人事审批', desc: '新员工入职流程', icon: '🎉' },
  { id: 6, name: '合同审批', category: '合同审批', desc: '合同审批流程', icon: '📝' },
];

const mockTodoTasks = [
  { id: 'T001', title: '采购审批-办公用品采购', process: '采购审批流程', node: '部门主管审批', initiator: '钱七', createTime: '2025-07-25 10:30', priority: 'NORMAL', crossTenant: false, deadline: '2025-07-26 10:30' },
  { id: 'T002', title: '请假申请-李四请假3天', process: '请假申请流程', node: 'HR审批', initiator: '李四', createTime: '2025-07-25 09:15', priority: 'NORMAL', crossTenant: false, deadline: '2025-07-26 09:15' },
  { id: 'T003', title: '报销审批-差旅费报销', process: '报销审批流程', node: '财务审核', initiator: '王五', createTime: '2025-07-24 16:00', priority: 'HIGH', crossTenant: false, deadline: '2025-07-25 16:00' },
  { id: 'T004', title: '合同审批-供应商合同', process: '合同审批流程', node: '法务审核', initiator: '赵六', createTime: '2025-07-24 14:20', priority: 'HIGH', crossTenant: true, deadline: '2025-07-25 14:20' },
  { id: 'T005', title: '入职审批-新员工入职', process: '员工入职流程', node: 'IT配置', initiator: 'HR', createTime: '2025-07-23 11:00', priority: 'NORMAL', crossTenant: false, deadline: '2025-07-26 11:00' },
];

const mockDoneTasks = [
  { id: 'D001', title: '请假申请-张三请假1天', process: '请假申请流程', node: '部门主管审批', initiator: '张三', completeTime: '2025-07-24 15:30', result: 'APPROVED' },
  { id: 'D002', title: '报销审批-办公用品报销', process: '报销审批流程', node: '部门主管审批', initiator: '孙八', completeTime: '2025-07-23 10:00', result: 'APPROVED' },
  { id: 'D003', title: '采购审批-设备采购', process: '采购审批流程', node: '部门主管审批', initiator: '周九', completeTime: '2025-07-22 14:00', result: 'REJECTED' },
];

const mockMyApply = [
  { id: 'P001', title: '我的请假申请', process: '请假申请流程', currentNode: 'HR审批', status: 'PENDING', createTime: '2025-07-25 08:00', duration: '2小时' },
  { id: 'P002', title: '设备采购申请', process: '采购审批流程', currentNode: '已完成', status: 'APPROVED', createTime: '2025-07-20 10:00', duration: '3天' },
  { id: 'P003', title: '差旅报销', process: '报销审批流程', currentNode: '财务审核', status: 'PENDING', createTime: '2025-07-24 16:00', duration: '1天' },
];

const priorityLabels: Record<string, { label: string; color: string }> = {
  HIGH: { label: '紧急', color: 'bg-danger-50 text-danger-500' },
  NORMAL: { label: '普通', color: 'bg-gray-100 text-gray-500' },
  LOW: { label: '低', color: 'bg-info-50 text-info-500' },
};

export default function PortalWorkflowPage() {
  const [tab, setTab] = useState<'start' | 'todo' | 'done' | 'apply'>('todo');
  const [page, setPage] = useState(1);

  const tabBtn = (key: typeof tab, label: string, Icon: typeof Clock, badge?: number) => (
    <button onClick={() => setTab(key)} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === key ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
      {Icon && <Icon size={15} />} {label}
      {badge && <span className="ml-1 rounded-full bg-danger-500 px-1.5 text-[10px] text-white">{badge}</span>}
    </button>
  );

  return (
    <div>
      <PageHeader title="流程中心" description="发起流程、待办任务、已办任务、我的申请" />
      <TableCard>
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          {tabBtn('start', '发起流程', Plus)}
          {tabBtn('todo', '待办任务', Clock, 5)}
          {tabBtn('done', '已办任务', CheckCircle)}
          {tabBtn('apply', '我的申请', FileText)}
        </div>

        {tab === 'start' && (
          <div>
            {/* Categories */}
            <div className="flex gap-2 border-b border-gray-100 px-4 py-3">
              {mockProcessCategories.map(c => (
                <button key={c.id} className="flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600 hover:bg-gray-200">
                  <span>{c.icon}</span>{c.name}<span className="text-gray-400">({c.count})</span>
                </button>
              ))}
            </div>
            {/* Process Cards */}
            <div className="grid grid-cols-3 gap-3 p-4">
              {mockStartableProcesses.map(p => (
                <div key={p.id} className="rounded-lg border border-gray-200 p-4 transition-all hover:border-primary-300 hover:shadow-card cursor-pointer">
                  <div className="mb-2 flex items-center gap-2">
                    <span className="text-2xl">{p.icon}</span>
                    <div>
                      <h3 className="text-sm font-semibold text-gray-900">{p.name}</h3>
                      <span className="text-[11px] text-gray-400">{p.category}</span>
                    </div>
                  </div>
                  <p className="mb-3 text-xs text-gray-500">{p.desc}</p>
                  <Button variant="ghost" size="sm" icon={Send}>发起</Button>
                </div>
              ))}
            </div>
          </div>
        )}

        {tab === 'todo' && (
          <>
            <FilterBar searchPlaceholder="搜索任务标题..." onSearch={() => {}} filters={<>
              {['全部', '紧急', '跨租户'].map((f, i) => (
                <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
              ))}
            </>} />
            <div className="overflow-x-auto">
              <table className="w-full text-[13px]">
                <thead><tr className="border-b border-gray-200 bg-gray-50">
                  {['任务标题', '流程', '当前节点', '发起人', '优先级', '创建时间', '截止时间', '操作'].map((h, i) => (
                    <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[4].includes(i) ? 'text-center' : i === 7 ? 'text-right' : 'text-left'}`}>{h}</th>
                  ))}
                </tr></thead>
                <tbody>
                  {mockTodoTasks.map(t => (
                    <tr key={t.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                      <td className="px-4 py-2.5">
                        <div className="flex items-center gap-2">
                          {t.crossTenant && <span className="rounded bg-info-50 px-1 py-0.5 text-[10px] text-info-500">跨租户</span>}
                          <span className="font-medium text-gray-900">{t.title}</span>
                        </div>
                      </td>
                      <td className="px-4 text-gray-500">{t.process}</td>
                      <td className="px-4 text-gray-500">{t.node}</td>
                      <td className="px-4 text-gray-500">{t.initiator}</td>
                      <td className="px-4 text-center"><span className={`rounded px-1.5 py-0.5 text-[11px] ${priorityLabels[t.priority]?.color}`}>{priorityLabels[t.priority]?.label}</span></td>
                      <td className="px-4 text-[11px] text-gray-400">{t.createTime}</td>
                      <td className="px-4 text-[11px] text-warning-500">{t.deadline}</td>
                      <td className="px-4 text-right"><button className="text-xs font-medium text-primary-500">办理</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination page={page} total={5} size={10} onChange={setPage} />
          </>
        )}

        {tab === 'done' && (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['任务标题', '流程', '处理节点', '发起人', '完成时间', '结果'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[5].includes(i) ? 'text-center' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {mockDoneTasks.map(t => (
                  <tr key={t.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5 font-medium text-gray-900">{t.title}</td>
                    <td className="px-4 text-gray-500">{t.process}</td>
                    <td className="px-4 text-gray-500">{t.node}</td>
                    <td className="px-4 text-gray-500">{t.initiator}</td>
                    <td className="px-4 text-[11px] text-gray-400">{t.completeTime}</td>
                    <td className="px-4 text-center"><StatusBadge status={t.result} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === 'apply' && (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['申请标题', '流程', '当前节点', '状态', '发起时间', '耗时', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[3].includes(i) ? 'text-center' : i === 6 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {mockMyApply.map(p => (
                  <tr key={p.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5 font-medium text-gray-900">{p.title}</td>
                    <td className="px-4 text-gray-500">{p.process}</td>
                    <td className="px-4 text-gray-500">{p.currentNode}</td>
                    <td className="px-4 text-center"><StatusBadge status={p.status} /></td>
                    <td className="px-4 text-[11px] text-gray-400">{p.createTime}</td>
                    <td className="px-4 text-gray-500">{p.duration}</td>
                    <td className="px-4 text-right">
                      <button className="text-xs font-medium text-primary-500">详情</button>
                      {p.status === 'PENDING' && <button className="ml-3 text-xs text-gray-400">催办</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </TableCard>
    </div>
  );
}
