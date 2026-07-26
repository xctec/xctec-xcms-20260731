import { useState, useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  PageHeader,
  FilterBar,
  TableCard,
  Table,
  Pagination,
  Button,
  Modal,
  Input,
  RadioGroup,
  Checkbox,
} from '@/components/ui';
import type { Column } from '@/components/ui';
import { toast } from '@/components/ui';
import { roleApi } from '@/api/identity';
import { authzApi } from '@/api/authorization';
import { Can } from '@/components/auth/Can';
import type { PermissionDTO, RoleDTO } from '@/types/authorization';

const SCOPE_OPTIONS = [
  { label: '全部数据', value: 'ALL' },
  { label: '仅本人', value: 'SELF' },
  { label: '本部门', value: 'CURRENT_DEPT' },
  { label: '本部门及子部门', value: 'DEPT_AND_CHILD' },
  { label: '自定义', value: 'CUSTOM' },
];

export default function Permission() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [selectedRole, setSelectedRole] = useState<number | null>(null);
  const [assignOpen, setAssignOpen] = useState(false);
  const [scopeType, setScopeType] = useState('ALL');
  const queryClient = useQueryClient();

  const { data, isFetching } = useQuery({
    queryKey: ['role-list', page, keyword],
    queryFn: () => roleApi.list({ page, size, keyword: keyword || undefined }),
  });
  const roles = (data?.list ?? []) as RoleDTO[];
  const total = data?.total ?? 0;

  const { data: permissions = [], isFetching: permLoading } = useQuery({
    queryKey: ['role-permissions', selectedRole],
    queryFn: () => authzApi.getRolePermissions(selectedRole!),
    enabled: selectedRole !== null,
  });

  const { data: scope } = useQuery({
    queryKey: ['role-scope', selectedRole],
    queryFn: () => authzApi.getDataScope(selectedRole!),
    enabled: selectedRole !== null,
  });

  useEffect(() => {
    if (scope?.scopeType) setScopeType(scope.scopeType);
  }, [scope]);

  const roleColumns: Column<RoleDTO>[] = [
    { key: 'id', title: 'ID', width: 64 },
    { key: 'roleName', title: '角色名称' },
    { key: 'roleCode', title: '角色编码' },
    { key: 'roleType', title: '类型' },
    { key: 'description', title: '描述' },
    { key: 'status', title: '状态', render: (r) => r.status },
  ];

  const permColumns: Column<PermissionDTO>[] = [
    { key: 'permCode', title: '权限编码' },
    { key: 'permName', title: '权限名称' },
    { key: 'module', title: '模块' },
    { key: 'permType', title: '类型', render: (p) => p.permType },
    { key: 'action', title: '操作', render: (p) => p.action },
  ];

  const handleSaveScope = async () => {
    if (selectedRole == null) return;
    await authzApi.updateDataScope(selectedRole, scopeType, []);
    toast.success('数据范围已保存');
  };

  return (
    <div className="space-y-4">
      <PageHeader title="权限管理" description="管理角色及其数据权限范围" />
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_1fr]">
        <TableCard>
          <FilterBar>
            <Input
              placeholder="搜索角色"
              value={keyword}
              onChange={(e) => {
                setKeyword(e.target.value);
                setPage(1);
              }}
            />
          </FilterBar>
          <Table
            rowKey={(r) => r.id ?? 0}
            columns={roleColumns}
            data={roles}
            loading={isFetching}
            emptyText="暂无角色"
            selectable
            selectedKeys={selectedRole != null ? [String(selectedRole)] : []}
            onSelectionChange={(keys) => setSelectedRole(keys.length ? Number(keys[0]) : null)}
          />
          <div className="flex justify-end pt-3">
            <Pagination page={page} size={size} total={total} onChange={setPage} />
          </div>
        </TableCard>

        <div className="space-y-4">
          <TableCard>
            <div className="mb-3 flex justify-end">
              <Can permission="role:permission">
                <Button disabled={selectedRole == null} onClick={() => setAssignOpen(true)}>
                  分配权限
                </Button>
              </Can>
            </div>
            {selectedRole == null ? (
              <div className="py-8 text-center text-sm text-gray-400">请选择左侧角色</div>
            ) : (
              <Table
                rowKey={(p) => p.id ?? 0}
                columns={permColumns}
                data={permissions}
                loading={permLoading}
                emptyText="该角色暂无权限"
              />
            )}
          </TableCard>

          <TableCard>
            {selectedRole == null ? (
              <div className="py-8 text-center text-sm text-gray-400">请选择左侧角色</div>
            ) : (
              <div className="space-y-4">
                <RadioGroup
                  options={SCOPE_OPTIONS}
                  value={scopeType}
                  onChange={(v) => setScopeType(String(v))}
                />
                <div className="flex justify-end">
                  <Can permission="role:data-scope">
                    <Button onClick={handleSaveScope}>保存数据范围</Button>
                  </Can>
                </div>
              </div>
            )}
          </TableCard>
        </div>
      </div>

      {assignOpen && selectedRole != null && (
        <AssignPermissionModal
          roleId={selectedRole}
          current={permissions.map((p) => p.id!)}
          onClose={() => setAssignOpen(false)}
          onSuccess={() => {
            setAssignOpen(false);
            queryClient.invalidateQueries({ queryKey: ['role-permissions', selectedRole] });
          }}
        />
      )}
    </div>
  );
}

function AssignPermissionModal({
  roleId,
  current,
  onClose,
  onSuccess,
}: {
  roleId: number;
  current: number[];
  onClose: () => void;
  onSuccess: () => void;
}) {
  const { data: all = [] } = useQuery({
    queryKey: ['all-permissions'],
    queryFn: () => authzApi.listPermissions(),
  });
  const [checked, setChecked] = useState<number[]>(current);

  const toggle = (id: number) => {
    setChecked((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const handleSave = async () => {
    await authzApi.assignPermissions(roleId, checked);
    toast.success('权限已分配');
    onSuccess();
  };

  return (
    <Modal open onClose={onClose} title="分配权限">
      <div className="max-h-[60vh] space-y-2 overflow-auto">
        {all.map((p) => (
          <label key={p.id} className="flex items-center gap-2 text-sm">
            <Checkbox checked={checked.includes(p.id!)} onChange={() => toggle(p.id!)} />
            <span>{p.permName}</span>
            <span className="text-gray-400">（{p.permCode}）</span>
          </label>
        ))}
      </div>
      <div className="flex justify-end gap-2 pt-3">
        <Button type="button" variant="ghost" onClick={onClose}>
          取消
        </Button>
        <Can permission="role:permission">
          <Button onClick={handleSave}>保存</Button>
        </Can>
      </div>
    </Modal>
  );
}
