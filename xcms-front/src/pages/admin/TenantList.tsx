import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  FilterBar,
  TableCard,
  Table,
  Pagination,
  Button,
  Modal,
  StatusBadge,
  Input,
  Select,
  Form,
  FormItem,
  useForm,
  toast,
} from '@/components/ui';
import type { Column } from '@/components/ui';
import { useConfirm } from '@/common/confirm';
import { tenantApi } from '@/api/tenant';
import type { TenantDTO, TenantCreateRequest } from '@/types/tenant';

const TYPE_LABEL: Record<string, string> = {
  ORGANIZATION: '组织型',
  PROJECT: '项目型',
  EXTERNAL: '外部型',
  PLATFORM: '平台型',
};

const STATUS_LABEL: Record<string, string> = {
  ACTIVE: '启用',
  SUSPENDED: '停用',
  LOCKED: '锁定',
  MIGRATING: '迁移中',
  ARCHIVED: '已归档',
};

export default function TenantList() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [editing, setEditing] = useState<TenantDTO | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const { confirm, Confirm } = useConfirm();

  const { data, isFetching } = useQuery({
    queryKey: ['tenants', page, size, keyword],
    queryFn: () => tenantApi.list({ query: { page, size, keyword } }),
  });
  const list = (data?.list ?? []) as TenantDTO[];
  const total = data?.total ?? 0;

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };
  const openEdit = (row: TenantDTO) => {
    setEditing(row);
    setModalOpen(true);
  };

  const handleDelete = (row: TenantDTO) => {
    confirm({
      title: '确认删除',
      description: `确定要删除租户「${row.tenantName}」吗？`,
      danger: true,
      onOk: async () => {
        await tenantApi.delete(row.id!);
        toast.success('删除成功');
        queryClient.invalidateQueries({ queryKey: ['tenants'] });
      },
    });
  };

  const columns: Column<TenantDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'tenantName', title: '租户名称' },
    { key: 'tenantCode', title: '租户编码' },
    {
      key: 'tenantType',
      title: '类型',
      render: (r) => TYPE_LABEL[r.tenantType ?? ''] || r.tenantType || '-',
    },
    {
      key: 'status',
      title: '状态',
      render: (r) => (
        <StatusBadge status={r.status ?? ''} label={STATUS_LABEL[r.status ?? ''] || r.status} />
      ),
    },
    {
      key: 'actions',
      title: '操作',
      align: 'center',
      render: (r) => (
        <>
          <Button size="sm" variant="ghost" onClick={() => openEdit(r)}>
            编辑
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-danger-500"
            onClick={() => handleDelete(r)}
          >
            删除
          </Button>
        </>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="租户管理"
        description="管理集团下的租户"
        actions={<Button onClick={openCreate}>+ 新建租户</Button>}
      />
      <FilterBar>
        <Input
          placeholder="搜索租户名称/编码"
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value);
            setPage(1);
          }}
        />
      </FilterBar>
      <TableCard>
        <Table
          rowKey={(r) => r.id ?? 0}
          columns={columns}
          data={list}
          loading={isFetching}
          emptyText="暂无租户数据"
        />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>

      {modalOpen && (
        <TenantFormModal
          editing={editing}
          onClose={() => setModalOpen(false)}
          onSuccess={() => {
            setModalOpen(false);
            queryClient.invalidateQueries({ queryKey: ['tenants'] });
          }}
        />
      )}
      {Confirm}
    </div>
  );
}

type TenantFormValues = {
  tenantName: string;
  tenantCode: string;
  tenantType: string;
  status: string;
};

function TenantFormModal({
  editing,
  onClose,
  onSuccess,
}: {
  editing: TenantDTO | null;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const form = useForm<TenantFormValues>(
    {
      tenantName: editing?.tenantName ?? '',
      tenantCode: editing?.tenantCode ?? '',
      tenantType: editing?.tenantType ?? 'ORGANIZATION',
      status: editing?.status ?? 'ACTIVE',
    },
    {
      tenantName: [{ required: '请输入租户名称' }],
      tenantCode: [{ required: '请输入租户编码' }],
      tenantType: [{ required: '请选择租户类型' }],
    },
  );

  const handleSave = async () => {
    if (!form.validate()) return;
    const values = form.values;
    if (editing) {
      await tenantApi.update({
        id: editing.id!,
        tenantName: values.tenantName,
        status: values.status as Parameters<typeof tenantApi.update>[0]['status'],
      });
      toast.success('更新成功');
    } else {
      await tenantApi.create({
        tenantCode: values.tenantCode,
        tenantName: values.tenantName,
        tenantType: values.tenantType as TenantCreateRequest['tenantType'],
        parentId: 1,
      });
      toast.success('创建成功');
    }
    onSuccess();
  };

  return (
    <Modal open onClose={onClose} title={editing ? '编辑租户' : '新建租户'}>
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="租户名称" required error={form.errors.tenantName}>
          <Input
            value={form.values.tenantName}
            onChange={(e) => form.setField('tenantName', e.target.value)}
          />
        </FormItem>
        <FormItem label="租户编码" required error={form.errors.tenantCode}>
          <Input
            value={form.values.tenantCode}
            onChange={(e) => form.setField('tenantCode', e.target.value)}
          />
        </FormItem>
        <FormItem label="租户类型" required error={form.errors.tenantType}>
          <Select
            value={form.values.tenantType}
            onChange={(e) => form.setField('tenantType', e.target.value)}
          >
            <option value="ORGANIZATION">组织型</option>
            <option value="PROJECT">项目型</option>
            <option value="EXTERNAL">外部型</option>
            <option value="PLATFORM">平台型</option>
          </Select>
        </FormItem>
        <FormItem label="状态">
          <Select
            value={form.values.status}
            onChange={(e) => form.setField('status', e.target.value)}
          >
            <option value="ACTIVE">启用</option>
            <option value="SUSPENDED">停用</option>
            <option value="LOCKED">锁定</option>
          </Select>
        </FormItem>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            取消
          </Button>
          <Button type="submit">{editing ? '保存' : '创建'}</Button>
        </div>
      </Form>
    </Modal>
  );
}
