import { useState } from 'react';
import { Plus, Bell, Mail, MessageSquare, Send, MoreHorizontal, type LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockMessages = [
  { id: 1, title: '采购审批流程待处理', type: 'TODO', channel: '站内信', recipient: '张三', content: '您有一条采购审批待处理', sendTime: '2025-07-25 10:30', status: 'SUCCESS', read: false },
  { id: 2, title: '系统维护通知', type: 'NOTICE', channel: '邮件', recipient: '全体用户', content: '系统将于今晚22:00进行维护', sendTime: '2025-07-25 09:00', status: 'SUCCESS', read: true },
  { id: 3, title: '密码即将过期', type: 'ALERT', channel: '站内信', recipient: '李四', content: '您的密码将在7天后过期', sendTime: '2025-07-24 16:00', status: 'SUCCESS', read: false },
  { id: 4, title: '新用户入职欢迎', type: 'NOTICE', channel: '邮件', recipient: '王五', content: '欢迎加入XCMS平台', sendTime: '2025-07-24 14:00', status: 'SUCCESS', read: true },
  { id: 5, title: '流程审批超时', type: 'ALERT', channel: '站内信', recipient: '赵六', content: '报销审批已超时24小时', sendTime: '2025-07-23 11:00', status: 'FAILED', read: false },
  { id: 6, title: '月度报表已生成', type: 'NOTICE', channel: '站内信', recipient: '全体管理员', content: '7月运营月报已生成', sendTime: '2025-07-22 08:00', status: 'SUCCESS', read: true },
  { id: 7, title: '权限变更通知', type: 'NOTICE', channel: '站内信', recipient: '钱七', content: '您的角色已变更', sendTime: '2025-07-21 15:00', status: 'SUCCESS', read: true },
];

const typeLabels: Record<string, { label: string; color: string }> = {
  TODO: { label: '待办', color: 'bg-primary-50 text-primary-600' },
  NOTICE: { label: '通知', color: 'bg-info-50 text-info-500' },
  ALERT: { label: '告警', color: 'bg-warning-50 text-warning-500' },
};
const channelIcons: Record<string, LucideIcon> = { 站内信: Bell, 邮件: Mail, 短信: MessageSquare };

export default function MessagePage() {
  const [page, setPage] = useState(1);
  const [tab, setTab] = useState<'list' | 'template'>('list');

  return (
    <div>
      <PageHeader title="消息管理" description="消息列表、消息模板、消息发送" actions={
        <>
          <Button variant="secondary" icon={MessageSquare}>模板管理</Button>
          <Button variant="primary" icon={Send}>发送消息</Button>
        </>
      } />
      <TableCard>
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          {['list', 'template'].map(t => (
            <button key={t} onClick={() => setTab(t as 'list' | 'template')} className={`px-3 py-2 text-sm font-medium border-b-2 ${tab === t ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
              {t === 'list' ? '消息列表' : '消息模板'}
            </button>
          ))}
        </div>

        {tab === 'list' ? (
          <>
            <FilterBar searchPlaceholder="搜索消息标题..." onSearch={() => {}} filters={
              <>{['全部', '待办', '通知', '告警'].map((f, i) => (
                <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
              ))}</>
            } />
            <div className="overflow-x-auto">
              <table className="w-full text-[13px]">
                <thead><tr className="border-b border-gray-200 bg-gray-50">
                  {['标题', '类型', '渠道', '接收人', '发送时间', '状态', '操作'].map((h, i) => (
                    <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[5].includes(i) ? 'text-center' : i === 6 ? 'text-right' : 'text-left'}`}>{h}</th>
                  ))}
                </tr></thead>
                <tbody>
                  {mockMessages.map(m => {
                    const ChannelIcon = channelIcons[m.channel] || Bell;
                    return (
                      <tr key={m.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                        <td className="px-4 py-2.5">
                          <div className="flex items-center gap-2">
                            {!m.read && <span className="h-1.5 w-1.5 rounded-full bg-danger-500" />}
                            <span className={`font-medium ${m.read ? 'text-gray-600' : 'text-gray-900'}`}>{m.title}</span>
                          </div>
                        </td>
                        <td className="px-4"><span className={`rounded px-1.5 py-0.5 text-[11px] ${typeLabels[m.type]?.color}`}>{typeLabels[m.type]?.label}</span></td>
                        <td className="px-4"><div className="flex items-center gap-1 text-gray-500"><ChannelIcon size={13} />{m.channel}</div></td>
                        <td className="px-4 text-gray-500">{m.recipient}</td>
                        <td className="px-4 text-[11px] text-gray-400">{m.sendTime}</td>
                        <td className="px-4 text-center"><StatusBadge status={m.status} /></td>
                        <td className="px-4 text-right"><button className="text-xs text-primary-500">详情</button></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination page={page} total={7} size={10} onChange={setPage} />
          </>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['模板编码', '模板名称', '消息类型', '渠道', '变量数', '状态', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[5].includes(i) ? 'text-center' : i === 6 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {[
                  { code: 'TPL_TODO', name: '待办提醒', type: 'TODO', channel: '站内信', vars: 3, status: 'ACTIVE' },
                  { code: 'TPL_NOTICE', name: '系统通知', type: 'NOTICE', channel: '邮件', vars: 2, status: 'ACTIVE' },
                  { code: 'TPL_ALERT', name: '告警通知', type: 'ALERT', channel: '站内信', vars: 4, status: 'ACTIVE' },
                ].map(t => (
                  <tr key={t.code} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5"><code className="font-mono text-[12px] text-primary-600">{t.code}</code></td>
                    <td className="px-4 font-medium text-gray-900">{t.name}</td>
                    <td className="px-4"><span className={`rounded px-1.5 py-0.5 text-[11px] ${typeLabels[t.type]?.color}`}>{typeLabels[t.type]?.label}</span></td>
                    <td className="px-4 text-gray-500">{t.channel}</td>
                    <td className="px-4 text-gray-500">{t.vars} 个</td>
                    <td className="px-4 text-center"><StatusBadge status={t.status} /></td>
                    <td className="px-4 text-right"><button className="text-xs text-primary-500">编辑</button></td>
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
