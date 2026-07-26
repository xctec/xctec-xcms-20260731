import { useState } from 'react';
import { Bell, Mail, Settings, Search, Trash2, Check, MoreHorizontal } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const mockMessages = [
  { id: 1, title: '采购审批待处理', type: 'TODO', sender: '流程引擎', content: '您有一条采购审批任务待处理，请尽快办理', time: '2025-07-25 10:30', read: false, starred: false },
  { id: 2, title: '系统维护通知', type: 'NOTICE', sender: '系统管理员', content: '系统将于今晚22:00-23:00进行维护升级，请提前保存工作', time: '2025-07-25 09:00', read: false, starred: true },
  { id: 3, title: '密码即将过期提醒', type: 'ALERT', sender: '安全中心', content: '您的账号密码将在7天后过期，请及时修改', time: '2025-07-24 16:00', read: false, starred: false },
  { id: 4, title: '新同事入职欢迎', type: 'NOTICE', sender: '人力资源部', content: '欢迎新同事加入团队！请查看入职指南。', time: '2025-07-24 14:00', read: true, starred: false },
  { id: 5, title: '报销审批超时', type: 'ALERT', sender: '流程引擎', content: '您的报销审批已超时24小时，请尽快处理', time: '2025-07-23 11:00', read: true, starred: false },
  { id: 6, title: '月度运营报表已生成', type: 'NOTICE', sender: '运营中心', content: '7月运营月报已生成，请查看详情', time: '2025-07-22 08:00', read: true, starred: true },
  { id: 7, title: '权限变更通知', type: 'NOTICE', sender: '权限中心', content: '您的角色已更新，新增了文件管理权限', time: '2025-07-21 15:00', read: true, starred: false },
  { id: 8, title: '版本更新通知', type: 'NOTICE', sender: '系统管理员', content: 'XCMS v2.1 已发布，新增多项功能', time: '2025-07-20 10:00', read: true, starred: false },
];

const typeLabels: Record<string, { label: string; color: string; icon: typeof Bell }> = {
  TODO: { label: '待办', color: 'bg-primary-50 text-primary-600', icon: Bell },
  NOTICE: { label: '通知', color: 'bg-info-50 text-info-500', icon: Mail },
  ALERT: { label: '告警', color: 'bg-warning-50 text-warning-500', icon: Bell },
};

