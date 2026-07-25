# Frontend Tech Stack

> Version: v1.0 | Date: 2026-07-25 | Status: Confirmed

## 1. 概述

XCMS 前端是集团中台门户，含管理面（13 个功能模块）与业务面（6 个功能模块）。前端与后端 Spring Boot 4 单体应用对接，采用"全 POST + ApiResponse"接口风格。

本文档定义前端技术选型与工程规范，作为前端团队开发的基线。

## 2. 技术选型总览

| 类别 | 选型 | 版本 | 定位 |
|------|------|------|------|
| 语言 | TypeScript | 5.4+ | 类型安全，与后端 DTO 字段对齐 |
| 构建 | Vite | 6+ | 开发体验（HMR、ESM） |
| 框架 | React | 18+ | UI 核心框架 |
| 路由 | React Router | v6 | 管理面/业务面嵌套路由 |
| 状态管理 | Zustand | 4+ | 客户端状态（UI 状态、用户信息、租户上下文） |
| 服务端状态 | TanStack Query | v5 | 列表/详情的缓存、失效、重试 |
| HTTP | axios | 1.7+ | 请求拦截器（Token、租户头、错误码统一处理） |
| 样式 | Tailwind CSS | 4+ | 布局、间距、响应式、自定义样式 |
| 图表 | ECharts + echarts-for-react | 5+ | 运营看板、计量报表 |
| 代码规范 | ESLint + Prettier | - | 统一代码风格 |
| 提交校验 | lint-staged | 15+ | 暂存区文件提交前校验 |
| 包管理 | pnpm | 9+ | 速度与磁盘效率 |
| Mock | MSW | 2+ | 联调前开发，与 axios 无缝衔接 |
| i18n | react-i18next | 14+（预留） | 多语言扩展点，Phase 1 仅中文 |

> **不引入项**：UI 组件库（Ant Design 等）、表单库、husky。组件由前端团队基于 Tailwind 自建，保持设计风格自主可控。

## 3. 选型说明

### 3.1 TypeScript

全量 TypeScript，禁用 `any`。后端 DTO（如 `TenantDTO`、`UserDTO`）通过手工定义 interface 对齐字段，保证类型安全。

```typescript
// 示例：与后端 ApiResponse<T> 对齐
interface ApiResponse<T> {
  errorCode: string;
  errorMsg: string;
  data: T;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

// 与后端 TenantDTO 对齐
interface TenantDTO {
  id: number;
  tenantCode: string;
  tenantName: string;
  tenantType: 'ORGANIZATION' | 'PROJECT' | 'EXTERNAL' | 'PLATFORM';
  parentId: number | null;
  level: number;
  path: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'MIGRATING' | 'ARCHIVED';
  deploymentMode: string;
  createdAt: string;
}
```

### 3.2 Vite + React

- Vite 6+ 作为构建工具，开发期 HMR 毫秒级热更
- React 18+ 使用函数组件 + Hooks，不使用 Class 组件
- 严格模式（`<React.StrictMode>`）开发期开启

### 3.3 React Router v6

采用嵌套路由结构，管理面与业务面分离：

```
/                        → 重定向到 /admin 或 /portal
/admin                   → 管理面布局
  /admin/tenant          → 租户管理
  /admin/organization    → 组织架构
  /admin/user            → 用户管理
  /admin/permission      → 权限管理
  ...
/portal                  → 业务面布局
  /portal/workbench      → 工作台
  /portal/workflow       → 流程中心
  /portal/message        → 消息中心
  ...
/login                   → 登录页（独立布局）
```

路由守卫根据 Token + 角色判断访问权限，双重角色顶部切换管理面/业务面。

### 3.4 Zustand — 客户端状态

管理不涉及服务端数据的 UI 状态：

- 当前登录用户信息（userId、tenantId、角色）
- 主题/布局偏好（侧边栏折叠状态、语言）
- 全局通知/确认弹窗状态
- 当前租户上下文（与后端 `TenantContext` 对齐）

