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
import { Select } from '@/components/ui';
import { roleApi } from '@/api/identity';
import { authzApi, dataRuleApi } from '@/api/authorization';
import { orgApi } from '@/api/organization';
import { Can } from '@/components/auth/Can';
import type { PermissionDTO, RoleDTO } from '@/types/authorization';
import type { Schemas } from '@/types/api-helpers';

const SCOPE_OPTIONS = [
  { label: '全部数据', value: 'ALL' },
  { label: '仅本人', value: 'SELF' },
  { label: '本部门', value: 'CURRENT_DEPT' },
  { label: '本部门及子部门', value: 'DEPT_AND_CHILD' },
  { label: '自定义', value: 'CUSTOM' },
];

/**
 * AT-19：数据范围预设 → data-rule 规则映射（前端约定，后端零改动）。
 * 预设规则命名 `preset:role-{roleId}:{preset}[:dept-{deptId}]`，资源类型统一 'default'：
 * - ALL            → 不创建规则（无规则即全量）
 * - SELF           → OWNER 维度规则 + 绑定角色
 * - CURRENT_DEPT / DEPT_AND_CHILD → ORG 维度规则（ruleConfig = 部门 path 前缀）+ 绑定角色
 * - CUSTOM         → 引导到数据规则管理配置，不在此页保存
 */
const SCOPE_RESOURCE = 'default';
const presetPrefix = (roleId: number) => `preset:role-${roleId}:`;

type DeptTreeNode = Schemas['DepartmentTreeDTO'];

/** 部门树拍平为下拉选项（带层级缩进） */
function flattenDeptTree(nodes: DeptTreeNode[] | undefined, depth = 0): { label: string; value: number }[] {
  if (!nodes) return [];
  return nodes.flatMap((n) => [
    { label: `${'　'.repeat(depth)}${n.deptName ?? n.id}`, value: n.id! },
    ...flattenDeptTree(n.children, depth + 1),
  ]);
}

