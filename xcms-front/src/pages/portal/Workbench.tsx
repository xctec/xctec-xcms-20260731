import { ClipboardList, Bell, FileCheck, Clock, ChevronRight, Plus, FileText, TrendingUp, Users } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

const stats = [
  { label: '待办任务', value: '5', icon: ClipboardList, color: 'text-primary-500', bg: 'bg-primary-50' },
  { label: '未读消息', value: '3', icon: Bell, color: 'text-success-500', bg: 'bg-success-50' },
  { label: '已办任务', value: '28', icon: FileCheck, color: 'text-info-500', bg: 'bg-info-50' },
  { label: '逾期任务', value: '1', icon: Clock, color: 'text-danger-500', bg: 'bg-danger-50' },
];

const mockTodos = [
  { id: 1, title: '采购审批-办公用品采购', process: '采购审批流程', node: '部门主管审批', time: '1小时前', priority: 'normal' },
  { id: 2, title: '请假申请-李四请假3天', process: '请假申请流程', node: 'HR审批', time: '3小时前', priority: 'normal' },
  { id: 3, title: '报销审批-差旅费报销', process: '报销审批流程', node: '财务审核', time: '5小时前', priority: 'high' },
  { id: 4, title: '合同审批-供应商合同', process: '合同审批流程', node: '法务审核', time: '昨天', priority: 'high' },
  { id: 5, title: '入职审批-新员工入职', process: '员工入职流程', node: 'IT配置', time: '2天前', priority: 'normal' },
];

const mockNotices = [
  { id: 1, title: '系统将于今晚22:00进行维护升级', time: '今天 09:00', read: false },
  { id: 2, title: '新版本 v2.1 已发布，新增多项功能', time: '昨天 10:00', read: false },
  { id: 3, title: '请及时修改默认密码，确保账号安全', time: '3天前', read: true },
  { id: 4, title: '7月运营月报已生成，请查看详情', time: '5天前', read: true },
];

const mockQuickActions = [
  { icon: FileText, label: '发起请假', color: 'text-primary-500', bg: 'bg-primary-50' },
  { icon: FileText, label: '发起报销', color: 'text-success-500', bg: 'bg-success-50' },
  { icon: FileText, label: '发起采购', color: 'text-warning-500', bg: 'bg-warning-50' },
  { icon: FileText, label: '发起出差', color: 'text-info-500', bg: 'bg-info-50' },
  { icon: Users, label: '查看团队', color: 'text-primary-500', bg: 'bg-primary-50' },
  { icon: TrendingUp, label: '数据概览', color: 'text-success-500', bg: 'bg-success-50' },
];

export default function WorkbenchPage() {
  return (
    <div>
      <PageHeader title="工作台" description="待办事项、通知公告、快捷入口" />

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {stats.map((s, i) => (
          <div key={i} className="rounded-lg border border-gray-200 bg-white p-4 shadow-card">
            <div className="flex items-center justify-between">
              <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${s.bg}`}>
                <s.icon size={20} className={s.color} />
              </div>
              <span className="text-2xl font-bold text-gray-900">{s.value}</span>
            </div>
            <p className="mt-2 text-sm text-gray-500">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="mb-6 rounded-lg border border-gray-200 bg-white p-4 shadow-card">
        <h3 className="mb-3 text-sm font-semibold text-gray-800">快捷入口</h3>
        <div className="grid grid-cols-6 gap-3">
          {mockQuickActions.map((a, i) => (
            <button key={i} className="flex flex-col items-center gap-2 rounded-lg p-3 hover:bg-gray-50">
              <div className={`flex h-10 w-10 items-center justify-center rounded-lg ${a.bg}`}>
                <a.icon size={18} className={a.color} />
              </div>
              <span className="text-xs text-gray-600">{a.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Todo + Notices */}
      <div className="grid grid-cols-2 gap-4">
        {/* Todo List */}
        <TableCard>
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h3 className="text-sm font-semibold text-gray-800">待办事项</h3>
            <Button variant="ghost" size="sm">查看全部 <ChevronRight size={13} /></Button>
          </div>
          <div className="divide-y divide-gray-100">
            {mockTodos.map(t => (
              <div key={t.id} className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 cursor-pointer">
                <div className={`flex h-2 w-2 rounded-full ${t.priority === 'high' ? 'bg-danger-500' : 'bg-primary-500'}`} />
                <div className="flex-1 min-w-0">
                  <div className="text-sm font-medium text-gray-900 truncate">{t.title}</div>
                  <div className="text-xs text-gray-400">{t.process} · {t.node}</div>
                </div>
                <span className="text-xs text-gray-400 whitespace-nowrap">{t.time}</span>
                <ChevronRight size={14} className="text-gray-300" />
              </div>
            ))}
          </div>
        </TableCard>

        {/* Notices */}
        <TableCard>
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h3 className="text-sm font-semibold text-gray-800">通知公告</h3>
            <Button variant="ghost" size="sm">查看全部 <ChevronRight size={13} /></Button>
          </div>
          <div className="divide-y divide-gray-100">
            {mockNotices.map(n => (
              <div key={n.id} className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 cursor-pointer">
                {!n.read && <span className="h-2 w-2 rounded-full bg-primary-500" />}
                {n.read && <span className="h-2 w-2 rounded-full bg-gray-200" />}
                <span className={`flex-1 text-sm ${n.read ? 'text-gray-500' : 'font-medium text-gray-900'}`}>{n.title}</span>
                <span className="text-xs text-gray-400 whitespace-nowrap">{n.time}</span>
              </div>
            ))}
          </div>
        </TableCard>
      </div>
    </div>
  );
}
