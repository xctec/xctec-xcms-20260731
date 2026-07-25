import { useState } from 'react';
import { Plus, Building2, ChevronRight, ChevronDown, MoreHorizontal } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { PageHeader, TableCard } from '@/components/ui/PageHeader';

interface DeptItem {
  id: number; name: string; code: string; manager: string; userCount: number; status: string; children?: DeptItem[];
}

const mockDepts: DeptItem[] = [
  { id: 1, name: '集团总部', code: 'ROOT', manager: '张三', userCount: 50, status: 'ACTIVE', children: [
    { id: 2, name: '技术研发部', code: 'TECH', manager: '李四', userCount: 30, status: 'ACTIVE' },
    { id: 3, name: '产品设计部', code: 'PRODUCT', manager: '王五', userCount: 15, status: 'ACTIVE' },
    { id: 4, name: '财务管理部', code: 'FINANCE', manager: '赵六', userCount: 8, status: 'ACTIVE' },
  ]},
];

function DeptRow({ dept, level = 0 }: { dept: DeptItem; level?: number }) {
  const [expanded, setExpanded] = useState(true);
  const hasChildren = dept.children && dept.children.length > 0;
  return (
    <>
      <tr className="border-b border-gray-100 hover:bg-primary-50/50">
        <td className="px-4 py-2.5" style={{ paddingLeft: `${16 + level * 24}px` }}>
          <div className="flex items-center gap-2">
            {hasChildren ? (
              <button onClick={() => setExpanded(!expanded)} className="text-gray-400 hover:text-gray-600">
                {expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
              </button>
            ) : <span className="w-3.5" />}
            <Building2 size={14} className="text-gray-400" />
            <span className="font-medium text-gray-900">{dept.name}</span>
          </div>
        </td>
        <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{dept.code}</code></td>
        <td className="px-4 text-gray-500">{dept.manager}</td>
        <td className="px-4 text-center text-gray-500">{dept.userCount}</td>
        <td className="px-4 text-center"><StatusBadge status={dept.status} /></td>
        <td className="px-4 text-right">
          <button className="text-xs font-medium text-primary-500 hover:text-primary-700">编辑</button>
          <button className="ml-3 text-xs text-gray-400 hover:text-gray-600">新增子部门</button>
          <button className="ml-2 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
        </td>
      </tr>
      {hasChildren && expanded && dept.children!.map((child) => (
        <DeptRow key={child.id} dept={child} level={level + 1} />
      ))}
    </>
  );
}

export default function OrganizationPage() {
  return (
    <div>
      <PageHeader title="组织架构" description="管理部门树、岗位、用户组" actions={<Button variant="primary" icon={Plus}>新建部门</Button>} />
      <div className="grid grid-cols-3 gap-4">
        {/* Left: Dept Tree */}
        <TableCard>
          <div className="border-b border-gray-100 px-4 py-3">
            <h3 className="text-sm font-semibold text-gray-800">部门列表</h3>
          </div>
          <table className="w-full text-[13px]">
            <thead><tr className="border-b border-gray-200 bg-gray-50">
              <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">部门名称</th>
              <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">编码</th>
              <th className="px-4 py-2 text-center text-xs font-medium text-gray-500">人数</th>
            </tr></thead>
            <tbody>
              <tr className="border-b border-gray-100 hover:bg-gray-50"><td className="px-4 py-2 text-primary-500 font-medium">集团总部</td><td className="px-4 text-gray-500">ROOT</td><td className="px-4 text-center text-gray-500">50</td></tr>
              <tr className="border-b border-gray-100 hover:bg-gray-50"><td className="px-4 py-2 pl-8 text-gray-700">├ 技术研发部</td><td className="px-4 text-gray-500">TECH</td><td className="px-4 text-center text-gray-500">30</td></tr>
              <tr className="border-b border-gray-100 hover:bg-gray-50"><td className="px-4 py-2 pl-8 text-gray-700">├ 产品设计部</td><td className="px-4 text-gray-500">PRODUCT</td><td className="px-4 text-center text-gray-500">15</td></tr>
              <tr className="hover:bg-gray-50"><td className="px-4 py-2 pl-8 text-gray-700">└ 财务管理部</td><td className="px-4 text-gray-500">FINANCE</td><td className="px-4 text-center text-gray-500">8</td></tr>
            </tbody>
          </table>
        </TableCard>

        {/* Right: Positions + Groups */}
        <div className="col-span-2 space-y-4">
          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <h3 className="text-sm font-semibold text-gray-800">岗位管理</h3>
              <Button variant="ghost" size="sm" icon={Plus}>新增岗位</Button>
            </div>
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['岗位名称', '编码', '所属部门', '职级', '状态', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2 text-xs font-medium text-gray-500 ${[4].includes(i) ? 'text-center' : i === 5 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {[
                  { name: '技术总监', code: 'TECH_DIR', dept: '技术研发部', level: 'L8', status: 'ACTIVE' },
                  { name: '高级工程师', code: 'SE', dept: '技术研发部', level: 'L6', status: 'ACTIVE' },
                  { name: '产品经理', code: 'PM', dept: '产品设计部', level: 'L6', status: 'ACTIVE' },
                  { name: '财务主管', code: 'FIN_MGR', dept: '财务管理部', level: 'L7', status: 'ACTIVE' },
                ].map((p, i) => (
                  <tr key={i} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5 font-medium text-gray-900">{p.name}</td>
                    <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{p.code}</code></td>
                    <td className="px-4 text-gray-500">{p.dept}</td>
                    <td className="px-4 text-gray-500">{p.level}</td>
                    <td className="px-4 text-center"><StatusBadge status={p.status} /></td>
                    <td className="px-4 text-right"><button className="text-xs text-primary-500">编辑</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </TableCard>

          <TableCard>
            <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
              <h3 className="text-sm font-semibold text-gray-800">用户组管理</h3>
              <Button variant="ghost" size="sm" icon={Plus}>新增用户组</Button>
            </div>
            <table className="w-full text-[13px]">
              <thead><tr className="border-b border-gray-200 bg-gray-50">
                {['组名称', '类型', '描述', '成员数', '操作'].map((h, i) => (
                  <th key={h} className={`px-4 py-2 text-xs font-medium text-gray-500 ${i === 4 ? 'text-right' : 'text-left'}`}>{h}</th>
                ))}
              </tr></thead>
              <tbody>
                {[
                  { name: '技术评审组', type: '跨部门', desc: '技术方案评审', count: 8 },
                  { name: '安全应急组', type: '安全', desc: '安全事件响应', count: 5 },
                ].map((g, i) => (
                  <tr key={i} className="border-b border-gray-100 hover:bg-primary-50/50">
                    <td className="px-4 py-2.5 font-medium text-gray-900">{g.name}</td>
                    <td className="px-4"><span className="rounded bg-primary-50 px-1.5 py-0.5 text-[11px] text-primary-600">{g.type}</span></td>
                    <td className="px-4 text-gray-500">{g.desc}</td>
                    <td className="px-4 text-gray-500">{g.count}</td>
                    <td className="px-4 text-right"><button className="text-xs text-primary-500">管理</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </TableCard>
        </div>
      </div>
    </div>
  );
}
