import { http, HttpResponse } from 'msw';
import { mockTenants } from './data/tenants';
import { mockAdminMenus, mockPortalMenus } from './data/menus';

const ok = <T>(data: T) => HttpResponse.json({ errorCode: '0', errorMsg: 'success', data });
let tenants = [...mockTenants];

export const handlers = [
  // ====== Auth ======
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      return ok({
        token: 'mock-jwt-token-' + Date.now(),
        refreshToken: 'mock-refresh-token',
        expiresIn: 7200,
        user: {
          id: 1,
          tenantId: 1,
          username: 'admin',
          realName: '超级管理员',
          tenantName: '集团总部',
          roles: [{ roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员' }],
        },
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
    user: {
      id: 1,
      tenantId: 1,
      username: 'admin',
      realName: '超级管理员',
      tenantName: '集团总部',
      roles: [{ roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员' }],
    },
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
        user: {
          id: 1,
          tenantId: 1,
          username: 'admin',
          realName: '超级管理员',
          tenantName: '集团总部',
          roles: [{ roleCode: 'SYSTEM_ADMIN', roleName: '系统管理员' }],
        },
        forceChangePassword: false,
      });
    }
    return HttpResponse.json({ errorCode: '1001', errorMsg: 'refreshToken 无效', data: null }, { status: 401 });
  }),

  // ====== Menu ======
  http.post('/api/menu/user-menus', async ({ request }) => {
    const body = (await request.json().catch(() => ({}))) as { face?: string };
    return ok(body.face === 'portal' ? mockPortalMenus : mockAdminMenus);
  }),
  http.post('/admin/menu/all', () => ok(mockAdminMenus)),

  // ====== Tenant ======
  http.post('/admin/tenant/list-children', async ({ request }) => {
    const body = (await request.json()) as { page: number; size: number };
    const start = (body.page - 1) * body.size;
    const list = tenants.slice(start, start + body.size);
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
      id: Date.now(), ...body, level: 2, path: '/1/' + Date.now(),
      status: 'ACTIVE', deploymentMode: 'shared', createdAt: '2025-07-25',
    } as never;
    tenants = [newTenant, ...tenants];
    return ok(newTenant);
  }),

  http.post('/admin/tenant/delete', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    tenants = tenants.filter((t) => t.id !== id);
    return ok(null);
  }),
];