```typescript
interface UserStore {
  userId: number | null;
  tenantId: number | null;
  roles: string[];
  token: string | null;
  setAuth: (token: string, userId: number, tenantId: number, roles: string[]) => void;
  clearAuth: () => void;
}

const useUserStore = create<UserStore>((set) => ({
  userId: null,
  tenantId: null,
  roles: [],
  token: null,
  setAuth: (token, userId, tenantId, roles) => set({ token, userId, tenantId, roles }),
  clearAuth: () => set({ token: null, userId: null, tenantId: null, roles: [] }),
}));
```

### 3.5 TanStack Query v5 — 服务端状态

所有列表/详情/变更等与服务端交互的数据用 React Query 管理：

- 自动缓存与失效（如创建租户后自动刷新租户列表）
- 内置 `isLoading`/`error`/`data` 状态，消除手写 loading 样板
- 分页查询配合 `keepPreviousData` 实现流畅翻页

```typescript
// 租户列表查询
const { data, isLoading, error } = useQuery({
  queryKey: ['tenants', { parentId, page, size }],
  queryFn: () => tenantApi.list({ parentId, page, size }),
});

// 创建租户（成功后自动刷新列表）
const mutation = useMutation({
  mutationFn: tenantApi.create,
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tenants'] }),
});
```

**分工原则**：Zustand 管"客户端有什么"（UI 状态、登录态），React Query 管"服务端返回什么"（数据缓存）。

### 3.6 axios — HTTP 客户端

统一封装请求拦截器，处理：

- Token 注入（请求头 `Authorization: Bearer {token}`）
- 租户上下文（请求头 `X-Tenant-Id`，由 store 读取）
- 响应拦截：`errorCode !== "0"` 时统一抛错 + 错误提示
- 401 自动跳转登录页

