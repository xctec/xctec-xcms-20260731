import { useState } from 'react';
import { Plus, Download, MoreHorizontal, User } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';
import { Can } from '@/components/auth/Can';

const mockUsers = [
  { id: 1, username: 'admin', realName: '超级管理员', email: 'admin@xcms.com', phone: '13800000001', dept: '集团总部', role: '系统管理员', status: 'ACTIVE', createdAt: '2025-01-15' },
  { id: 2, username: 'zhangsan', realName: '张三', email: 'zhangsan@xcms.com', phone: '13800000002', dept: '技术研发部', role: '部门主管', status: 'ACTIVE', createdAt: '2025-02-01' },
  { id: 3, username: 'lisi', realName: '李四', email: 'lisi@xcms.com', phone: '13800000003', dept: '技术研发部', role: '高级工程师', status: 'ACTIVE', createdAt: '2025-02-15' },
  { id: 4, username: 'wangwu', realName: '王五', email: 'wangwu@xcms.com', phone: '13800000004', dept: '产品设计部', role: '产品经理', status: 'ACTIVE', createdAt: '2025-03-01' },
  { id: 5, username: 'zhaoliu', realName: '赵六', email: 'zhaoliu@xcms.com', phone: '13800000005', dept: '财务管理部', role: '财务主管', status: 'LOCKED', createdAt: '2025-03-20' },
  { id: 6, username: 'qianqi', realName: '钱七', email: 'qianqi@xcms.com', phone: '13800000006', dept: '技术研发部', role: '工程师', status: 'ACTIVE', createdAt: '2025-04-10' },
  { id: 7, username: 'sunba', realName: '孙八', email: 'sunba@xcms.com', phone: '13800000007', dept: '产品设计部', role: '设计师', status: 'SUSPENDED', createdAt: '2025-05-05' },
  { id: 8, username: 'zhoujiu', realName: '周九', email: 'zhoujiu@xcms.com', phone: '13800000008', dept: '技术研发部', role: '工程师', status: 'ACTIVE', createdAt: '2025-06-12' },
  { id: 9, username: 'wushi', realName: '吴十', email: 'wushi@xcms.com', phone: '13800000009', dept: '财务管理部', role: '会计', status: 'ACTIVE', createdAt: '2025-06-20' },
];

export default function UserListPage() {
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const filtered = mockUsers.filter(u => !keyword || u.realName.includes(keyword) || u.username.includes(keyword));
  const paged = filtered.slice((page - 1) * 10, page * 10);

  return (
    <div>
      <PageHeader title="用户管理" description="管理系统用户、角色分配、状态管理" actions={
        <>
          {/* 按钮级权限：无 user:export 权限时不渲染（演示隐藏） */}
          <Can permission="user:export">
            <Button variant="secondary" icon={Download}>导出</Button>
          </Can>
          {/* 按钮级权限：拥有 user:create 才展示新建按钮 */}
          <Can permission="user:create">
            <Button variant="primary" icon={Plus}>新建用户</Button>
          </Can>
        </>
      } />
      <TableCard>
        <FilterBar searchValue={keyword} searchPlaceholder="搜索姓名/用户名..." onSearch={setKeyword} filters={
          <>
            {['全部', '正常', '已锁定', '已暂停'].map((f, i) => (
              <button key={f} className={`rounded-full px-3 py-1 text-xs font-medium ${i === 0 ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500'}`}>{f}</button>
            ))}
          </>
        } />
        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead><tr className="border-b border-gray-200 bg-gray-50">
              {['用户', '用户名', '邮箱', '手机号', '部门', '角色', '状态', '创建时间', '操作'].map((h, i) => (
                <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[6].includes(i) ? 'text-center' : i === 8 ? 'text-right' : 'text-left'}`}>{h}</th>
              ))}
            </tr></thead>
            <tbody>
              {paged.map(u => (
                <tr key={u.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                  <td className="px-4 py-2.5">
                    <div className="flex items-center gap-2">
                      <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary-100"><User size={14} className="text-primary-700" /></div>
                      <span className="font-medium text-gray-900">{u.realName}</span>
                    </div>
                  </td>
                  <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{u.username}</code></td>
                  <td className="px-4 text-gray-500">{u.email}</td>
                  <td className="px-4 text-gray-500">{u.phone}</td>
                  <td className="px-4 text-gray-500">{u.dept}</td>
                  <td className="px-4"><span className="rounded bg-primary-50 px-1.5 py-0.5 text-[11px] text-primary-600">{u.role}</span></td>
                  <td className="px-4 text-center"><StatusBadge status={u.status} /></td>
                  <td className="px-4 text-[11px] text-gray-400">{u.createdAt}</td>
                  <td className="px-4 text-right">
                    <button className="text-xs font-medium text-primary-500">编辑</button>
                    <Can permission="user:reset-pwd">
                      <button className="ml-2 text-xs text-gray-400">重置密码</button>
                    </Can>
                    <button className="ml-2 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Pagination page={page} total={filtered.length} size={10} onChange={setPage} />
      </TableCard>
    </div>
  );
}