export default function PortalMessagePage() {
  const [tab, setTab] = useState<'inbox' | 'notice' | 'settings'>('inbox');
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');

  const filtered = mockMessages.filter(m => !keyword || m.title.includes(keyword));
  const unreadCount = mockMessages.filter(m => !m.read).length;

  const tabBtn = (key: typeof tab, label: string, Icon: typeof Bell, badge?: number) => (
    <button onClick={() => setTab(key)} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === key ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
      {Icon && <Icon size={15} />} {label}
      {badge && <span className="ml-1 rounded-full bg-danger-500 px-1.5 text-[10px] text-white">{badge}</span>}
    </button>
  );

  return (
    <div>
      <PageHeader title="消息中心" description="站内信、通知公告、消息设置" />
      <TableCard>
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          {tabBtn('inbox', '收件箱', Bell, unreadCount)}
          {tabBtn('notice', '系统通知', Mail)}
          {tabBtn('settings', '消息设置', Settings)}
        </div>

        {tab === 'inbox' && (
          <>
            <FilterBar searchValue={keyword} searchPlaceholder="搜索消息标题..." onSearch={setKeyword} filters={
              <>{['全部', '未读', '待办', '告警'].map((f, i) => (
                <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
              ))}</>
            } />

            {/* Batch Actions */}
            <div className="flex items-center gap-3 border-b border-gray-100 px-4 py-2 bg-gray-50/50">
              <button className="flex items-center gap-1 text-xs text-gray-500 hover:text-primary-500"><Check size={13} />全部标记已读</button>
              <button className="flex items-center gap-1 text-xs text-gray-500 hover:text-danger-500"><Trash2 size={13} />删除已选</button>
            </div>

            <div className="divide-y divide-gray-100">
              {filtered.map(m => {
                const TypeIcon = typeLabels[m.type]?.icon || Bell;
                return (
                  <div key={m.id} className={`flex items-start gap-3 px-4 py-3 hover:bg-primary-50/50 cursor-pointer ${!m.read ? 'bg-primary-50/20' : ''}`}>
                    <input type="checkbox" className="mt-1 rounded border-gray-300 text-primary-500" />
                    <div className={`mt-1 flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg ${typeLabels[m.type]?.color}`}>
                      <TypeIcon size={14} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        {!m.read && <span className="h-2 w-2 rounded-full bg-danger-500" />}
                        <span className={`text-sm ${m.read ? 'font-normal text-gray-600' : 'font-semibold text-gray-900'}`}>{m.title}</span>
                        <span className={`rounded px-1.5 py-0.5 text-[10px] ${typeLabels[m.type]?.color}`}>{typeLabels[m.type]?.label}</span>
                      </div>
                      <p className="mt-0.5 truncate text-xs text-gray-400">{m.content}</p>
                      <div className="mt-1 flex items-center gap-2 text-[11px] text-gray-400">
                        <span>{m.sender}</span><span>·</span><span>{m.time}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-1">
                      <button className="text-gray-300 hover:text-warning-500"><Mail size={14} /></button>
                      <button className="text-gray-300 hover:text-danger-500"><Trash2 size={14} /></button>
                      <button className="text-gray-300 hover:text-gray-500"><MoreHorizontal size={14} /></button>
                    </div>
                  </div>
                );
              })}
            </div>
            <Pagination page={page} total={filtered.length} size={10} onChange={setPage} />
          </>
        )}

        {tab === 'notice' && (
          <div className="p-4 space-y-3">
            {mockMessages.filter(m => m.type === 'NOTICE').map(m => (
              <div key={m.id} className="rounded-lg border border-gray-200 p-4 hover:shadow-card cursor-pointer">
                <div className="flex items-center justify-between mb-1">
                  <h3 className="text-sm font-semibold text-gray-900">{m.title}</h3>
                  <span className="text-[11px] text-gray-400">{m.time}</span>
                </div>
                <p className="text-xs text-gray-500">{m.content}</p>
                <div className="mt-2 text-[11px] text-gray-400">来源: {m.sender}</div>
              </div>
            ))}
          </div>
        )}

        {tab === 'settings' && (
          <div className="p-4 space-y-4">
            <h3 className="text-sm font-semibold text-gray-800">接收方式</h3>
            {[
              { name: '站内信', desc: '在系统内接收消息通知', enabled: true },
              { name: '邮件通知', desc: '将消息同步发送到邮箱', enabled: true },
              { name: '短信通知', desc: '紧急消息通过短信发送', enabled: false },
              { name: '待办提醒', desc: '流程待办实时提醒', enabled: true },
            ].map(s => (
              <div key={s.name} className="flex items-center justify-between rounded-lg border border-gray-200 p-3">
                <div>
                  <div className="text-sm font-medium text-gray-800">{s.name}</div>
                  <div className="text-xs text-gray-400">{s.desc}</div>
                </div>
                <button className={`relative h-6 w-11 rounded-full transition-colors ${s.enabled ? 'bg-success-500' : 'bg-gray-300'}`}>
                  <span className={`absolute top-0.5 h-5 w-5 rounded-full bg-white transition-transform ${s.enabled ? 'translate-x-5' : 'translate-x-0.5'}`} />
                </button>
              </div>
            ))}

            <h3 className="pt-4 text-sm font-semibold text-gray-800">免打扰时段</h3>
            <div className="flex items-center gap-3 rounded-lg border border-gray-200 p-3">
              <span className="text-sm text-gray-600">每日</span>
              <input type="time" defaultValue="22:00" className="rounded border border-gray-300 px-2 py-1 text-sm" />
              <span className="text-gray-400">至</span>
              <input type="time" defaultValue="08:00" className="rounded border border-gray-300 px-2 py-1 text-sm" />
              <span className="text-xs text-gray-400">该时段内不接收非紧急消息</span>
            </div>

            <div className="flex justify-end">
              <Button variant="primary">保存设置</Button>
            </div>
          </div>
        )}
      </TableCard>
    </div>
  );
}