```typescript
const api = axios.create({ baseURL: '/api', timeout: 30000 });

api.interceptors.request.use((config) => {
  const { token, tenantId } = useUserStore.getState();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (tenantId) config.headers['X-Tenant-Id'] = String(tenantId);
  return config;
});

api.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse<unknown>;
    if (data.errorCode !== '0') {
      message.error(data.errorMsg);
      return Promise.reject(new Error(data.errorMsg));
    }
    return data.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      useUserStore.getState().clearAuth();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 3.7 Tailwind CSS 4 — 样式方案

- 全局原子化 CSS，配合设计系统的 Design Tokens（色彩、间距、圆角）
- 自定义组件不依赖 UI 组件库，用 Tailwind 原子类组合
- 暗色模式通过 `dark:` 变体预留
- 响应式断点：`sm`（640px）、`md`（768px）、`lg`（1024px）、`xl`（1280px）

```css
/* tailwind.config.ts — Design Tokens 与设计系统对齐 */
export default {
  theme: {
    extend: {
      colors: {
        primary: { /* 主色 */ },
        /* 详见设计系统文档 */
      },
    },
  },
};
```

### 3.8 ECharts — 图表

运营看板（`05-operations.md`）与计量报表需要图表：
- 柱状图（资源使用 TOP5、租户层级分布）
- 折线图（用户活跃趋势、流程完成率趋势）
- 饼图（配额使用率分布）
- 仪表盘（系统健康度）

通过 `echarts-for-react` 封装为 React 组件。

### 3.9 lint-staged（无 husky）

不引入 husky（git hook 自动安装），由开发者在本地配置 git hook 或通过 CI 校验。lint-staged 配置：

```json
// package.json
{
  "lint-staged": {
    "*.{ts,tsx}": ["eslint --fix", "prettier --write"],
    "*.{css,md,json}": ["prettier --write"]
  }
}
```

> 由于不使用 husky 自动安装 hook，需在项目 README 中说明：开发者需手动配置 `pre-commit` hook 或依赖 CI 校验。

### 3.10 MSW — Mock

联调前用 MSW（Mock Service Worker）拦截网络请求返回 Mock 数据，与 axios 无缝衔接。切换到真实后端只需关闭 MSW，零代码改动。

```typescript
// mock/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/admin/tenant/list', () => {
    return HttpResponse.json({
      errorCode: '0',
      errorMsg: 'success',
      data: { list: mockTenants, total: 3 },
    });
  }),
];
```

### 3.11 react-i18next（预留）

Phase 1 仅中文，但代码结构按可扩展写：
- 所有文案走 `t('key')` 而非硬编码
- 菜单/按钮/提示文案提取到 `locales/zh-CN.json`
- 后续增加 `locales/en-US.json` 即可支持多语言

## 4. 工程规范

### 4.1 目录结构

```
xcms-frontend/
├── public/
├── src/
│   ├── api/                    # API 请求封装（按后端模块组织，与 api 模块一一对应）
│   │   ├── http.ts             # axios 实例 + 拦截器（Token/租户头/错误码）
│   │   ├── tenant.ts           # 租户管理（对应 tenant-api）
│   │   ├── organization.ts     # 组织架构（对应 org-api）
│   │   ├── identity.ts         # 用户/角色（对应 identity-api）
│   │   ├── auth.ts             # 登录/会话（对应 identity-api AuthService）
│   │   ├── authorization.ts    # 权限管理（对应 auth-api）
│   │   ├── workflow.ts         # 流程管理（对应 workflow-api）
│   │   ├── message.ts          # 消息中心（对应 message-api）
│   │   ├── configuration.ts    # 配置管理（对应 config-api）
│   │   ├── file.ts             # 文件管理（对应 file-api）
│   │   ├── task.ts             # 任务调度（对应 task-api）
│   │   ├── audit.ts            # 审计日志（对应 audit-api）
│   │   ├── portal.ts           # 工作台（业务面聚合）
│   │   └── operation.ts        # 运营看板
│   ├── components/             # 通用组件（无 UI 库，基于 Tailwind）
│   │   ├── Table/
│   │   ├── Form/
│   │   ├── Tree/
│   │   ├── Modal/
│   │   └── ...
│   ├── layouts/                # 布局
│   │   ├── AdminLayout.tsx     # 管理面布局（侧边栏+顶栏）
│   │   ├── PortalLayout.tsx    # 业务面布局
│   │   └── AuthLayout.tsx      # 登录页布局
│   ├── pages/                  # 页面（按路由组织）
│   │   ├── admin/
│   │   │   ├── tenant/
│   │   │   ├── organization/
│   │   │   ├── user/
│   │   │   └── ...
│   │   ├── portal/
│   │   │   ├── workbench/
│   │   │   ├── workflow/
│   │   │   └── ...
│   │   └── login/
│   ├── hooks/                  # 自定义 Hooks
│   ├── stores/                 # Zustand stores
│   │   ├── useUserStore.ts
│   │   └── useAppStore.ts
│   ├── types/                  # TypeScript 类型定义（与后端 DTO 对齐）
│   │   ├── tenant.ts
│   │   ├── organization.ts
│   │   └── ...
│   ├── utils/                  # 工具函数
│   ├── locales/                # i18n 文案
│   │   └── zh-CN.json
│   ├── mocks/                  # MSW Mock 数据
│   │   ├── handlers.ts
│   │   └── data/
│   ├── routes/                 # 路由配置
│   ├── styles/                 # 全局样式
│   └── App.tsx
├── .eslintrc.cjs
├── .prettierrc
├── tailwind.config.ts
├── tsconfig.json
├── vite.config.ts
├── package.json
└── pnpm-lock.yaml
```

### 4.2 命名规范

| 类别 | 规范 | 示例 |
|------|------|------|
| 文件 | kebab-case | `tenant-list.tsx`、`use-auth.ts` |
| 组件 | PascalCase | `TenantList`、`DepartmentTree` |
| Hook | camelCase + `use` 前缀 | `useTenantList`、`useAuth` |
| Store | camelCase + `use` 前缀 + `Store` 后缀 | `useUserStore`、`useAppStore` |
| 类型 | PascalCase | `TenantDTO`、`DepartmentTreeDTO` |
| 常量 | UPPER_SNAKE_CASE | `MAX_TENANT_LEVEL`、`DEFAULT_PAGE_SIZE` |
| API 函数 | camelCase | `listTenants`、`createTenant` |

### 4.3 API 请求规范

- 所有接口遵循后端"全 POST"风格，参数通过 `@RequestBody`（即 `data` 传递）
- GET 仅限文件下载等流式响应场景
- API 函数返回 Promise<T>（已解包 data），不含 ApiResponse 外壳

```typescript
// src/api/tenant.ts
import { http } from './http';
import type { TenantDTO, PageResult, TenantCreateRequest } from '@/types/tenant';

