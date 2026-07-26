import { useState } from 'react';
import { Plus, Shield, MoreHorizontal, Lock, Eye } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

const mockRoles = [
  { id: 1, code: 'SYSTEM_ADMIN', name: '系统管理员', desc: '拥有系统全部权限', scope: 'TENANT', scopeValue: null, userCount: 1, status: 'ACTIVE' },
  { id: 2, code: 'TENANT_ADMIN', name: '租户管理员', desc: '租户内全部管理权限', scope: 'TENANT', scopeValue: null, userCount: 3, status: 'ACTIVE' },
  { id: 3, code: 'DEPT_MANAGER', name: '部门主管', desc: '管理部门内用户和资源', scope: 'DEPT', scopeValue: '技术研发部', userCount: 5, status: 'ACTIVE' },
  { id: 4, code: 'FINANCE_USER', name: '财务人员', desc: '财务相关操作权限', scope: 'CUSTOM', scopeValue: '财务组', userCount: 4, status: 'ACTIVE' },
  { id: 5, code: 'VIEWER', name: '访客', desc: '只读权限', scope: 'TENANT', scopeValue: null, userCount: 2, status: 'ACTIVE' },
  { id: 6, code: 'AUDITOR', name: '审计员', desc: '审计日志查看权限', scope: 'TENANT', scopeValue: null, userCount: 1, status: 'ACTIVE' },
];

const scopeLabels: Record<string, string> = { TENANT: '租户级', DEPT: '部门级', CUSTOM: '自定义' };

export default function PermissionPage() {
  const [selectedRole, setSelectedRole] = useState(mockRoles[0]);

  const menuTree = [
    { name: '租户管理', children: ['查看', '创建', '编辑', '删除', '迁移'] },
    { name: '组织架构', children: ['查看', '创建', '编辑', '删除'] },
    { name: '用户管理', children: ['查看', '创建', '编辑', '删除', '重置密码'] },
    { name: '权限管理', children: ['查看', '角色管理', '权限分配'] },
    { name: '流程管理', children: ['查看', '发起', '审批'] },
    { name: '配置管理', children: ['查看', '参数管理', '字典管理'] },
  ];

  return (
    <div>
      <PageHeader title="权限管理" description="管理角色、菜单权限、数据权限" actions={<Button variant="primary" icon={Plus}>新建角色</Button>} />
      <div className="grid grid-cols-3 gap-4">
        {/* Role List */}
        <TableCard>
          <div className="border-b border-gray-100 px-4 py-3"><h3 className="text-sm font-semibold text-gray-800">角色列表</h3></div>
          <div className="p-2">
            {mockRoles.map(role => (
              <button key={role.id} onClick={() => setSelectedRole(role)} className={`w-full flex items-center gap-3 rounded-md px-3 py-2 text-left transition-colors ${selectedRole.id === role.id ? 'bg-primary-50' : 'hover:bg-gray-50'}`}>
                <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${selectedRole.id === role.id ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-400'}`}>
                  <Shield size={16} />
                </div>
                <div className="flex-1 min-w-0">
                  <div className={`text-sm font-medium truncate ${selectedRole.id === role.id ? 'text-primary-700' : 'text-gray-700'}`}>{role.name}</div>
                  <div className="text-[11px] text-gray-400">{role.userCount} 人 · {scopeLabels[role.scope]}</div>
                </div>
                <StatusBadge status={role.status} label="" />
              </button>
            ))}
          </div>
        </TableCard>

        {/* Permission Tree */}
        <div className="col-span-2 space-y-4">
          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <div>
                <h3 className="text-sm font-semibold text-gray-800">{selectedRole.name} — 菜单权限</h3>
                <p className="mt-0.5 text-[11px] text-gray-400">{selectedRole.desc}</p>
              </div>
              <Button variant="secondary" size="sm" icon={Lock}>保存权限</Button>
            </div>
            <div className="p-4">
              {menuTree.map((menu, mi) => (
                <div key={mi} className="mb-3">
                  <label className="flex items-center gap-2 py-1.5">
                    <input type="checkbox" defaultChecked={mi < 3} className="rounded border-gray-300 text-primary-500" />
                    <span className="text-sm font-medium text-gray-700">{menu.name}</span>
                  </label>
                  <div className="ml-6 flex flex-wrap gap-3">
                    {menu.children.map((perm, pi) => (
                      <label key={pi} className="flex items-center gap-1.5">
                        <input type="checkbox" defaultChecked={mi < 3} className="rounded border-gray-300 text-primary-500" />
                        <span className="text-xs text-gray-500">{perm}</span>
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </TableCard>

          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <h3 className="text-sm font-semibold text-gray-800">数据权限</h3>
              <Button variant="ghost" size="sm" icon={Eye}>预览</Button>
            </div>
            <div className="p-4">
              <div className="flex gap-4">
                {['全部数据', '本部门', '本部门及子部门', '自定义'].map((scope, i) => (
                  <label key={scope} className="flex items-center gap-1.5">
                    <input type="radio" name="dataScope" defaultChecked={i === 1} className="border-gray-300 text-primary-500" />
                    <span className="text-sm text-gray-600">{scope}</span>
                  </label>
                ))}
              </div>
            </div>
          </TableCard>
        </div>
      </div>
    </div>
  );
}
