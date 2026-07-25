import { useState } from 'react';
import { Plus, Settings, BookOpen, ToggleLeft, MoreHorizontal, type LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

const mockParams = [
  { key: 'sys.tenant.maxLevel', value: '5', desc: '租户最大层级', type: 'NUMBER', status: 'ACTIVE' },
  { key: 'sys.session.timeout', value: '7200', desc: '会话超时(秒)', type: 'NUMBER', status: 'ACTIVE' },
  { key: 'sys.password.minLength', value: '8', desc: '密码最小长度', type: 'NUMBER', status: 'ACTIVE' },
  { key: 'sys.upload.maxSize', value: '104857600', desc: '上传文件大小限制(字节)', type: 'NUMBER', status: 'ACTIVE' },
  { key: 'sys.captcha.enabled', value: 'true', desc: '是否启用验证码', type: 'BOOLEAN', status: 'ACTIVE' },
];

const mockDicts = [
  { type: 'tenant_type', name: '租户类型', items: 4, status: 'ACTIVE' },
  { type: 'user_status', name: '用户状态', items: 5, status: 'ACTIVE' },
  { type: 'role_scope', name: '角色作用域', items: 3, status: 'ACTIVE' },
  { type: 'process_status', name: '流程状态', items: 6, status: 'ACTIVE' },
];

const mockFeatures = [
  { code: 'TENANT_MANAGEMENT', name: '租户管理', enabled: true, desc: '租户的增删改查' },
  { code: 'WORKFLOW_ENGINE', name: '流程引擎', enabled: true, desc: '流程审批功能' },
  { code: 'SSO_LOGIN', name: '单点登录', enabled: false, desc: 'SSO集成（Phase 3）' },
  { code: 'CROSS_TENANT_AUDIT', name: '跨租户审计', enabled: true, desc: '集团审计中心' },
  { code: 'INDEPENDENT_DEPLOYMENT', name: '独立部署', enabled: false, desc: '子公司独立部署（Phase 4）' },
  { code: 'API_RATE_LIMIT', name: 'API限流', enabled: true, desc: '租户级API限流' },
];

export default function ConfigPage() {
  const [tab, setTab] = useState<'param' | 'dict' | 'feature'>('param');

  const tabBtn = (key: typeof tab, label: string, Icon?: LucideIcon) => (
    <button onClick={() => setTab(key)} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === key ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
      {Icon && <Icon size={15} />} {label}
    </button>
  );

  return (
    <div>
      <PageHeader title="配置管理" description="系统参数、字典管理、功能开关" actions={
        tab === 'param' ? <Button variant="primary" icon={Plus}>新增参数</Button> :
        tab === 'dict' ? <Button variant="primary" icon={Plus}>新增字典</Button> :
        <></>
      } />
      <TableCard>
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          {tabBtn('param', '系统参数', Settings)}
          {tabBtn('dict', '字典管理', BookOpen)}
          {tabBtn('feature', '功能开关', ToggleLeft)}
        </div>

        {tab === 'param' && (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['参数Key', '参数值', '说明', '类型', '状态', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[4].includes(i) ? 'text-center' : i === 5 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {mockParams.map(p => (
                  <tr key={p.key} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5"><code className="font-mono text-[12px] text-primary-600">{p.key}</code></td>
                    <td className="px-4 font-medium text-gray-900">{p.value}</td>
                    <td className="px-4 text-gray-500">{p.desc}</td>
                    <td className="px-4"><span className="rounded bg-gray-100 px-1.5 py-0.5 text-[11px] text-gray-500">{p.type}</span></td>
                    <td className="px-4 text-center"><StatusBadge status={p.status} /></td>
                    <td className="px-4 text-right"><button className="text-xs font-medium text-primary-500">编辑</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === 'dict' && (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['字典类型', '字典名称', '字典项数', '状态', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[3].includes(i) ? 'text-center' : i === 4 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {mockDicts.map(d => (
                  <tr key={d.type} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5"><code className="font-mono text-[12px] text-primary-600">{d.type}</code></td>
                    <td className="px-4 font-medium text-gray-900">{d.name}</td>
                    <td className="px-4 text-gray-500">{d.items} 项</td>
                    <td className="px-4 text-center"><StatusBadge status={d.status} /></td>
                    <td className="px-4 text-right"><button className="text-xs font-medium text-primary-500">管理字典项</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === 'feature' && (
          <div className="divide-y divide-gray-100">
            {mockFeatures.map(f => (
              <div key={f.code} className="flex items-center justify-between px-4 py-3">
                <div className="flex items-center gap-3">
                  <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${f.enabled ? 'bg-success-50' : 'bg-gray-100'}`}>
                    <ToggleLeft size={18} className={f.enabled ? 'text-success-500' : 'text-gray-400'} />
                  </div>
                  <div>
                    <div className="text-sm font-medium text-gray-900">{f.name}</div>
                    <div className="text-[11px] text-gray-400">{f.desc}</div>
                  </div>
                </div>
                <button className={`relative h-6 w-11 rounded-full transition-colors ${f.enabled ? 'bg-success-500' : 'bg-gray-300'}`}>
                  <span className={`absolute top-0.5 h-5 w-5 rounded-full bg-white transition-transform ${f.enabled ? 'translate-x-5' : 'translate-x-0.5'}`} />
                </button>
              </div>
            ))}
          </div>
        )}
      </TableCard>
    </div>
  );
}