export default function Permission() {
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [selectedRole, setSelectedRole] = useState<number | null>(null);
  const [assignOpen, setAssignOpen] = useState(false);
  const [scopeType, setScopeType] = useState('ALL');
  const [scopeDeptId, setScopeDeptId] = useState<number | null>(null);
  const [scopeSaving, setScopeSaving] = useState(false);
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

  // AT-19：从真实的 data-rule 端点读取规则，按命名约定回显该角色的预设范围
  const { data: scopeRules = [] } = useQuery({
    queryKey: ['role-scope-rules', selectedRole],
    queryFn: () => dataRuleApi.list(SCOPE_RESOURCE),
    enabled: selectedRole !== null,
  });

  const { data: deptTree = [] } = useQuery({
    queryKey: ['dept-tree'],
    queryFn: () => orgApi.getDeptTree(),
  });
  const deptOptions = flattenDeptTree(deptTree);

  useEffect(() => {
    if (selectedRole == null) return;
    const prefix = presetPrefix(selectedRole);
    const rule = scopeRules.find((r) => r.ruleName?.startsWith(prefix));
    if (!rule) {
      setScopeType('ALL');
      setScopeDeptId(null);
      return;
    }
    // ruleName 形如 preset:role-1:CURRENT_DEPT:dept-5
    const parts = rule.ruleName!.slice(prefix.length).split(':');
    setScopeType(parts[0] || 'ALL');
    const deptPart = parts.find((p) => p.startsWith('dept-'));
    setScopeDeptId(deptPart ? Number(deptPart.slice(5)) : null);
  }, [scopeRules, selectedRole]);

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
    if (scopeType === 'CUSTOM') {
      toast.error('自定义范围请在数据规则管理中直接配置规则并绑定角色');
      return;
    }
    const needDept = scopeType === 'CURRENT_DEPT' || scopeType === 'DEPT_AND_CHILD';
    if (needDept && scopeDeptId == null) {
      toast.error('请选择部门');
      return;
    }
    setScopeSaving(true);
    try {
      // 1. 清理该角色的旧预设规则（解绑 + 删除）
      const prefix = presetPrefix(selectedRole);
      const oldRules = (await dataRuleApi.list(SCOPE_RESOURCE)).filter((r) =>
        r.ruleName?.startsWith(prefix)
      );
      for (const rule of oldRules) {
        await dataRuleApi.unbind(rule.id!, selectedRole);
        await dataRuleApi.delete(rule.id!);
      }
      // 2. 按预设创建新规则并绑定（ALL 无需规则）
      if (scopeType === 'SELF') {
        const rule = await dataRuleApi.create({
          ruleName: `${prefix}SELF`,
          ruleType: 'PRESET',
          resourceType: SCOPE_RESOURCE,
          dimension: 'OWNER',
        });
        await dataRuleApi.bind(rule.id!, selectedRole);
      } else if (needDept) {
        const dept = await orgApi.getDept(scopeDeptId!);
        const rule = await dataRuleApi.create({
          ruleName: `${prefix}${scopeType}:dept-${scopeDeptId}`,
          ruleType: 'PRESET',
          resourceType: SCOPE_RESOURCE,
          dimension: 'ORG',
          ruleConfig: dept.path ?? `/${scopeDeptId}/`,
        });
        await dataRuleApi.bind(rule.id!, selectedRole);
      }
      queryClient.invalidateQueries({ queryKey: ['role-scope-rules'] });
      toast.success('数据范围已保存');
    } finally {
      setScopeSaving(false);
    }
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
                {(scopeType === 'CURRENT_DEPT' || scopeType === 'DEPT_AND_CHILD') && (
                  <Select
                    value={scopeDeptId ?? ''}
                    onChange={(e) => setScopeDeptId(e.target.value ? Number(e.target.value) : null)}
                    options={[{ label: '请选择部门', value: '' }, ...deptOptions]}
                  />
                )}
                {scopeType === 'CUSTOM' && (
                  <div className="rounded-md bg-gray-50 px-3 py-2 text-xs text-gray-500">
                    自定义范围请在数据规则管理中配置规则（维度/配置）并绑定本角色。
                  </div>
                )}
                <div className="flex justify-end">
                  <Can permission="role:data-scope">
                    <Button onClick={handleSaveScope} disabled={scopeSaving}>
                      {scopeSaving ? '保存中…' : '保存数据范围'}
                    </Button>
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
          current={permissions}
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

// 跨表权限 id 可能重复，用 `permType:id` 复合 key 唯一标识一条权限，避免选择/渲染冲突。
const permKey = (p: PermissionDTO) => `${p.permType ?? ''}:${p.id}`;

function AssignPermissionModal({
  roleId,
  current,
  onClose,
  onSuccess,
}: {
  roleId: number;
  current: PermissionDTO[];
  onClose: () => void;
  onSuccess: () => void;
}) {
  const { data: all = [] } = useQuery({
    queryKey: ['all-permissions'],
    queryFn: () => authzApi.listPermissions(),
  });
  const [checked, setChecked] = useState<string[]>(current.map(permKey));

  const toggle = (key: string) => {
    setChecked((prev) => (prev.includes(key) ? prev.filter((x) => x !== key) : [...prev, key]));
  };

  const handleSave = async () => {
    const items = all
      .filter((p) => checked.includes(permKey(p)))
      .map((p) => ({ permId: p.id!, permType: p.permType }));
    await authzApi.assignPermissions(roleId, items);
    toast.success('权限已分配');
    onSuccess();
  };

  return (
    <Modal open onClose={onClose} title="分配权限">
      <div className="max-h-[60vh] space-y-2 overflow-auto">
        {all.map((p) => (
          <label key={permKey(p)} className="flex items-center gap-2 text-sm">
            <Checkbox checked={checked.includes(permKey(p))} onChange={() => toggle(permKey(p))} />
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
