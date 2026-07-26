import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  TableCard,
  Table,
  Button,
  Modal,
  Tree,
  Input,
  Select,
  Form,
  FormItem,
  useForm,
} from '@/components/ui';
import type { Column, TreeNode } from '@/components/ui';
import { useConfirm } from '@/common/confirm';
import { toast } from '@/components/ui';
import { orgApi } from '@/api/organization';
import type { DepartmentTreeDTO, UserPositionDTO, UserGroupDTO } from '@/types/organization';

function toTree(nodes: DepartmentTreeDTO[]): TreeNode[] {
  return nodes.map((n) => ({
    key: String(n.id),
    title: n.deptName ?? '',
    children: n.children && n.children.length ? toTree(n.children) : undefined,
  }));
}

export default function Organization() {
  const [selectedDept, setSelectedDept] = useState<number | null>(null);
  const [deptModal, setDeptModal] = useState(false);
  const [positionModal, setPositionModal] = useState(false);
  const [groupModal, setGroupModal] = useState(false);
  const queryClient = useQueryClient();
  const { confirm, Confirm } = useConfirm();

  const { data: tree = [] } = useQuery({
    queryKey: ['dept-tree'],
    queryFn: async () => (await orgApi.getDeptTree()) as DepartmentTreeDTO[],
  });
  const { data: positions = [], isFetching: posLoading } = useQuery({
    queryKey: ['positions', selectedDept],
    queryFn: async () => (await orgApi.getPositions(selectedDept!)) as UserPositionDTO[],
    enabled: selectedDept !== null,
  });
  const { data: groups = [] } = useQuery({
    queryKey: ['groups'],
    queryFn: async () => (await orgApi.getUserGroups()) as UserGroupDTO[],
  });

  const handleDeletePosition = (row: UserPositionDTO) => {
    confirm({
      title: '删除岗位',
      description: `确定删除岗位「${row.positionName}」吗？`,
      onOk: async () => {
        await orgApi.deletePosition(row.id!);
        toast.success('已删除');
        queryClient.invalidateQueries({ queryKey: ['positions', selectedDept] });
      },
    });
  };
  const handleDeleteGroup = (row: UserGroupDTO) => {
    confirm({
      title: '删除用户组',
      description: `确定删除用户组「${row.groupName}」吗？`,
      onOk: async () => {
        await orgApi.deleteGroup(row.id!);
        toast.success('已删除');
        queryClient.invalidateQueries({ queryKey: ['groups'] });
      },
    });
  };

  const positionColumns: Column<UserPositionDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'deptName', title: '部门' },
    { key: 'positionName', title: '岗位名称' },
    {
      key: 'actions',
      title: '操作',
      align: 'center',
      render: (r) => (
        <Button
          size="sm"
          variant="ghost"
          className="text-danger-500"
          onClick={() => handleDeletePosition(r)}
        >
          删除
        </Button>
      ),
    },
  ];
  const groupColumns: Column<UserGroupDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'groupName', title: '组名称' },
    { key: 'description', title: '描述' },
    { key: 'type', title: '类型' },
    { key: 'memberCount', title: '成员数' },
    {
      key: 'actions',
      title: '操作',
      align: 'center',
      render: (r) => (
        <Button
          size="sm"
          variant="ghost"
          className="text-danger-500"
          onClick={() => handleDeleteGroup(r)}
        >
          删除
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <PageHeader
        title="组织架构"
        description="管理部门、岗位与用户组"
        actions={<Button onClick={() => setDeptModal(true)}>+ 新建部门</Button>}
      />
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[280px_1fr]">
      <TableCard>
        <Tree
          data={toTree(tree)}
          selectedKey={selectedDept != null ? String(selectedDept) : undefined}
          defaultExpandAll
          onSelect={(key) => setSelectedDept(Number(key))}
        />
      </TableCard>

        <div className="space-y-4">
          <TableCard>
            <div className="mb-3 flex items-center justify-between">
              <span className="text-sm font-medium text-gray-700">岗位</span>
              <Button disabled={selectedDept == null} onClick={() => setPositionModal(true)}>
                + 新建岗位
              </Button>
            </div>
            {selectedDept == null ? (
              <div className="py-8 text-center text-sm text-gray-400">请选择左侧部门</div>
            ) : (
              <Table
                rowKey={(r) => r.id ?? 0}
                columns={positionColumns}
                data={positions}
                loading={posLoading}
                emptyText="暂无岗位"
              />
            )}
          </TableCard>

          <TableCard>
            <div className="mb-3 flex items-center justify-between">
              <span className="text-sm font-medium text-gray-700">用户组</span>
              <Button onClick={() => setGroupModal(true)}>+ 新建用户组</Button>
            </div>
            <Table
              rowKey={(r) => r.id ?? 0}
              columns={groupColumns}
              data={groups}
              emptyText="暂无用户组"
            />
          </TableCard>
        </div>
      </div>

      {deptModal && (
        <DeptFormModal
          onClose={() => setDeptModal(false)}
          onSuccess={() => {
            setDeptModal(false);
            queryClient.invalidateQueries({ queryKey: ['dept-tree'] });
          }}
        />
      )}
      {positionModal && selectedDept != null && (
        <PositionFormModal
          deptId={selectedDept}
          onClose={() => setPositionModal(false)}
          onSuccess={() => {
            setPositionModal(false);
            queryClient.invalidateQueries({ queryKey: ['positions', selectedDept] });
          }}
        />
      )}
      {groupModal && (
        <GroupFormModal
          onClose={() => setGroupModal(false)}
          onSuccess={() => {
            setGroupModal(false);
            queryClient.invalidateQueries({ queryKey: ['groups'] });
          }}
        />
      )}
      {Confirm}
    </div>
  );
}

