import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Download, RefreshCw, Building2, MoreHorizontal, ChevronRight, Trash2, Edit, Power } from 'lucide-react';
import { tenantApi } from '@/api/tenant';
import type { TenantDTO, TenantStatus, TenantType } from '@/types/tenant';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Pagination } from '@/components/ui/Pagination';
import { PageHeader, FilterBar, TableCard } from '@/components/ui/PageHeader';
import { Modal } from '@/components/ui/Modal';
import { toast } from '@/components/ui/Toast';

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
  const [createOpen, setCreateOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<TenantDTO | null>(null);
  const [menuTarget, setMenuTarget] = useState<number | null>(null);
  const [createForm, setCreateForm] = useState({ tenantCode: '', tenantName: '', tenantType: 'ORGANIZATION', parentId: '' });
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['tenants', { page, keyword, statusFilter }],
    queryFn: () => tenantApi.list({ parentId: 0, query: { page, size: 10 } }),
  });

  const createMutation = useMutation({
    mutationFn: (data: { tenantCode: string; tenantName: string; tenantType: string; parentId: number }) =>
      tenantApi.create({ tenantCode: data.tenantCode, tenantName: data.tenantName, tenantType: data.tenantType as TenantType, parentId: data.parentId }),
    onSuccess: () => { toast.success('租户创建成功'); setCreateOpen(false); queryClient.invalidateQueries({ queryKey: ['tenants'] }); setCreateForm({ tenantCode: '', tenantName: '', tenantType: 'ORGANIZATION', parentId: '' }); },
    onError: (err) => toast.error(err instanceof Error ? err.message : '创建失败'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => tenantApi.delete(id),
    onSuccess: () => { toast.success('租户已删除'); setDeleteTarget(null); queryClient.invalidateQueries({ queryKey: ['tenants'] }); },
    onError: () => toast.error('删除失败'),
  });

  const tenants = data?.list || [];
  const total = data?.total || 0;

  const handleExport = () => {
    toast.success('正在导出租户列表...');
  };

  const handleRefresh = () => {
    queryClient.invalidateQueries({ queryKey: ['tenants'] });
    toast.success('数据已刷新');
  };

  const handleCreate = () => {
    if (!createForm.tenantCode || !createForm.tenantName) { toast.error('请填写完整信息'); return; }
    createMutation.mutate({ ...createForm, parentId: createForm.parentId ? Number(createForm.parentId) : 0 });
  };

  return (
    <div>
      <PageHeader
        title="租户管理"
        description="管理集团下属所有租户、子公司及项目"
        actions={<>
          <Button variant="secondary" icon={Download} onClick={handleExport}>导出</Button>
          <Button variant="secondary" icon={RefreshCw} onClick={handleRefresh}>刷新</Button>
          <Button variant="primary" icon={Plus} onClick={() => setCreateOpen(true)}>新建租户</Button>
        </>}
      />

      <TableCard>
        <FilterBar
          searchValue={keyword}
          searchPlaceholder="搜索名称/编码..."
          onSearch={setKeyword}
          filters={quickFilters.map(f => (
            <button key={f.value} onClick={() => setStatusFilter(f.value)} className={`flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium transition-colors ${statusFilter === f.value ? 'bg-primary-500 text-white' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}`}>{f.label}<span className="opacity-70">{f.count}</span></button>
          ))}
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
                          <div className="flex h-7 w-7 items-center justify-center rounded-md bg-primary-50"><Building2 size={14} className="text-primary-500" /></div>
                          <span className="font-medium text-gray-900">{t.tenantName}</span>
                        </div>
                      </td>
                      <td className="px-4"><code className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[11px] text-gray-500">{t.tenantCode}</code></td>
                      <td className="px-4 text-gray-500">{t.tenantType}</td>
                      <td className="px-4 text-gray-400">-</td>
                      <td className="px-4 text-center text-gray-500">{t.level}</td>
                      <td className="px-4 text-center text-gray-500">128</td>
                      <td className="px-4"><div className="flex items-center gap-2 min-w-[100px]"><div className="flex-1 h-1.5 rounded-full bg-gray-100 overflow-hidden"><div className="h-full rounded-full bg-success-500" style={{ width: `${quotaPct}%` }} /></div><span className="text-[11px] text-gray-400 w-8">{quotaPct}%</span></div></td>
                      <td className="px-4 text-center"><StatusBadge status={t.status ?? ''} /></td>
                      <td className="px-4 text-[11px] text-gray-400">{t.createdAt}</td>
                      <td className="px-4 text-right relative">
                        <button className="text-xs font-medium text-primary-500 hover:text-primary-700" onClick={() => toast.info(`查看租户详情: ${t.tenantName}`)}>详情</button>
                        <button onClick={() => setMenuTarget(menuTarget === t.id ? null : (t.id ?? null))} className="ml-2 text-gray-400 hover:text-gray-600"><MoreHorizontal size={14} /></button>
                        {menuTarget === t.id && (
                          <div className="absolute right-4 top-10 z-20 w-32 rounded-lg border border-gray-200 bg-white py-1 shadow-lg animate-fade-in">
                            <button onClick={() => { toast.info(`编辑租户: ${t.tenantName}`); setMenuTarget(null); }} className="flex w-full items-center gap-2 px-3 py-1.5 text-xs text-gray-700 hover:bg-gray-50"><Edit size={13} />编辑</button>
                            <button onClick={() => { toast.info(`停用租户: ${t.tenantName}`); setMenuTarget(null); }} className="flex w-full items-center gap-2 px-3 py-1.5 text-xs text-gray-700 hover:bg-gray-50"><Power size={13} />停用</button>
                            <button onClick={() => { setDeleteTarget(t); setMenuTarget(null); }} className="flex w-full items-center gap-2 px-3 py-1.5 text-xs text-danger-500 hover:bg-danger-50"><Trash2 size={13} />删除</button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
        <Pagination page={page} total={total} size={10} onChange={setPage} />
      </TableCard>

      {/* Create Modal */}
      <Modal
        open={createOpen}
        title="新建租户"
        onClose={() => setCreateOpen(false)}
        footer={<>
          <Button variant="secondary" onClick={() => setCreateOpen(false)}>取消</Button>
          <Button variant="primary" onClick={handleCreate} loading={createMutation.isPending}>确定</Button>
        </>}
      >
        <div className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">租户编码 <span className="text-danger-500">*</span></label>
            <input value={createForm.tenantCode} onChange={(e) => setCreateForm({ ...createForm, tenantCode: e.target.value })} placeholder="如 GROUP_HQ" className="h-9 w-full rounded-md border border-gray-300 px-3 text-sm outline-none focus:border-primary-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">租户名称 <span className="text-danger-500">*</span></label>
            <input value={createForm.tenantName} onChange={(e) => setCreateForm({ ...createForm, tenantName: e.target.value })} placeholder="如 集团总部" className="h-9 w-full rounded-md border border-gray-300 px-3 text-sm outline-none focus:border-primary-500" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">租户类型</label>
            <select value={createForm.tenantType} onChange={(e) => setCreateForm({ ...createForm, tenantType: e.target.value })} className="h-9 w-full rounded-md border border-gray-300 px-3 text-sm outline-none focus:border-primary-500">
              <option value="ORGANIZATION">组织</option>
              <option value="PROJECT">项目</option>
              <option value="EXTERNAL">外部</option>
              <option value="PLATFORM">平台</option>
            </select>
          </div>
        </div>
      </Modal>

      {/* Delete Confirm */}
      <Modal
        open={!!deleteTarget}
        title="确认删除"
        onClose={() => setDeleteTarget(null)}
        width={400}
        footer={<>
          <Button variant="secondary" onClick={() => setDeleteTarget(null)}>取消</Button>
          <Button variant="danger" onClick={() => deleteTarget && deleteMutation.mutate(deleteTarget.id!)} loading={deleteMutation.isPending}>确认删除</Button>
        </>}
      >
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-danger-50"><Trash2 size={20} className="text-danger-500" /></div>
          <div>
            <p className="text-sm font-medium text-gray-800">确定要删除租户 "{deleteTarget?.tenantName}" 吗？</p>
            <p className="mt-1 text-xs text-gray-400">删除后不可恢复，该租户下所有数据将被清除。</p>
          </div>
        </div>
      </Modal>
    </div>
  );
}
