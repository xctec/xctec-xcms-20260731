# XCMS Frontend

> 企业集团中台前端 — Vite + React + TypeScript + Tailwind CSS

## 技术栈

| 类别 | 选型 |
|------|------|
| 语言 | TypeScript 5.7 |
| 构建 | Vite 6 |
| 框架 | React 18 |
| 路由 | React Router v6 |
| 状态管理 | Zustand（客户端状态） |
| 服务端状态 | TanStack Query v5 |
| HTTP | axios + 拦截器 |
| 样式 | Tailwind CSS 3 |
| 图标 | Lucide Icons |
| Mock | MSW (Mock Service Worker) |
| i18n | react-i18next |
| 包管理 | pnpm |

> 不引入 UI 组件库（Ant Design 等），基于 Tailwind 自建组件，保持设计风格自主可控。

## 快速开始

```bash
# 安装依赖
pnpm install

# 开发模式（带 Mock 数据，无需后端）
pnpm dev:mock

# 联调模式（代理到后端 http://localhost:8080）
pnpm dev

# 构建
pnpm build

# 代码检查
pnpm lint
```

## Mock 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | SYSTEM_ADMIN |

## 目录结构

```
src/
├── api/              # API 封装（按后端模块组织）
│   ├── http.ts       # axios 实例 + 拦截器
│   ├── auth.ts       # 登录/会话
│   ├── tenant.ts     # 租户管理
│   ├── identity.ts   # 用户/角色
│   ├── organization.ts
│   ├── authorization.ts
│   ├── workflow.ts
│   ├── message.ts
│   ├── configuration.ts
│   ├── file.ts
│   ├── task.ts
│   ├── audit.ts
│   ├── portal.ts
│   ├── operation.ts
│   └── menu.ts
├── components/       # 通用组件
│   └── layout/       # 布局组件（Sidebar, Topbar）
├── layouts/          # 页面布局
├── pages/            # 页面
│   ├── auth/         # 登录/注册
│   ├── admin/        # 管理面页面
│   ├── portal/       # 业务面页面
│   └── error/        # 404/403
├── routes/           # 路由配置
├── stores/           # Zustand stores
├── types/            # TypeScript 类型定义
├── locales/          # i18n 文案
├── mocks/            # MSW Mock 数据
│   ├── handlers.ts
│   └── data/
└── i18n.ts
```

## 设计系统

详见 `docs/frontend/02-design-system.md`。