type DeptFormValues = { deptName: string; deptCode: string; parentId: string; sortOrder: string };
function DeptFormModal({ onClose, onSuccess }: { onClose: () => void; onSuccess: () => void }) {
  const form = useForm<DeptFormValues>(
    { deptName: '', deptCode: '', parentId: '0', sortOrder: '0' },
    {
      deptName: [{ required: '请输入部门名称' }],
      deptCode: [{ required: '请输入部门编码' }],
    },
  );
  const handleSave = async () => {
    if (!form.validate()) return;
    await orgApi.createDept({
      deptName: form.values.deptName,
      deptCode: form.values.deptCode,
      parentId: Number(form.values.parentId),
      sortOrder: Number(form.values.sortOrder),
    });
    toast.success('部门已创建');
    onSuccess();
  };
  return (
    <Modal open onClose={onClose} title="新建部门">
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="部门名称" required error={form.errors.deptName}>
          <Input value={form.values.deptName} onChange={(e) => form.setField('deptName', e.target.value)} />
        </FormItem>
        <FormItem label="部门编码" required error={form.errors.deptCode}>
          <Input value={form.values.deptCode} onChange={(e) => form.setField('deptCode', e.target.value)} />
        </FormItem>
        <FormItem label="上级部门">
          <Select value={form.values.parentId} onChange={(e) => form.setField('parentId', e.target.value)}>
            <option value="0">根部门</option>
          </Select>
        </FormItem>
        <FormItem label="排序">
          <Input value={form.values.sortOrder} onChange={(e) => form.setField('sortOrder', e.target.value)} />
        </FormItem>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            取消
          </Button>
          <Button type="submit">创建</Button>
        </div>
      </Form>
    </Modal>
  );
}

type PositionFormValues = { positionName: string; positionCode: string; level: string; sortOrder: string };
function PositionFormModal({
  deptId,
  onClose,
  onSuccess,
}: {
  deptId: number;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const form = useForm<PositionFormValues>(
    { positionName: '', positionCode: '', level: '1', sortOrder: '0' },
    {
      positionName: [{ required: '请输入岗位名称' }],
      positionCode: [{ required: '请输入岗位编码' }],
    },
  );
  const handleSave = async () => {
    if (!form.validate()) return;
    await orgApi.createPosition({
      deptId,
      positionName: form.values.positionName,
      positionCode: form.values.positionCode,
      level: Number(form.values.level),
      sortOrder: Number(form.values.sortOrder),
    });
    toast.success('岗位已创建');
    onSuccess();
  };
  return (
    <Modal open onClose={onClose} title="新建岗位">
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="岗位名称" required error={form.errors.positionName}>
          <Input
            value={form.values.positionName}
            onChange={(e) => form.setField('positionName', e.target.value)}
          />
        </FormItem>
        <FormItem label="岗位编码" required error={form.errors.positionCode}>
          <Input
            value={form.values.positionCode}
            onChange={(e) => form.setField('positionCode', e.target.value)}
          />
        </FormItem>
        <FormItem label="职级">
          <Input value={form.values.level} onChange={(e) => form.setField('level', e.target.value)} />
        </FormItem>
        <FormItem label="排序">
          <Input
            value={form.values.sortOrder}
            onChange={(e) => form.setField('sortOrder', e.target.value)}
          />
        </FormItem>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            取消
          </Button>
          <Button type="submit">创建</Button>
        </div>
      </Form>
    </Modal>
  );
}

type GroupFormValues = { groupName: string; description: string; type: string };
function GroupFormModal({ onClose, onSuccess }: { onClose: () => void; onSuccess: () => void }) {
  const form = useForm<GroupFormValues>(
    { groupName: '', description: '', type: 'SYSTEM' },
    { groupName: [{ required: '请输入组名称' }] },
  );
  const handleSave = async () => {
    if (!form.validate()) return;
    await orgApi.createUserGroup({
      groupName: form.values.groupName,
      description: form.values.description,
      type: form.values.type,
    });
    toast.success('用户组已创建');
    onSuccess();
  };
  return (
    <Modal open onClose={onClose} title="新建用户组">
      <Form onSubmit={handleSave} className="space-y-4">
        <FormItem label="组名称" required error={form.errors.groupName}>
          <Input value={form.values.groupName} onChange={(e) => form.setField('groupName', e.target.value)} />
        </FormItem>
        <FormItem label="描述">
          <Input
            value={form.values.description}
            onChange={(e) => form.setField('description', e.target.value)}
          />
        </FormItem>
        <FormItem label="类型">
          <Select value={form.values.type} onChange={(e) => form.setField('type', e.target.value)}>
            <option value="SYSTEM">系统</option>
            <option value="CUSTOM">自定义</option>
          </Select>
        </FormItem>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            取消
          </Button>
          <Button type="submit">创建</Button>
        </div>
      </Form>
    </Modal>
  );
}
