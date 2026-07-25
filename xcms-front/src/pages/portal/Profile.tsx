import { useState } from 'react';
import { User, Lock, Palette, Shield, Mail, Phone, Building2, Smartphone, Monitor, Tablet, LogOut, KeyRound, Eye, CheckCircle } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

const mockRoles = [
  { id: 1, code: 'TENANT_ADMIN', name: '租户管理员', scope: '租户级', scopeValue: '集团总部', status: 'ACTIVE' },
  { id: 2, code: 'DEPT_MANAGER', name: '部门主管', scope: '部门级', scopeValue: '技术研发部', status: 'ACTIVE' },
];

const mockPermissions = [
  { module: '租户管理', perms: ['查看', '创建', '编辑'] },
  { module: '用户管理', perms: ['查看', '创建', '编辑', '删除'] },
  { module: '组织架构', perms: ['查看', '创建'] },
  { module: '流程管理', perms: ['查看', '发起', '审批'] },
  { module: '文件管理', perms: ['查看', '上传', '下载', '分享'] },
];

const mockDevices = [
  { id: 1, name: 'MacBook Pro', device: 'macOS · Chrome', ip: '192.168.1.100', lastActive: '2025-07-25 10:30', current: true },
  { id: 2, name: 'iPhone 15', device: 'iOS · Safari', ip: '10.0.0.50', lastActive: '2025-07-24 18:00', current: false },
  { id: 3, name: 'iPad Pro', device: 'iPadOS · Chrome', ip: '10.0.0.51', lastActive: '2025-07-20 14:00', current: false },
];

const deviceIcons: Record<string, typeof Monitor> = { macOS: Monitor, iOS: Smartphone, iPadOS: Tablet };

