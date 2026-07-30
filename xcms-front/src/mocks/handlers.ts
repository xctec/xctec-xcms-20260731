import { http, HttpResponse } from 'msw';
import { mockTenants } from './data/tenants';
import { mockAdminMenus, mockPortalMenus } from './data/menus';
import { mockDepartments, mockPositions, mockGroups } from './data/organization';
import { mockUsers } from './data/users';
import { mockRoles, mockPermissions, mockRolePermissions, mockDataRules, mockDataRuleBindings, nextDataRuleId } from './data/roles';

const ok = <T>(data: T) => HttpResponse.json({ errorCode: '0', errorMsg: 'success', data });
let tenants = [...mockTenants];

const mockUser = {
  id: 1,
  tenantId: 1,
  username: 'admin',
  realName: '超级管理员',
  tenantName: '集团总部',
  roles: [{ roleCode: 'tenant_admin', roleName: '租户管理员' }],
};

export const handlers = [
  // ====== Auth ======
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      // AT-12：refresh token 经 httpOnly cookie 下发，body 不再返回
      return ok({
        token: 'mock-jwt-token-' + Date.now(),
        expiresIn: 7200,
        user: mockUser,
        forceChangePassword: false,
      });
    }
    return HttpResponse.json({ errorCode: '1002', errorMsg: '用户名或密码错误', data: null });
  }),

  http.post('/api/auth/logout', () => ok(null)),
  http.post('/api/auth/user-info', () => ok({
    token: 'mock-jwt-token',
    expiresIn: 7200,
    user: mockUser,
    forceChangePassword: false,
  })),

  // ====== Token 刷新 ======
  // AT-12：refresh token 在 httpOnly cookie（浏览器自动携带），mock 直接视为有效
  http.post('/api/auth/refresh', () =>
    ok({
      token: 'mock-jwt-token-' + Date.now(),
      expiresIn: 7200,
      user: mockUser,
      forceChangePassword: false,
    })
  ),

  // ====== Menu ======
  // 管理面菜单（对应后端 /admin/permission/menus）
  http.post('/api/admin/permission/menus', () => ok(mockAdminMenus)),
  // 门户菜单（对应后端 /portal/menus）
  http.post('/api/portal/menus', () => ok(mockPortalMenus)),
  http.post('/api/admin/menu/all', () => ok(mockAdminMenus)),

  // ====== Tenant ======
  http.post('/api/admin/tenant/list-children', async ({ request }) => {
    const body = (await request.json()) as {
      page?: number;
      size?: number;
      query?: { page?: number; size?: number; keyword?: string };
    };
    const page = Number(body.query?.page ?? body.page ?? 1);
    const size = Number(body.query?.size ?? body.size ?? 10);
    const start = (page - 1) * size;
    const list = tenants.slice(start, start + size);
    return ok({ list, total: tenants.length });
  }),
  http.post('/api/admin/tenant/tree', () => ok(tenants)),
  http.post('/api/admin/tenant/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    return ok(tenants.find((t) => t.id === id) || null);
  }),
  http.post('/api/admin/tenant/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const newTenant = {
      id: Date.now(),
      ...body,
      level: 2,
      path: '/1/' + Date.now(),
      status: 'ACTIVE',
      deploymentMode: 'shared',
      createdAt: '2025-07-25',
    } as never;
    tenants = [newTenant, ...tenants];
    return ok(newTenant);
  }),
  http.post('/api/admin/tenant/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const id = Number(body.id);
    tenants = tenants.map((t) =>
      t.id === id ? ({ ...t, ...body } as (typeof tenants)[number]) : t,
    );
    return ok(tenants.find((t) => t.id === id) || null);
  }),
  http.post('/api/admin/tenant/delete', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    tenants = tenants.filter((t) => t.id !== id);
    return ok(null);
  }),

  // ====== 组织架构（对齐后端 DepartmentController / PositionController / UserGroupController）======
  http.post('/api/admin/department/tree', () => ok(mockDepartments)),
  http.post('/api/admin/department/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), children: [], ...body });
  }),
  http.post('/api/admin/department/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/api/admin/department/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/department/move', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/position/list-by-dept', async ({ request }) => {
    const body = (await request.json()) as { id?: number };
    const positions = body.id != null ? mockPositions[body.id] ?? [] : [];
    return ok(positions);
  }),
  http.post('/api/admin/position/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), positionId: Date.now(), ...body });
  }),
  http.post('/api/admin/position/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/user-group/list', () => ok(mockGroups)),
  http.post('/api/admin/user-group/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), memberCount: 0, ...body });
  }),
  http.post('/api/admin/user-group/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/user-group/add-members', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/user-group/remove-members', async ({ request }) => {
    await request.json();
    return ok(null);
  }),

  // ====== 用户 ======
  http.post('/api/admin/user/list', async ({ request }) => {
    const body = (await request.json()) as { page?: number; size?: number; keyword?: string };
    const page = Number(body.page ?? 1);
    const size = Number(body.size ?? 10);
    const kw = (body.keyword || '').trim();
    const filtered = kw
      ? mockUsers.filter((u) => (u.username ?? '').includes(kw) || (u.realName || '').includes(kw))
      : mockUsers;
    const start = (page - 1) * size;
    return ok({ list: filtered.slice(start, start + size), total: filtered.length });
  }),
  http.post('/api/admin/user/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    return ok(mockUsers.find((u) => u.id === id) || null);
  }),
  http.post('/api/admin/user/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const user = { id: Date.now(), tenantId: 1, status: 'ACTIVE', ...body } as never;
    mockUsers.unshift(user);
    return ok(user);
  }),
  http.post('/api/admin/user/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/api/admin/user/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/api/admin/user/reset-password', async ({ request }) => {
    await request.json();
    return ok(null);
  }),

  // ====== 角色 / 权限 ======
  http.post('/api/admin/role/list', async ({ request }) => {
    const body = (await request.json()) as { page?: number; size?: number; keyword?: string };
    const page = Number(body.page ?? 1);
    const size = Number(body.size ?? 10);
    const kw = (body.keyword || '').trim();
    const filtered = kw
      ? mockRoles.filter((r) => (r.roleName || '').includes(kw) || (r.roleCode || '').includes(kw))
      : mockRoles;
    const start = (page - 1) * size;
    return ok({ list: filtered.slice(start, start + size), total: filtered.length });
  }),
  http.post('/api/admin/role/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const role = { id: Date.now(), status: 'ACTIVE', ...body } as never;
    mockRoles.unshift(role);
    return ok(role);
  }),
  http.post('/api/admin/role/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/api/admin/role/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  // 对齐后端 RolePermissionController：/admin/role-permission/get（IdRequest{id}）
  http.post('/api/admin/role-permission/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    const ids = mockRolePermissions[id] ?? [];
    return ok(mockPermissions.filter((p) => ids.includes(p.id!)));
  }),
  // /admin/role-permission/assign（RolePermissionAssignRequest{roleId, permissions: PermissionAssignRequest[]}）
  http.post('/api/admin/role-permission/assign', async ({ request }) => {
    const { roleId, permissions } = (await request.json()) as {
      roleId: number;
      permissions: { permId: number; permType?: string }[];
    };
    const permissionIds = (permissions ?? []).map((p) => p.permId);
    mockRolePermissions[roleId] = permissionIds || [];
    return ok(null);
  }),
  // 对齐后端 POST /admin/permission/list（列出全部权限，含操作权限 + 菜单/按钮权限）
  http.post('/api/admin/permission/list', () => ok(mockPermissions)),
  // ====== 数据规则（AT-19：对齐后端 DataRuleController /admin/data-rule/*） ======
  http.post('/api/admin/data-rule/list', async ({ request }) => {
    const { resourceType } = (await request.json()) as { resourceType?: string };
    return ok(mockDataRules.filter((r) => !resourceType || r.resourceType === resourceType));
  }),
  http.post('/api/admin/data-rule/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const rule = { id: nextDataRuleId(), status: 'ACTIVE', ...body };
    mockDataRules.push(rule as (typeof mockDataRules)[number]);
    return ok(rule);
  }),
  http.post('/api/admin/data-rule/delete', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    const idx = mockDataRules.findIndex((r) => r.id === id);
    if (idx >= 0) mockDataRules.splice(idx, 1);
    return ok(null);
  }),
  http.post('/api/admin/data-rule/bind', async ({ request }) => {
    const { id, roleId } = (await request.json()) as { id: number; roleId: number };
    mockDataRuleBindings.push({ ruleId: id, roleId });
    return ok(null);
  }),
  http.post('/api/admin/data-rule/unbind', async ({ request }) => {
    const { id, roleId } = (await request.json()) as { id: number; roleId: number };
    const idx = mockDataRuleBindings.findIndex((b) => b.ruleId === id && b.roleId === roleId);
    if (idx >= 0) mockDataRuleBindings.splice(idx, 1);
    return ok(null);
  }),
];
