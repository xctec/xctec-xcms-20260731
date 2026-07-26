# XCMS 前端后续规划（Roadmap）

> Version: v1.0 | Date: 2026-07-26
> 基线：`e8fa998 feat(front): 初始化前端工程骨架与业务页面`（master）+ `7096fe8 qianduan`（pre）
> 评审方式：抽查 `http.ts`/`tenant.ts`/`TenantList.tsx`/`tailwind.config`/`vite.config`/`main.tsx` + 目录清点

---

## 一、现状评估

### ✅ 已就绪（基线良好）

| 项 | 状态 |
|----|------|
| 工程骨架 | Vite6 + React18 + TS5.7 + Tailwind3 + Zustand + TanStack Query v5 + axios + MSW + ECharts + Lucide + i18next，`package.json` 齐全 |
| 入口装配 | `main.tsx` 正确挂载 `QueryClientProvider`(staleTime 30s) + `ThemeProvider` + `ToastContainer` + i18n + MSW(`VITE_MOCK`) |
| HTTP 层 | `http.ts` 拦截器规范：Token/`X-Tenant-Id` 注入、`errorCode!="0"` 解包抛错、401 清态跳登录 |
| API 层 | 15 个模块 api 文件，`tenant.ts` 模式正确（返回解包 data，全 POST） |
| 路由 | 管理面(11)/业务面(5)/auth(3)/error(2) 全量懒加载 + `RequireAuth` 守卫 |
| 设计系统落地 | `tailwind.config` 色彩映射到 CSS 变量（`--c-primary` 等），`darkMode:'class'`，主题/暗色切换链路通；`ThemeProvider`/`PreferenceDrawer` 存在 |
| 样板页面 | `TenantList` 是完整真页面（useQuery/useMutation/筛选/表格/Modal/下拉/分页/Toast） |
| 联调代理 | `vite.config` 代理 `/api`、`/admin` → `localhost:8080`，`@` 别名 ✓ |

### ⚠️ 主要差距（实测）

| # | 差距 | 证据 | 影响 |
|---|------|------|------|
| G1 | **页面大面积占位** | `pages/Placeholder.tsx` 存在；admin 11 页仅 `TenantList` 为真，portal 5 页疑似占位 | 无法交付，需逐页实现 |
| G2 | **类型定义缺失** | `types/` 仅 4 文件（common/identity/menu/tenant），15 个 api 模块缺 11 个类型 | api 层无类型保护，`tsc` 可能大量 `any` |
| G3 | **核心组件库单薄** | `components/ui` 仅 Button/Modal/PageHeader/Pagination/StatusBadge/Toast；缺 Table/Form/Tree/Drawer/Dropdown/Skeleton/EmptyState/StatCard/Sparkline | `TenantList` 表格/表单/下拉全内联，22 页将重复造轮子、风格不一 |
| G4 | **密度系统未贯通** | `tailwind.config` 有 CSS 变量，但页面用硬编码 `py-3`/`h-9`，未消费 `--row-height`/`--btn-height` | 三档密度切换在表格/表单上不生效 |
| G5 | **Mock 覆盖不足** | `mocks/data` 仅 menus/tenants | `dev:mock` 下大部分页面 404/报错，离线开发受阻 |
| G6 | **无测试基建** | `package.json` 无 Vitest/RTL，设计系统检查清单未验 | 回归无保障 |
| G7 | **鉴权不完整** | `RequireAuth` 仅判 token；无 token 刷新、无角色路由守卫、无权限驱动菜单/按钮显隐 | 多角色/权限场景不可用 |
| G8 | **后端类型对齐手工** | `types/` 手写；后端已有 springdoc OpenAPI | DTO 漂移风险，可改为 OpenAPI 生成 |

---

## 二、分阶段规划

### Phase 0 — 基线收口（组件库 + 基建）｜关键路径，阻塞所有页面

> 目标：把"能写页面"的地基打牢，避免 22 页各写各的。先于 Phase 1 完成。

**0.1 核心组件库（按设计系统 `02-design-system.md` 实现，基于 Tailwind + CSS 变量）**
- `Table`：固定列(checkbox+首列 sticky)、行展开、批量选择、配额进度条、状态标签、空/加载态；行高消费 `--row-height`
- `Form` + `useForm`：受控字段、校验规则、错误提示、`Input`/`Select`/`Radio`/`Checkbox`/`DatePicker`；高度消费 `--input-height`
- `Tree`：缩进、展开图标、选中态、拖拽排序占位
- `Drawer`（右侧滑出 400/600）、`Dropdown`（顶栏/行内）、`Skeleton`、`EmptyState`、`ErrorState`
- `StatCard` + `Sparkline`（运营看板用）、`BatchActionBar`
- 统一消费密度变量，三档切换全组件生效（补 G3、G4）

**0.2 类型补全与对齐**
- 补 `types/`：organization、authorization、workflow、message、configuration、file、task、audit、operation、portal（11 个）（补 G2）
- 与后端 `*-api` 模块 DTO 逐字段对齐；评估接入 OpenAPI 生成（见 0.6）

**0.3 鉴权增强**
- token 刷新（refresh token 流程，对接后端 `/api/auth/refresh`）
- `RequireAuth` 扩展为角色/权限路由守卫；菜单按权限渲染；按钮级权限指令/工具（补 G7）