export default function ProfilePage() {
  const [tab, setTab] = useState<'info' | 'security' | 'preference' | 'permission'>('info');

  const tabBtn = (key: typeof tab, label: string, Icon: typeof User) => (
    <button onClick={() => setTab(key)} className={`flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 ${tab === key ? 'border-primary-500 text-primary-600' : 'border-transparent text-gray-400'}`}>
      {Icon && <Icon size={15} />} {label}
    </button>
  );

  return (
    <div>
      <PageHeader title="个人中心" description="个人信息、安全设置、偏好设置、我的权限" />
      <TableCard>
        <div className="flex gap-1 border-b border-gray-100 px-4 pt-3">
          {tabBtn('info', '个人信息', User)}
          {tabBtn('security', '安全设置', Lock)}
          {tabBtn('preference', '偏好设置', Palette)}
          {tabBtn('permission', '我的权限', Shield)}
        </div>

        {/* Personal Info */}
        {tab === 'info' && (
          <div className="p-6">
            <div className="flex items-center gap-4 mb-6">
              <div className="h-20 w-20 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center">
                <User size={32} className="text-white" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-gray-900">超级管理员</h2>
                <p className="text-sm text-gray-500">admin · 集团总部 · L0</p>
                <Button variant="secondary" size="sm" className="mt-2">更换头像</Button>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 max-w-2xl">
              {[
                { label: '用户名', value: 'admin', icon: User },
                { label: '姓名', value: '超级管理员', icon: User },
                { label: '邮箱', value: 'admin@xcms.com', icon: Mail },
                { label: '手机号', value: '138****0001', icon: Phone },
                { label: '所属租户', value: '集团总部', icon: Building2 },
                { label: '部门', value: '集团总部', icon: Building2 },
              ].map(field => (
                <div key={field.label}>
                  <label className="mb-1 block text-xs text-gray-400">{field.label}</label>
                  <div className="flex items-center gap-2 rounded-md border border-gray-200 px-3 py-2">
                    <field.icon size={14} className="text-gray-400" />
                    <span className="text-sm text-gray-700">{field.value}</span>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-6 flex justify-end">
              <Button variant="primary">保存修改</Button>
            </div>
          </div>
        )}

        {/* Security Settings */}
        {tab === 'security' && (
          <div className="p-6 space-y-6">
            {/* Change Password */}
            <div>
              <h3 className="mb-3 text-sm font-semibold text-gray-800 flex items-center gap-2"><KeyRound size={16} /> 修改密码</h3>
              <div className="max-w-md space-y-3">
                <div>
                  <label className="mb-1 block text-xs text-gray-400">当前密码</label>
                  <div className="relative">
                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input type="password" placeholder="请输入当前密码" className="h-9 w-full rounded-md border border-gray-300 pl-9 pr-9 text-sm outline-none focus:border-primary-500" />
                    <Eye size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 cursor-pointer" />
                  </div>
                </div>
                <div>
                  <label className="mb-1 block text-xs text-gray-400">新密码</label>
                  <div className="relative">
                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input type="password" placeholder="请输入新密码" className="h-9 w-full rounded-md border border-gray-300 pl-9 pr-9 text-sm outline-none focus:border-primary-500" />
                  </div>
                </div>
                <div>
                  <label className="mb-1 block text-xs text-gray-400">确认新密码</label>
                  <div className="relative">
                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input type="password" placeholder="请再次输入新密码" className="h-9 w-full rounded-md border border-gray-300 pl-9 pr-9 text-sm outline-none focus:border-primary-500" />
                  </div>
                </div>
                <Button variant="primary">确认修改</Button>
              </div>
            </div>

            {/* Two-Factor */}
            <div className="pt-4 border-t border-gray-100">
              <h3 className="mb-3 text-sm font-semibold text-gray-800 flex items-center gap-2"><Shield size={16} /> 二次验证</h3>
              <div className="flex items-center justify-between rounded-lg border border-gray-200 p-4 max-w-md">
                <div>
                  <div className="text-sm font-medium text-gray-800">两步验证</div>
                  <div className="text-xs text-gray-400">登录时需要输入验证码</div>
                </div>
                <button className="relative h-6 w-11 rounded-full bg-gray-300">
                  <span className="absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-white" />
                </button>
              </div>
            </div>

            {/* Login Devices */}
            <div className="pt-4 border-t border-gray-100">
              <h3 className="mb-3 text-sm font-semibold text-gray-800 flex items-center gap-2"><Monitor size={16} /> 登录设备管理</h3>
              <div className="space-y-2 max-w-2xl">
                {mockDevices.map(d => {
                  const DeviceIcon = deviceIcons[d.device.split(' ')[0]] || Monitor;
                  return (
                    <div key={d.id} className="flex items-center gap-3 rounded-lg border border-gray-200 p-3">
                      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100">
                        <DeviceIcon size={16} className="text-gray-500" />
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-gray-800">{d.name}</span>
                          {d.current && <span className="rounded bg-success-50 px-1.5 py-0.5 text-[10px] text-success-500">当前设备</span>}
                        </div>
                        <div className="text-xs text-gray-400">{d.device} · {d.ip} · {d.lastActive}</div>
                      </div>
                      {!d.current && (
                        <button className="flex items-center gap-1 text-xs text-danger-500 hover:underline">
                          <LogOut size={13} /> 下线
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Preferences */}
        {tab === 'preference' && (
          <div className="p-6 space-y-6">
            <div>
              <h3 className="mb-3 text-sm font-semibold text-gray-800">界面偏好</h3>
              <div className="grid grid-cols-2 gap-3 max-w-lg">
                <div className="rounded-lg border border-gray-200 p-3">
                  <label className="block text-xs text-gray-400 mb-1">主题模式</label>
                  <div className="flex gap-2">
                    <button className="flex items-center gap-1.5 rounded border-2 border-primary-500 px-3 py-1 text-xs"><Palette size={13} /> 亮色</button>
                    <button className="flex items-center gap-1.5 rounded border border-gray-200 px-3 py-1 text-xs">暗色</button>
                  </div>
                </div>
                <div className="rounded-lg border border-gray-200 p-3">
                  <label className="block text-xs text-gray-400 mb-1">语言</label>
                  <select className="h-8 w-full rounded border border-gray-300 text-sm outline-none"><option>简体中文</option><option>English</option></select>
                </div>
                <div className="rounded-lg border border-gray-200 p-3">
                  <label className="block text-xs text-gray-400 mb-1">界面密度</label>
                  <select className="h-8 w-full rounded border border-gray-300 text-sm outline-none"><option>标准</option><option>紧凑</option><option>宽松</option></select>
                </div>
                <div className="rounded-lg border border-gray-200 p-3">
                  <label className="block text-xs text-gray-400 mb-1">时区</label>
                  <select className="h-8 w-full rounded border border-gray-300 text-sm outline-none"><option>Asia/Shanghai (UTC+8)</option><option>UTC</option></select>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-gray-100">
              <h3 className="mb-3 text-sm font-semibold text-gray-800">通知偏好</h3>
              <div className="space-y-2 max-w-lg">
                {[
                  { name: '待办任务提醒', desc: '有新待办时通知', enabled: true },
                  { name: '流程审批提醒', desc: '流程到达时通知', enabled: true },
                  { name: '消息通知', desc: '收到消息时通知', enabled: true },
                  { name: '系统公告', desc: '系统公告通知', enabled: false },
                ].map(n => (
                  <div key={n.name} className="flex items-center justify-between rounded-lg border border-gray-200 p-3">
                    <div>
                      <div className="text-sm font-medium text-gray-800">{n.name}</div>
                      <div className="text-xs text-gray-400">{n.desc}</div>
                    </div>
                    <button className={`relative h-6 w-11 rounded-full transition-colors ${n.enabled ? 'bg-success-500' : 'bg-gray-300'}`}>
                      <span className={`absolute top-0.5 h-5 w-5 rounded-full bg-white transition-transform ${n.enabled ? 'translate-x-5' : 'translate-x-0.5'}`} />
                    </button>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-end"><Button variant="primary">保存设置</Button></div>
          </div>
        )}

        {/* Permissions */}
        {tab === 'permission' && (
          <div className="p-6 space-y-6">
            {/* Roles */}
            <div>
              <h3 className="mb-3 text-sm font-semibold text-gray-800">我的角色</h3>
              <div className="grid grid-cols-2 gap-3 max-w-2xl">
                {mockRoles.map(r => (
                  <div key={r.id} className="rounded-lg border border-gray-200 p-4">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-50"><Shield size={16} className="text-primary-500" /></div>
                        <span className="text-sm font-semibold text-gray-900">{r.name}</span>
                      </div>
                      <StatusBadge status={r.status} />
                    </div>
                    <div className="text-xs text-gray-400">
                      <code className="font-mono">{r.code}</code>
                      <span className="ml-2">{r.scope} · {r.scopeValue}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Permissions */}
            <div className="pt-4 border-t border-gray-100">
              <h3 className="mb-3 text-sm font-semibold text-gray-800">菜单权限</h3>
              <div className="max-w-2xl space-y-2">
                {mockPermissions.map(p => (
                  <div key={p.module} className="flex items-center gap-3 rounded-lg border border-gray-200 p-3">
                    <span className="text-sm font-medium text-gray-700 w-24">{p.module}</span>
                    <div className="flex flex-wrap gap-1.5">
                      {p.perms.map(perm => (
                        <span key={perm} className="flex items-center gap-1 rounded bg-success-50 px-2 py-0.5 text-[11px] text-success-500">
                          <CheckCircle size={11} /> {perm}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Data Scope */}
            <div className="pt-4 border-t border-gray-100">
              <h3 className="mb-3 text-sm font-semibold text-gray-800">数据范围</h3>
              <div className="rounded-lg border border-gray-200 p-4 max-w-2xl">
                <div className="flex items-center gap-2 mb-2">
                  <Eye size={16} className="text-primary-500" />
                  <span className="text-sm font-medium text-gray-800">本部门及子部门</span>
                </div>
                <p className="text-xs text-gray-400">可查看本部门及下属部门的数据，跨租户数据需额外授权。</p>
              </div>
            </div>
          </div>
        )}
      </TableCard>
    </div>
  );
}
