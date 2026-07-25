import { http, HttpResponse } from 'msw';
import { mockTenants } from './data/tenants';
import { mockMenus } from './data/menus';

const ok = <T>(data: T) => HttpResponse.json({ errorCode: '0', errorMsg: 'success', data });

export const handlers = [
  // ====== Auth ======
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      return ok({
        token: 'mock-jwt-token-' + Date.now(),
        refreshToken: 'mock-refresh-token',
        userId: 1,
        username: 'admin',
        realName: '超级管理员',
        tenantId: 1,
        tenantName: '集团总部',
        roles: ['SYSTEM_ADMIN'],
        expiresIn: 7200,
      });
    }
    return HttpResponse.json({ errorCode: '1002', errorMsg: '用户名或密码错误', data: null });
  }),

  http.post('/api/auth/logout', () => ok(null)),

  http.post('/api/auth/user-info', () =>
    ok({
      token: 'mock-jwt-token',
      refreshToken: 'mock-refresh-token',
      userId: 1,
      username: 'admin',
      realName: '超级管理员',
      tenantId: 1,
      tenantName: '集团总部',
      roles: ['SYSTEM_ADMIN'],
      expiresIn: 7200,
    })
  ),

  // ====== Menu ======
  http.post('/api/menu/user-menus', () => ok(mockMenus)),
  http.post('/admin/menu/all', () => ok(mockMenus)),

  // ====== Tenant ======
  http.post('/admin/tenant/list-children', async ({ request }) => {
    const body = (await request.json()) as { page: number; size: number };
    const start = (body.page - 1) * body.size;
    const list = mockTenants.slice(start, start + body.size);
    return ok({ list, total: mockTenants.length });
  }),

  http.post('/admin/tenant/tree', () => ok(mockTenants)),

  http.post('/admin/tenant/get', async ({ request }) => {
    const { id } = (await request.json()) as { id: number };
    const tenant = mockTenants.find((t) => t.id === id);
    return ok(tenant || null);
  }),

  http.post('/admin/tenant/create', async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return ok({ id: Date.now(), ...body, level: 2, path: '/1/' + Date.now(), status: 'ACTIVE', createdAt: '2025-07-25' });
  }),

  http.post('/admin/tenant/delete', () => ok(null)),
];