**0.4 测试基建**
- 引入 Vitest + @testing-library/react；组件库单测；关键 hook（useForm/useAuth）单测（补 G6）

**0.5 构建与质量门禁**
- 跑通 `pnpm build`（`tsc && vite build`）修 type 错；`pnpm lint` 清零；`tsconfig` 严格化（`noUnusedLocals` 等）
- 确认 `postcss`/`autoprefixer`/`tailwind` 内容扫描覆盖全部页面

**0.6（可选）OpenAPI 类型生成**
- 后端已有 springdoc `/v3/api-docs`；用 `openapi-typescript` 或 `orval` 生成 `types/api.d.ts` + api 调用，替代手写（补 G8，降低漂移）

**退出标准**：组件库可复用、`pnpm build`/`lint` 通过、三档密度+7 主题+暗色在组件 demo 页验证、types 覆盖全模块。

---

### Phase 1 — 核心管理面（数据密集型）｜依赖 Phase 0

> 目标：交付管理面最高频 4 个模块，验证组件库。

| 模块 | 页面要点 | 后端就绪 |
|------|---------|---------|
| 租户管理 | `TenantList` 已有→重构为用 `Table` 组件 + `Form`；补树形视图（`tenantApi.getTree`）、配额详情、启用/停用/迁移操作 | ✅（TenantController） |
| 组织架构 | `Tree`（部门树）+ 右侧 `Table`（岗位/用户组/成员）；拖拽移动子树 | 部分（OrganizationController） |
| 用户管理 | `Table` + `Form`（注册/编辑）+ 角色分配 + 状态/锁定操作 + 密码重置 | ✅（UserController） |
| 权限管理 | 角色列表 + 权限矩阵（菜单/按钮/数据权限）+ 跨租户授权；`Tree`+`Table` 组合 | ✅（RoleController/PermissionController） |

**退出标准**：4 模块端到端可用（dev:mock + 联调双通），覆盖增删改查/批量/状态流转。

---

### Phase 2 — 其余管理面｜依赖 Phase 0

| 模块 | 页面要点 |
|------|---------|
| 流程管理 | 流程模板列表、设计器入口（BPMN）、实例/任务查询；`Table`+`Drawer` 详情 |
| 配置管理 | 参数/特性开关/字典/编码规则 Tab；`Table`+`Form`+`Drawer` |
| 消息中心（管理） | 消息模板/渠道配置；`Table`+`Form` |
| 文件管理 | 文件列表/上传/存储配置；分块上传、预览 |
| 任务调度 | 任务定义/执行日志/异步任务；`Table`+`Drawer` 日志详情 |
| 审计日志 | 审计策略 + 日志检索（多筛选）；高密度 `Table` |
| 运营看板 | `StatCard`+ECharts（柱/折/饼/仪表盘）；健康检查/告警 |

**退出标准**：管理面 11 路由全部真页面，无 Placeholder。

---

### Phase 3 — 业务面（Portal）｜依赖 Phase 0

| 页面 | 要点 |
|------|------|
| 工作台 | KPI 卡 + 待办列表 + 通知；`StatCard`+`Sparkline` |
| 流程中心 | 我的流程/待办/已办；`Table`+`Drawer` 审批 |
| 消息中心 | 消息列表/已读/详情；`Table`+`Drawer` |
| 文件管理 | 个人文件/上传/分享 |
| 个人中心 | 资料/密码/偏好（主题/密度/语言）持久化 |

**退出标准**：业务面 5 路由真页面，管理面/业务面顶部切换流畅。

---

### Phase 4 — 联调与交付

- 全模块联调真实后端（关闭 MSW），验证 token/租户头/错误码链路
- 权限端到端：不同角色看到的菜单/按钮/数据范围正确
- 响应式断点验证（375/768/1024/1440）
- a11y：对比度 ≥4.5:1、键盘导航、`prefers-reduced-motion`
- 性能：路由懒加载 chunk、首屏 LCP、ECharts 按需
- 生产构建 + 部署（nginx 反代 /api→后端）
- 设计系统交付前检查清单逐项过（`02-design-system.md` §15）

---

## 三、优先级与建议

1. **先 Phase 0 组件库**——它是关键路径。否则 Phase 1-3 的 20+ 页面会各自内联表格/表单，后续重构成本极高（`TenantList` 已现此问题）。
2. **类型与后端对齐**——后端已有 OpenAPI，建议 Phase 0.6 引入生成，杜绝手写漂移。
3. **Mock 随页面滚动补**——每开发一个模块，同步补该模块 MSW handler（补 G5），保证 `dev:mock` 始终可用。
4. **密度/主题贯通**——在组件库阶段一次性把 CSS 变量消费做对（G4），避免页面阶段返工。
5. **测试左移**——组件库单测先行；页面用 RTL 覆盖关键交互。

## 四、分支与协作建议

- 当前 `pre` 分支已有 `qianduan` 增量（ThemeProvider/PreferenceDrawer/ForgotPassword/TenantList 增强）；建议以 `pre` 为前端主干，Phase 0 起按模块开 feature 分支合入。
- 后端 `feat-seed-data`/`fix-seed-tenant-context`/`feat-entity-ddl-consistency` 已就绪登录与租户/用户/角色链路，Phase 1 可直接联调。

---

## 变更记录

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-07-26 | v1.0 | 基于最新提交抽查产出首版 Roadmap，识别 8 项差距，规划 Phase 0-4 |