export const tenantApi = {
  list: (params: TenantListRequest) =>
    http.post<PageResult<TenantDTO>>('/admin/tenant/list-children', params),
  create: (data: TenantCreateRequest) =>
    http.post<TenantDTO>('/admin/tenant/create', data),
  get: (id: number) =>
    http.post<TenantDTO>('/admin/tenant/get', { id }),
  delete: (id: number) =>
    http.post<void>('/admin/tenant/delete', { id }),
};
```

### 4.4 代码规范（ESLint + Prettier）

```json
// .prettierrc
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2
}
```

ESLint 推荐 `@typescript-eslint/recommended` + React 插件，禁用 `any`。

### 4.5 提交规范

Git 提交信息遵循 Conventional Commits：

```
<type>(<scope>): <subject>

type: feat | fix | docs | style | refactor | test | chore
scope: frontend | tenant | organization | ...
```

示例：
```
feat(frontend): 初始化前端工程骨架（Vite + React + TS）
feat(tenant): 实现租户管理页面
fix(organization): 修复部门树移动后子树未刷新
```

## 5. 与后端的对接约定

### 5.1 接口风格

- 全 POST（ADR-010），参数在 `@RequestBody`（JSON body）
- 统一响应：`{ errorCode: "0", errorMsg: "success", data: T }`
- 分页：`PageResult<T> = { list: T[], total: number }`
- 错误码：`errorCode` 非 `"0"` 即错误，前端统一拦截提示

### 5.2 鉴权

- 登录成功后返回 Token，存入 Zustand + localStorage
- 请求头：`Authorization: Bearer {token}`
- 租户头：`X-Tenant-Id: {tenantId}`
- 401 响应：清除登录态，跳转登录页

### 5.3 数据类型对齐

前端 `src/types/` 下的 interface 与后端 `*-api` 模块的 DTO 一一对应。后端 DTO 变更时前端同步更新类型定义。

## 6. 开发环境

| 工具 | 版本要求 |
|------|---------|
| Node.js | 20+（LTS） |
| pnpm | 9+ |
| 浏览器 | Chrome/Edge 最新版（开发调试） |

```bash
# 初始化
pnpm install

# 开发（含 MSW Mock）
pnpm dev

# 联调（代理到后端，关闭 Mock）
pnpm dev:proxy

# 构建
pnpm build

# 代码检查
pnpm lint
```

## 7. 不引入项说明

| 不引入 | 原因 | 替代方案 |
|--------|------|---------|
| UI 组件库（Ant Design 等） | 严重影响设计风格，需保持视觉自主可控 | 基于 Tailwind 自建组件 |
| 表单库（antd Form 等） | 同上 | 基于 Tailwind + 自定义 Hooks 封装表单 |
| husky | 不自动安装 git hook | lint-staged + 手动 hook 或 CI 校验 |

## 8. 待后续确认

- 设计系统（色彩/字体/间距/组件规范）— 需单独产出
- 页面原型 — 需按管理面/业务面逐页设计
- 前端工程初始化 — 需搭建 Vite + React + TS 骨架
