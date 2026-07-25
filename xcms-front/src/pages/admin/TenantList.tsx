import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Plus, Download, RefreshCw, Building2, MoreHorizontal, ChevronRight } from 'lucide-react';
import { tenantApi } from '@/api/tenant';
import type { TenantDTO, TenantStatus } from '@/types/tenant';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';

const quickFilters = [
  { label: '全部', value: '', count: 8 },
  { label: '正常', value: 'ACTIVE', count: 5 },
  { label: '已暂停', value: 'SUSPENDED', count: 1 },
  { label: '已锁定', value: 'LOCKED', count: 1 },
  { label: '迁移中', value: 'MIGRATING', count: 1 },
];

export default function TenantListPage() {
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['tenants', { page, keyword, statusFilter }],
    queryFn: () => tenantApi.list({ parentId: 0, page, size: 10 }),
  });

  const tenants = data?.list || [];
  const total = data?.total || 0;
  const size = 10;

  return (
    <div>
      <PageHeader
        title="租户管理"
        description="管理集团下属所有租户、子公司及项目"
        actions={
          <>
            <Button variant="secondary" icon={Download} size="md">导出</Button>
            <Button variant="secondary" icon={RefreshCw} size="md">刷新</Button>
            <Button variant="primary" icon={Plus} size="md">新建租户</Button>
          </>
        }
      />

      <TableCard>
        <FilterBar
          searchValue={keyword}
          searchPlaceholder="搜索名称/编码..."
          onSearch={setKeyword}
          filters={
            quickFilters.map((f) => (
              <button
                key={f.value}
                onClick={() => setStatusFilter(f.value)}
                className={`flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                  statusFilter === f.value ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                }`}
              >
                {f.label}
                <span className="opacity-70">{f.count}</span>
              </button>
            ))
          }
        />

        {isLoading ? (
          <div className="flex h-40 items-center justify-center text-sm text-gray-400">加载中...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50">
                  {['租户名称', '编码', '类型', '上级', '层级', '用户数', '配额使用', '状态', '创建时间', '操作'].map((h, i) => (
                    <th key={h} className={`px-4 py-2.5 text-xs font-medium text-gray-500 ${[5, 6, 7].includes(i) ? 'text-center' : i === 9 ? 'text-right' : 'text-left'}`}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {tenants.map((t: TenantDTO) => {
                  const quotaPct = Math.round((1280 / 5000) * 100);
                  return (
                    <tr key={t.id} className="border-b border-gray-100 hover:bg-primary-50/50">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <ChevronRight size={12} className="text-gray-300" />
                          <div className="flex h-7 w-7 items-center justify-center rounded-md bg-primary-50">
                            <Building2 size={14} className="text-primary-500" />
                          </div>
                          <span className="font-medium text-gray-900">{t.tenantName}</span>
                        </div>
                      </td>
                      <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{t.tenantCode}</code></td>
                      <td className="px-4 text-gray-500">{t.tenantType}</td>
                      <td className="px-4 text-gray-400">-</td>
                      <td className="px-4 text-center text-gray-500">{t.level}</td>
                      <td className="px-4 text-center text-gray-500">128</td>
                      <td className="px-4">
                        <div className="flex items-center gap-2 min-w-[100px]">
                          <div className="flex-1 h-1.5 rounded-full bg-gray-100 overflow-hidden">
                            <div className="h-full rounded-full bg-success-500" style={{ width: `${quotaPct}%` }} />
                          </div>
                          <span className="text-[11px] text-gray-400 w-8">{quotaPct}%</span>
                        </div>
                      </td>
                      <td className="px-4 text-center"><StatusBadge status={t.status} /></td>
                      <td className="px-4 text-[11px] text-gray-400">{t.createdAt}</td>
                      <td className="px-4 text-right">
                        <button className="text-xs font-medium text-primary-500 hover:text-primary-700">详情</button>
                        <button className="ml-2 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <Pagination page={page} total={total} size={size} onChange={setPage} />
      </TableCard>
    </div>
  );
}
