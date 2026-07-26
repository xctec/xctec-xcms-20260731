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
} from '@/components/ui';
import type { Column } from '@/components/ui';
import { useConfirm } from '@/common/confirm';
import { Can } from '@/components/auth/Can';
import { toast } from '@/components/ui';
import { userApi } from '@/api/identity';
import type { UserDTO } from '@/types/user';

const STATUS_LABEL: Record<string, string> = {
  ACTIVE: '启用',
  ENABLED: '正常',
  DISABLED: '禁用',
  LOCKED: '锁定',
};

export default function UserList() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [editing, setEditing] = useState<UserDTO | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const queryClient = useQueryClient();
  const { confirm, Confirm } = useConfirm();

  const { data, isFetching } = useQuery({
    queryKey: ['user-list', page, keyword],
    queryFn: () => userApi.list({ page, size, keyword: keyword || undefined }),
  });
  const list = (data?.list ?? []) as UserDTO[];
  const total = data?.total ?? 0;

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };
  const openEdit = (row: UserDTO) => {
    setEditing(row);
    setModalOpen(true);
  };

  const handleDelete = (row: UserDTO) => {
    confirm({
      title: '确认删除',
      description: `确定要删除用户「${row.realName || row.username}」吗？`,
      onOk: async () => {
        await userApi.delete(row.id!);
        toast.success('删除成功');
        queryClient.invalidateQueries({ queryKey: ['user-list'] });
      },
    });
  };

  const handleReset = (row: UserDTO) => {
    confirm({
      title: '重置密码',
      description: `确定要将「${row.realName || row.username}」的密码重置为 123456 吗？`,
      onOk: async () => {
        await userApi.resetPassword(row.id!);
        toast.success('密码已重置为 123456');
      },
    });
  };

  const columns: Column<UserDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'username', title: '用户名' },
    { key: 'realName', title: '姓名' },
    { key: 'employeeNo', title: '工号' },
    { key: 'email', title: '邮箱' },
    { key: 'phone', title: '手机号' },
    {
      key: 'roles',
      title: '角色',
      render: (r) => (r.roles || []).map((role) => role.roleName).join('、') || '-',
    },
    {
      key: 'status',
      title: '状态',
      render: (r) => <StatusBadge status={r.status ?? ''} label={STATUS_LABEL[r.status ?? ''] || r.status} />,
    },
    {
      key: 'actions',
      title: '操作',
      align: 'center',
      render: (r) => (
        <>
          <Can permission="user:edit">
            <Button size="sm" variant="ghost" onClick={() => openEdit(r)}>
              编辑
            </Button>
          </Can>
          <Can permission="user:reset-pwd">
            <Button size="sm" variant="ghost" onClick={() => handleReset(r)}>
              重置密码
            </Button>
          </Can>
          <Can permission="user:delete">
            <Button
              size="sm"
              variant="ghost"
              className="text-danger-500"
              onClick={() => handleDelete(r)}
            >
              删除
            </Button>
          </Can>
        </>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="用户管理"
        description="管理系统用户"
        actions={
          <Can permission="user:create">
            <Button onClick={openCreate}>+ 新建用户</Button>
          </Can>
        }
      />
      <TableCard>
        <FilterBar>
          <Input
            placeholder="搜索用户名/姓名"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value);
              setPage(1);
            }}
          />
        </FilterBar>
        <Table
          rowKey={(r) => r.id ?? 0}
          columns={columns}
          data={list}
          loading={isFetching}
          emptyText="暂无用户数据"
        />
        <div className="flex justify-end pt-3">
          <Pagination page={page} size={size} total={total} onChange={setPage} />
        </div>
      </TableCard>

      {modalOpen && (
        <UserFormModal
          editing={editing}
          onClose={() => setModalOpen(false)}
          onSuccess={() => {
            setModalOpen(false);
            queryClient.invalidateQueries({ queryKey: ['user-list'] });
          }}
        />
      )}
      {Confirm}
    </div>
  );
}

type UserFormValues = {
  username: string;
  realName: string;
  employeeNo: string;
  email: string;
  phone: string;
  status: string;
  password: string;
};

function UserFormModal({
  editing,
  onClose,
  onSuccess,
}: {
  editing: UserDTO | null;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const form = useForm<UserFormValues>(
    {
      username: editing?.username ?? '',
      realName: editing?.realName ?? '',
      employeeNo: editing?.employeeNo ?? '',
      email: editing?.email ?? '',
      phone: editing?.phone ?? '',
      status: editing?.status ?? 'ACTIVE',
      password: '',
    },
    {
      username: [{ required: '请输入用户名' }],
      realName: [{ required: '请输入姓名' }],
      ...(editing ? {} : { password: [{ required: '请输入初始密码' }] }),
    },
  );

  const handleSave = async () => {
    if (!form.validate()) return;
    const values = form.values;
    if (editing) {
      await userApi.update({ id: editing.id!, ...values } as Parameters<typeof userApi.update>[0]);
      toast.success('更新成功');
    } else {
      await userApi.create(values as Parameters<typeof userApi.create>[0]);
      toast.success('创建成功');
    }
    onSuccess();
  };

  return (
    <Modal open onClose={onClose} title={editing ? '编辑用户' : '新建用户'}>
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="用户名" required error={form.errors.username}>
          <Input
            value={form.values.username}
            onChange={(e) => form.setField('username', e.target.value)}
          />
        </FormItem>
        <FormItem label="姓名" required error={form.errors.realName}>
          <Input
            value={form.values.realName}
            onChange={(e) => form.setField('realName', e.target.value)}
          />
        </FormItem>
        <FormItem label="工号">
          <Input
            value={form.values.employeeNo}
            onChange={(e) => form.setField('employeeNo', e.target.value)}
          />
        </FormItem>
        <FormItem label="邮箱">
          <Input value={form.values.email} onChange={(e) => form.setField('email', e.target.value)} />
        </FormItem>
        <FormItem label="手机号">
          <Input value={form.values.phone} onChange={(e) => form.setField('phone', e.target.value)} />
        </FormItem>
        <FormItem label="状态">
          <Select
            value={form.values.status}
            onChange={(e) => form.setField('status', e.target.value)}
          >
            <option value="ACTIVE">启用</option>
            <option value="ENABLED">正常</option>
            <option value="DISABLED">禁用</option>
          </Select>
        </FormItem>
        {!editing && (
          <FormItem label="初始密码" required error={form.errors.password}>
            <Input
              type="password"
              value={form.values.password}
              onChange={(e) => form.setField('password', e.target.value)}
            />
          </FormItem>
        )}
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
