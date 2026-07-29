import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  FilterBar,
  TableCard,
  Table,
  Pagination,
  Button,
  StatusBadge,
  Input,
  Select,
  toast,
  type Column,
} from '@/components/ui';
import { useConfirm } from '@/common/confirm';
import { Can } from '@/components/auth/Can';
import { auditApi } from '@/api/audit';
import type { Schemas } from '@/types/api-helpers';

type AuditLogDTO = Schemas['AuditLogDTO'];
type AuditQuery = Schemas['AuditQuery'];

export default function Audit() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [params, setParams] = useState<Partial<AuditQuery>>({});
  const queryClient = useQueryClient();
  const { confirm, Confirm } = useConfirm();

  const { data, isFetching } = useQuery({
    queryKey: ['audit-list', page, params],
    queryFn: () => auditApi.list({ ...params, page, size }),
  });
  const list = (data?.list ?? []) as AuditLogDTO[];
  const total = data?.total ?? 0;

  const handleExport = async () => {
    try {
      const blob = (await auditApi.export({ ...params })) as unknown as Blob;
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit-${Date.now()}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('导出已触发');
    } catch {
      toast.error('导出失败');
    }
  };

  const handleDelete = (row: AuditLogDTO) =>
    confirm({
      title: '确认删除',
      description: `确定要删除审计记录 #${row.id} 吗？`,
      onOk: async () => {
        toast.success('已删除');
        queryClient.invalidateQueries({ queryKey: ['audit-list'] });
      },
    });

  const columns: Column<AuditLogDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'bizModule', title: '业务模块' },
    { key: 'eventType', title: '事件类型' },
    { key: 'action', title: '操作' },
    { key: 'operatorName', title: '操作人' },
    { key: 'ip', title: 'IP' },
    {
      key: 'success',
      title: '结果',
      render: (r) => (
        <StatusBadge status={r.success ? 'SUCCESS' : 'FAILED'} label={r.success ? '成功' : '失败'} />
      ),
    },
    { key: 'errorMsg', title: '错误信息' },
    {
      key: 'actions',
      title: '操作',
      render: (r) => (
        <Can permission="audit:delete">
          <Button size="sm" variant="ghost" className="text-danger-500" onClick={() => handleDelete(r)}>
            删除
          </Button>
        </Can>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="审计日志"
        description="查看与导出系统操作审计记录"
        actions={
          <Can permission="audit:export">
            <Button onClick={handleExport}>导出</Button>
          </Can>
        }
      />
      <TableCard>
        <FilterBar>
          <Input
            placeholder="业务模块"
            value={params.bizModule ?? ''}
            onChange={(e) => {
              setParams((p) => ({ ...p, bizModule: e.target.value || undefined }));
              setPage(1);
            }}
          />
          <Input
            placeholder="事件类型"
            value={params.eventType ?? ''}
            onChange={(e) => {
              setParams((p) => ({ ...p, eventType: e.target.value || undefined }));
              setPage(1);
            }}
          />
          <Select
            value={params.success === undefined ? '' : String(params.success)}
            onChange={(e) =>
              setParams((p) => ({
                ...p,
                success: e.target.value === '' ? undefined : e.target.value === 'true',
              }))
            }
          >
            <option value="">全部结果</option>
            <option value="true">成功</option>
            <option value="false">失败</option>
          </Select>
        </FilterBar>
        <Table rowKey={(r) => r.id ?? 0} columns={columns} data={list} loading={isFetching} emptyText="暂无审计记录" />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>
      {Confirm}
    </div>
  );
}
