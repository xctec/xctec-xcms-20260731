import { http, HttpResponse } from 'msw';
import { mockTenants } from './data/tenants';
import { mockAdminMenus, mockPortalMenus } from './data/menus';
import { mockDepartments, mockPositions, mockGroups } from './data/organization';
import { mockUsers } from './data/users';
import { mockRoles, mockPermissions, mockRolePermissions, mockRoleDataScope } from './data/roles';

const ok = <T>(data: T) => HttpResponse.json({ errorCode: '0', errorMsg: 'success', data });
let tenants = [...mockTenants];

const mockUser = {
  id: 1,
  tenantId: 1,
  username: 'admin',
  realName: '超级管理员',
  tenantName: '集团总部',
  roles: [{ roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员' }],
};

export const handlers = [
  // ====== Auth ======
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      return ok({
        token: 'mock-jwt-token-' + Date.now(),
        refreshToken: 'mock-refresh-token',
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
    refreshToken: 'mock-refresh-token',
    expiresIn: 7200,
    user: mockUser,
    forceChangePassword: false,
  })),

  // ====== Token 刷新 ======
  http.post('/api/auth/refresh', async ({ request }) => {
    const body = (await request.json()) as { refreshToken?: string };
    // 演示：携带有效 refreshToken 即视为刷新成功，返回全新令牌
    if (body.refreshToken) {
      return ok({
        token: 'mock-jwt-token-' + Date.now(),
        refreshToken: 'mock-refresh-token-' + Date.now(),
        expiresIn: 7200,
        user: mockUser,
        forceChangePassword: false,
      });
    }
    return HttpResponse.json({ errorCode: '1001', errorMsg: 'refreshToken 无效', data: null }, { status: 401 });
  }),

  // ====== Menu ======
  // 管理面菜单（对应后端 /admin/permission/menus）
  http.post('/admin/permission/menus', () => ok(mockAdminMenus)),
  // 门户菜单（对应后端 /portal/menus）
  http.post('/portal/menus', () => ok(mockPortalMenus)),
  http.post('/admin/menu/all', () => ok(mockAdminMenus)),

  // ====== Tenant ======
  http.post('/admin/tenant/list-children', async ({ request }) => {
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
  http.post('/admin/tenant/tree', () => ok(tenants)),
  http.post('/admin/tenant/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    return ok(tenants.find((t) => t.id === id) || null);
  }),
  http.post('/admin/tenant/create', async ({ request }) => {
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
  http.post('/admin/tenant/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const id = Number(body.id);
    tenants = tenants.map((t) =>
      t.id === id ? ({ ...t, ...body } as (typeof tenants)[number]) : t,
    );
    return ok(tenants.find((t) => t.id === id) || null);
  }),
  http.post('/admin/tenant/delete', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    tenants = tenants.filter((t) => t.id !== id);
    return ok(null);
  }),

  // ====== 组织架构 ======
  http.post('/admin/org/dept/tree', () => ok(mockDepartments)),
  http.post('/admin/org/dept/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), children: [], ...body });
  }),
  http.post('/admin/org/dept/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/admin/org/dept/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/admin/org/dept/move', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/admin/org/position/list', async ({ request }) => {
    const body = (await request.json()) as { deptId?: number };
    const positions = body.deptId != null ? mockPositions[body.deptId] ?? [] : [];
    return ok(positions);
  }),
  http.post('/admin/org/position/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), positionId: Date.now(), ...body });
  }),
  http.post('/admin/org/position/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/admin/org/group/list', () => ok(mockGroups)),
  http.post('/admin/org/group/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), memberCount: 0, ...body });
  }),
  http.post('/admin/org/group/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),

  // ====== 用户 ======
  http.post('/admin/user/list', async ({ request }) => {
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
  http.post('/admin/user/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    return ok(mockUsers.find((u) => u.id === id) || null);
  }),
  http.post('/admin/user/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const user = { id: Date.now(), tenantId: 1, status: 'ACTIVE', ...body } as never;
    mockUsers.unshift(user);
    return ok(user);
  }),
  http.post('/admin/user/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/admin/user/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  http.post('/admin/user/reset-password', async ({ request }) => {
    await request.json();
    return ok(null);
  }),

  // ====== 角色 / 权限 ======
  http.post('/admin/role/list', async ({ request }) => {
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
  http.post('/admin/role/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    const role = { id: Date.now(), status: 'ACTIVE', ...body } as never;
    mockRoles.unshift(role);
    return ok(role);
  }),
  http.post('/admin/role/update', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ ...body });
  }),
  http.post('/admin/role/delete', async ({ request }) => {
    await request.json();
    return ok(null);
  }),
  // 对齐后端 RolePermissionController：/admin/role-permission/get（IdRequest{id}）
  http.post('/admin/role-permission/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    const ids = mockRolePermissions[id] ?? [];
    return ok(mockPermissions.filter((p) => ids.includes(p.id!)));
  }),
  // /admin/role-permission/assign（RolePermissionAssignRequest{roleId, permissions: PermissionAssignRequest[]}）
  http.post('/admin/role-permission/assign', async ({ request }) => {
    const { roleId, permissions } = (await request.json()) as {
      roleId: number;
      permissions: { permId: number }[];
    };
    const permissionIds = (permissions ?? []).map((p) => p.permId);
    mockRolePermissions[roleId] = permissionIds || [];
    return ok(null);
  }),
  // TODO(后端待补): listPermissions 依赖后端「列举全部权限」接口，当前仅 mock 支撑开发联调
  http.post('/admin/authz/permissions', () => ok(mockPermissions)),
  http.post('/admin/authz/data-scope', async ({ request }) => {
    const { roleId } = (await request.json()) as { roleId: number };
    return ok(mockRoleDataScope[roleId] ?? { scopeType: 'ALL', scopeValues: [] });
  }),
  http.post('/admin/authz/update-scope', async ({ request }) => {
    const { roleId, scopeType, scopeValues } = (await request.json()) as {
      roleId: number;
      scopeType: string;
      scopeValues: string[];
    };
    mockRoleDataScope[roleId] = { scopeType, scopeValues: scopeValues || [] };
    return ok(null);
  }),
];
