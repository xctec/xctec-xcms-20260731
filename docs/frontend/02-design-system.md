# XCMS Design System

> Version: v2.0 | Date: 2026-07-25 | Status: Confirmed
> 产品类型：企业集团中台 | 设计风格：Data-Dense Dashboard（数据密集型仪表盘）

---

## 1. 设计原则

XCMS 是企业集团中台，面向管理员和业务用户，以表格、表单、树形结构、流程图为主。设计原则：

1. **信息密度优先** — 在有限屏幕空间内展示最多有效信息，减少留白浪费
2. **功能 > 装饰** — 去视觉噪音，颜色仅用于状态语义，不做纯装饰
3. **一致的可预期性** — 同类操作在不同页面表现一致，降低学习成本
4. **清晰的层级** — 通过字号、颜色、间距建立视觉层级，一眼定位重点
5. **可访问性** — 对比度满足 WCAG AA（4.5:1），支持键盘导航
6. **尺寸自适应** — 支持紧凑/标准/宽松三档密度切换，适配不同场景与屏幕

---

## 2. 尺寸系统（Density）

XCMS 支持 **紧凑 / 标准 / 宽松** 三档密度，用户可在顶栏 Size 切换器中实时切换。所有组件尺寸由 `densityMap` 统一驱动。

### 2.1 适用场景

| 密度 | 场景 |
|------|------|
| compact（紧凑） | 数据密集型场景，一屏看最多数据（运维监控、数据报表、审计日志） |
| standard（标准） | 日常管理操作（默认值） |
| comfortable（宽松） | 大屏展示、触屏设备、高管看板 |

### 2.2 尺寸映射表

| Token | compact | standard | comfortable | 用途 |
|-------|---------|----------|-------------|------|
| `rowHeight` | 36px | 44px | 56px | 表格行高 |
| `headerHeight` | 32px | 38px | 44px | 表头高度 |
| `cellPadX` | 12px | 16px | 20px | 单元格水平内边距 |
| `btnHeight` | 28px | 34px | 40px | 按钮高度（Primary/Secondary/Danger） |
| `btnHeightSm` | 24px | 30px | 36px | 小按钮高度（展开行内操作） |
| `inputHeight` | 28px | 34px | 40px | 输入框/Select 高度 |
| `cardPadding` | 12px | 16px | 24px | 卡片内边距 |
| `cardRadius` | 8px | 10px | 12px | 卡片圆角 |
| `gap` | 12px | 16px | 20px | 区块间距 |
| `gapSm` | 8px | 12px | 16px | 组件内间距 |
| `fontSize` | 12px | 13px | 14px | 正文 |
| `fontSizeSm` | 11px | 12px | 13px | 次要文字 |
| `fontSizeXs` | 10px | 11px | 12px | 辅助文字/标签 |
| `titleSize` | 18px | 22px | 26px | 页面标题 |
| `statValueSize` | 22px | 26px | 32px | 统计数字 |
| `iconSize` | 16px | 18px | 20px | 标准图标 |
| `iconSizeSm` | 14px | 15px | 17px | 小图标 |
| `menuHeight` | 32px | 36px | 44px | 菜单项高度 |
| `badgeHeight` | 24px | 30px | 34px | 分页/筛选标签高度 |
| `topbarHeight` | 48px | 56px | 64px | 顶栏高度 |
| `sidebarWidth` | 200px | 240px | 260px | 侧边栏宽度 |

### 2.3 实现方式

```typescript
type Density = 'compact' | 'standard' | 'comfortable';

const densityMap: Record<Density, Record<string, number>> = {
  compact: { rowHeight: 36, headerHeight: 32, btnHeight: 28, ... },
  standard: { rowHeight: 44, headerHeight: 38, btnHeight: 34, ... },
  comfortable: { rowHeight: 56, headerHeight: 44, btnHeight: 40, ... },
};

// 在根组件注入 CSS 变量
const d = densityMap[currentDensity];
document.documentElement.style.setProperty('--row-height', `${d.rowHeight}px`);
document.documentElement.style.setProperty('--btn-height', `${d.btnHeight}px`);
```

---

## 3. 色彩系统

### 3.1 主题色系（7 套）

XCMS 支持 7 套主题色系，用户可在顶栏主题切换器中选择。每套色系含亮/暗双色定义，切换即时生效。

| 色系 | 名称 | 亮色 Primary | 暗色 Primary | 风格 |
|------|------|-------------|-------------|------|
| `blue` | 靛蓝 | `#2563EB` | `#3B82F6` | 默认，专业沉稳 |
| `violet` | 紫罗兰 | `#7C3AED` | `#A78BFA` | 现代，SaaS 风格 |
| `emerald` | 翡翠 | `#059669` | `#34D399` | 自然，增长导向 |
| `cyan` | 青碧 | `#0891B2` | `#22D3EE` | 科技，数据监控 |
| `rose` | 玫瑰 | `#E11D48` | `#FB7185` | 活力，创新产品 |
| `amber` | 琥珀 | `#D97706` | `#F59E0B` | 温暖，轻量管理 |
| `slate` | 石板 | `#475569` | `#94A3B8` | 中性，极简风格 |

每套色系需定义完整色阶（50/100/200/500/600/700），以 `blue` 为例：

| Token | 色值 | 用途 |
|-------|------|------|
| `primary-50` | `#EFF6FF` | 悬浮背景、选中行底色 |
| `primary-100` | `#DBEAFE` | 浅色标签背景、头像背景 |
| `primary-200` | `#BFDBFE` | 边框高亮 |
| `primary-500` | `#2563EB` | **主色**，按钮、链接、激活态 |
| `primary-600` | `#1D4ED8` | 按钮 hover |
| `primary-700` | `#1E40AF` | 按钮 active、标题强调 |

> 其他 6 套色系的完整色阶定义见 `frontend-preview/src/App.tsx` 中 `schemes` 对象。

### 3.2 语义色（固定，不随主题切换）

语义色不随主题色系变化，保持状态语义一致。

| Token | 色值 | 用途 |
|-------|------|------|
| `success-500` | `#16A34A` | 成功状态、ACTIVE、启用 |
| `success-50` | `#F0FDF4` | 成功背景 |
| `warning-500` | `#D97706` | 警告、SUSPENDED、待处理 |
| `warning-50` | `#FFFBEB` | 警告背景 |
| `danger-500` | `#DC2626` | 错误、LOCKED、删除、禁用 |
| `danger-50` | `#FEF2F2` | 错误背景 |
| `info-500` | `#0891B2` | 信息提示、MIGRATING |
| `info-50` | `#ECFEFF` | 信息背景 |

### 3.3 中性色（随亮/暗模式变化）

| Token | 亮色 | 暗色 | 用途 |
|-------|------|------|------|
| `bg` | `#F8FAFC` | `#0F172A` | 页面背景 |
| `card` | `#FFFFFF` | `#1E293B` | 卡片、面板、表格容器 |
| `text` | `#0F172A` | `#F1F5F9` | 主文字、标题 |
| `textSec` | `#475569` | `#94A3B8` | 次要文字 |
| `textMuted` | `#94A3B8` | `#64748B` | 辅助文字、占位符 |
| `border` | `#E2E8F0` | `#334155` | 边框、分割线 |
| `borderLight` | `#F1F5F9` | `#1E293B` | 浅分割线 |
| `hover` | `#F8FAFC` | `#1E293B` | 行 hover 背景 |
| `headerBg` | `#F8FAFC` | `#1E293B` | 表头背景 |
| `codeBg` | `#F1F5F9` | `#334155` | 代码/编码背景 |

### 3.4 状态色映射

| 业务状态 | 语义色 | 标签样式 |
|---------|--------|---------|
| ACTIVE | success | `bg-success-50 text-success-500` |
| SUSPENDED | warning | `bg-warning-50 text-warning-500` |
| LOCKED / ARCHIVED | danger | `bg-danger-50 text-danger-500` |
| MIGRATING | info | `bg-info-50 text-info-500` |

---

## 4. 亮/暗模式

### 4.1 双模式规范

XCMS 支持亮/暗双模式，用户可在顶栏主题切换器中切换。

| 模式 | 页面背景 | 卡片背景 | 主文字 | 边框 |
|------|---------|---------|--------|------|
| Light（亮色） | `#F8FAFC` | `#FFFFFF` | `#0F172A` | `#E2E8F0` |
| Dark（暗色） | `#0F172A` | `#1E293B` | `#F1F5F9` | `#334155` |

### 4.2 实现方式

通过 CSS 变量驱动，切换时设置 `data-theme` 属性：

```typescript
useEffect(() => {
  const root = document.documentElement;
  root.setAttribute('data-theme', mode);
  // 注入对应模式的色值到 CSS 变量
  root.style.setProperty('--c-bg', mode === 'dark' ? '#0F172A' : '#F8FAFC');
  root.style.setProperty('--c-card', mode === 'dark' ? '#1E293B' : '#FFFFFF');
  // ...
}, [mode]);
```

### 4.3 暗色模式适配要点

- 阴影增强：暗色模式阴影 `rgba(0,0,0,0.3~0.4)`，亮色模式 `rgba(0,0,0,0.04~0.1)`
- 边框对比：暗色模式边框色需提亮（`#334155`），否则与背景区分度不足
- 图标/语义色：暗色模式下语义色需提亮（如 success 从 `#16A34A` → `#4ADE80`）

---

## 5. 字体排版

### 5.1 字体家族

选用 **Inter**（Google Fonts 开源），单一字体多字重，简洁专业，专为屏幕阅读优化。

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');
```

- **等宽场景**：租户编码、用户 ID、错误码等用 `font-mono`（JetBrains Mono）

### 5.2 字号层级

字号随密度联动（见 §2.2），以标准密度为基准：

| Token | standard | compact | comfortable | 用途 |
|-------|----------|---------|-------------|------|
| `text-xs` | 11px | 10px | 12px | 辅助文字、标签 |
| `text-sm` | 12px | 11px | 13px | 次要文字、表格次要列 |
| `text-base` | 13px | 12px | 14px | **正文/表格默认**、表单输入 |
| `text-lg` | 16px | 14px | 18px | 小标题、卡片标题 |
| `text-xl` | 22px | 18px | 26px | 页面标题 |
| `text-2xl` | 26px | 22px | 32px | 统计数字 |

> 中台默认正文 13px（标准密度），而非常见的 16px，因信息密度优先。

### 5.3 字重

| Token | weight | 用途 |
|-------|--------|------|
| `font-normal` | 400 | 正文 |
| `font-medium` | 500 | 表单 label、表格表头、菜单项 |
| `font-semibold` | 600 | 卡片标题、强调文字 |
| `font-bold` | 700 | 页面标题、统计数字、按钮文字 |

---

## 6. 间距系统

采用 **4px 基准网格**，间距随密度联动：

| Token | compact | standard | comfortable | 用途 |
|-------|---------|----------|-------------|------|
| `space-1` | 4px | 4px | 4px | 图标与文字间距（固定） |
| `space-2` | 8px | 8px | 8px | 紧凑内边距（固定） |
| `gapSm` | 8px | 12px | 16px | 组件内间距 |
| `gap` | 12px | 16px | 20px | **默认间距**、区块间距 |
| `pad` | 12px | 16px | 20px | 单元格/按钮水平内边距 |
| `cardP` | 12px | 16px | 24px | 卡片内边距 |
| `space-6` | 24px | 24px | 24px | 卡片间距（固定） |

### 6.1 页面布局间距

```
┌─ Page Padding: gap ────────────────────────────────┐
│  ┌─ Card ────────────────────────────────────────┐  │
│  │ Card Padding: cardP                          │  │
│  │                                               │  │
│  │  Section Gap: gap                             │  │
│  │                                               │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  Card Gap: gap                                      │
│                                                     │
│  ┌─ Card ────────────────────────────────────────┐  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## 7. 圆角与阴影

### 7.1 圆角（随密度联动）

| Token | compact | standard | comfortable | 用途 |
|-------|---------|----------|-------------|------|
| `rounded-none` | 0 | 0 | 0 | 表格、分隔线 |
| `rounded-sm` | 2px | 2px | 2px | 小标签、badge |
| `rounded` | 4px | 4px | 4px | 输入框、按钮 |
| `rounded-md` | 6px | 6px | 6px | 下拉菜单、tooltip |
| `cardRadius` | 8px | 10px | 12px | **卡片、弹窗、面板** |
| `rounded-full` | 9999px | 9999px | 9999px | 头像、状态圆点 |

### 7.2 阴影（随亮/暗模式变化）

| Token | 亮色 | 暗色 | 用途 |
|-------|------|------|------|
| `shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` | `0 1px 2px rgba(0,0,0,0.2)` | 输入框、按钮 |
| `shadow` | `0 1px 3px rgba(0,0,0,0.1)` | `0 1px 3px rgba(0,0,0,0.3)` | 下拉菜单、popover |
| `shadow-card` | `0 1px 3px rgba(0,0,0,0.06)` | `0 4px 12px rgba(0,0,0,0.3)` | **卡片默认** |
| `shadow-hover` | `0 4px 12px rgba(0,0,0,0.08)` | `0 8px 24px rgba(0,0,0,0.4)` | 卡片 hover、弹窗 |
| `shadow-none` | none | none | 表格容器（用边框替代） |

---

## 8. 栅格与布局

### 8.1 栅格系统

- 12 列栅格，列间距随密度联动
- 内容区最大宽度 1600px，超宽屏居中
- 断点：

| 断点 | 宽度 | 场景 |
|------|------|------|
| `sm` | ≥640px | 平板竖屏 |
| `md` | ≥768px | 平板横屏 |
| `lg` | ≥1024px | **桌面（中台主要场景）** |
| `xl` | ≥1280px | 宽屏桌面 |
| `2xl` | ≥1536px | 超宽屏 |

### 8.2 管理面布局

```
┌──────────────────────────────────────────────────────────────────┐
│ TopBar (48-64px)  [管理面|业务面] | 面包屑    [主题][size][语言][🔔][用户]│
├──────────┬───────────────────────────────────────────────────────┤
│          │                                                       │
│ SideBar  │  Content Area (max-width: 1600px, 居中)               │
│ (200-    │  ┌─ Page Header (标题 + 操作按钮) ──────────────────┐ │
│  260px)  │  ├─ Stats Cards (4列, 带趋势图) ────────────────────┤ │
│          │  ├─ Quick Filters + Search ──────────────────────────┤ │
│ 含:      │  ├─ Batch Action Bar (选中时出现) ─────────────────┤ │
│ - Logo   │  ├─ Table (固定列 + 展开行 + 批量选择) ─────────────┤ │
│ - 搜索   │  └─ Pagination ─────────────────────────────────────┘ │
│ - 菜单组 │                                                       │
│ - 用户卡 │                                                       │
│ - 折叠   │                                                       │
└──────────┴───────────────────────────────────────────────────────┘
```

**顶栏分区**：
- 左侧：管理面/业务面切换 + 分隔线 + 面包屑
- 右侧（从左到右）：主题色切换 → Size 切换 → 语言 → 通知 → 用户 Profile

**侧边栏分区**（从上到下）：
- Logo（渐变背景）
- 全局搜索（含 ⌘K 快捷键）
- 菜单分组（按模块分类，带徽标）
- 用户卡片（头像 + 姓名 + 租户·层级）
- 折叠按钮

### 8.3 业务面布局

```
┌──────────────────────────────────────────────────────────────────┐
│ TopBar  [管理面|业务面] | 面包屑          [主题][size][语言][🔔][用户]│
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌─ Workbench ──────────────────────────────────────────────┐   │
│  │ ┌─KPI─┐ ┌─KPI─┐ ┌─KPI─┐ ┌─KPI─┐                          │   │
│  │ └─────┘ └─────┘ └─────┘ └─────┘                          │   │
│  │ ┌─Todo List ──────┐ ┌─ Notifications ────────────────────┐ │   │
│  │ │                 │ │                                     │ │   │
│  │ └─────────────────┘ └─────────────────────────────────────┘ │   │
│  └────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────┘
```

---

## 9. 组件规范

> 不引入 UI 组件库，以下规范指导基于 Tailwind + CSS 变量自建组件。

### 9.1 按钮

| 类型 | 样式 | 高度（随密度） |
|------|------|---------------|
| Primary | `bg-primary text-white hover:bg-primary-600` | 28-40px |
| Secondary | `bg-card border border-border text-textSec hover:border-primary` | 同上 |
| Danger | `bg-danger text-white hover:opacity-90` | 同上 |
| Ghost | `bg-transparent text-primary hover:bg-primary-50` | 同上 |
| Link | `text-primary hover:underline` | 自适应 |

按钮 hover 有 `box-shadow` 微光效果：
```tsx
<button style={{
  height: d.btnHeight, padding: `0 ${d.pad}px`,
  borderRadius: 6, border: 'none', cursor: 'pointer',
  backgroundColor: c.primary, color: '#fff',
  boxShadow: `0 2px 8px ${c.primary}30`,
  display: 'flex', alignItems: 'center', gap: 6,
}}>
  <Plus size={d.iconSm} /> 新建租户
</button>
```

### 9.2 表格

信息密度核心组件。

| 元素 | 规范 |
|------|------|
| 表头 | `backgroundColor: headerBg`, `height: headerHeight`, `fontSize: fontSizeSm`, `color: textSec` |
| 行 | `height: rowHeight`, `fontSize: fontSize`, hover → `backgroundColor: hover` |
| 选中行 | `backgroundColor: primary-50` |
| 单元格 | `padding: 0 ${cellPadX}px` |
| 操作列 | 右对齐，Primary 链接 + 更多按钮（hover 高亮） |

**进阶能力**：
- **固定列**：Checkbox 列 + 首列（租户名称）sticky 定位，横向滚动不跟随
- **行展开**：点击行展开详情，ChevronRight 旋转 90° 指示
- **批量选择**：全选 Checkbox + 选中后出现批量操作栏
- **配额进度条**：使用率 >80% 红色、>60% 黄色、其他绿色
- **状态标签**：胶囊样式 `borderRadius: 12`，含状态圆点

### 9.3 统计卡片

| 元素 | 规范 |
|------|------|
| 容器 | `card`, `borderRadius: cardRadius`, `padding: cardP`, hover 时 `shadowHover` |
| 左侧色条 | 3px 宽，语义色 |
| 图标 | 圆角方块背景 `color + 15% 透明度`，图标 `color` |
| 数值 | `statValueSize` + `font-bold` |
| 趋势图 | Sparkline，带渐变填充区域 |
| 变化指示 | 右上角，↑ 绿色 / ↓ 红色（预警类指标反向） |

### 9.4 表单

| 元素 | 规范 |
|------|------|
| Label | `fontSizeSm font-medium text-textSec`，必填项加 `*`（红色） |
| Input | `height: inputHeight`, `borderRadius: 6`, focus 时 `borderColor: primary` |
| 错误提示 | `fontSizeXs text-danger mt-1` |
| 间距 | 表单项间距 `gapSm`，分组间距 `gap` |
| 按钮 | 表单底部右对齐：取消（Secondary）+ 确定（Primary） |

### 9.5 树形组件

- 节点高度随密度（`menuHeight`），缩进 20px/级
- 展开图标：ChevronRight（展开变 ChevronDown）
- 选中节点：`bg-primary-50 text-primary-700`
- 拖拽排序：拖拽时显示插入线（主色 2px）

### 9.6 状态标签

胶囊样式，含状态圆点：

```tsx
<span style={{
  display: 'inline-flex', alignItems: 'center', gap: 5,
  padding: '2px 8px', borderRadius: 12,
  fontSize: fontSizeXs, fontWeight: 500,
  backgroundColor: statusBg[status], color: statusColor[status],
}}>
  <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: statusColor[status] }} />
  {statusLabel}
</span>
```

### 9.7 弹窗（Modal）

- 遮罩：`bg-black/40`
- 容器：`bg-card rounded-lg shadow-hover`，最大宽 `max-w-lg`（480px）
- 头部：标题 `text-lg font-semibold` + 关闭按钮（右上 X）
- 底部：按钮右对齐
- 动画：淡入 + 轻微上移（150ms）

### 9.8 抽屉（Drawer）

- 宽度：400px（默认）/ 600px（lg）
- 位置：右侧滑出
- 遮罩：同 Modal

### 9.9 消息提示（Toast）

| 类型 | 图标 | 颜色 |
|------|------|------|
| success | CheckCircle | `success` |
| error | XCircle | `danger` |
| warning | AlertTriangle | `warning` |
| info | Info | `info` |

- 位置：顶部居中，距顶 `gap`（16px）
- 自动消失：3 秒（error 为 5 秒）

### 9.10 下拉菜单（Dropdown）

顶栏图标按钮的下拉菜单规范：

- 触发：图标按钮（边框 + 圆角 6px）
- 定位：`position: absolute`, top: `topbarHeight - 4`, right: 0
- 容器：`bg-card`, `borderRadius: 8`, `border`, `shadow-hover`, `minWidth: 180px`
- 菜单项：`padding: 6px 8px`, `borderRadius: 5`, hover → `bg-hover`
- 当前项：`bg-primary-50 text-primary` + 右侧 CheckCircle
- 分隔线：1px `border` 色

### 9.11 批量操作栏

选中行后出现：

- 背景：`primary-50`
- 左侧：已选数量 + 清除按钮
- 右侧：批量操作按钮（锁定/停用/删除）
- 删除按钮用 danger 边框 + 透明背景

---

## 10. 图标规范

使用 **Lucide Icons**（SVG 图标库，线条风格）。

- 标准尺寸：随密度（`iconSize`，16-20px）
- 小尺寸：`iconSizeSm`（14-17px），用于表格操作列、菜单项
- 颜色继承 `currentColor`，通过 `color` 属性控制
- **禁止使用 emoji 作为图标**

顶栏图标映射：

| 功能 | 图标 |
|------|------|
| 主题色切换 | Palette |
| Size 切换 | Type |
| 语言 | Globe |
| 通知 | Bell |
| 暗色/亮色 | Moon / Sun |
| 密度（紧凑/标准/宽松） | Type |

菜单图标映射：

| 菜单 | 图标 |
|------|------|
| 租户管理 | Building2 |
| 组织架构 | Network |
| 用户管理 | Users |
| 权限管理 | Shield |
| 运营看板 | BarChart3 |
| 消息中心 | Bell |
| 配置管理 | Settings |
| 文件管理 | FolderOpen |
| 审计日志 | FileText |
| 工作台 | LayoutDashboard |
| 个人中心 | User |

---

## 11. 状态规范

### 11.1 加载态（Loading）

| 场景 | 方案 |
|------|------|
| 表格加载 | 表格区域居中 Spinner（`animate-spin`），保留表头 |
| 按钮提交 | 按钮内显示 Spinner + 禁用按钮 |
| 页面加载 | 页面骨架屏（Skeleton），灰色块占位 |
| 局部刷新 | 局部半透明遮罩 + Spinner |

### 11.2 空状态（Empty）

- 居中图标（64px，`text-muted`）+ 标题 + 引导文案 + 操作按钮
- 示例：租户列表为空 → "暂无租户" + "创建第一个租户" 按钮

### 11.3 错误态（Error）

- 网络错误：全屏错误页，图标 + "网络异常，请稍后重试" + 重试按钮
- 权限不足：403 页面，"您无权访问此页面"
- 找不到页面：404 页面

### 11.4 无权限态

- 菜单项：隐藏无权限的菜单
- 按钮/操作：无权限的按钮不渲染或禁用（`disabled` + tooltip 提示）

---

## 12. 动画与过渡

| 场景 | 时长 | 缓动 |
|------|------|------|
| 按钮 hover/active | 150ms | `ease-in-out` |
| 下拉菜单展开 | 200ms | `ease-out` |
| 弹窗淡入 | 150ms | `ease-out` |
| 抽屉滑出 | 250ms | `ease-out` |
| 页面切换 | 200ms | `ease-out` |
| 表格行 hover | 100ms | `linear` |
| 卡片 hover 阴影 | 200ms | `ease-out` |
| 侧边栏折叠 | 200ms | `ease-in-out` |
| 展开 Chevron 旋转 | 150ms | `ease-out` |
| 密度切换 | 即时 | — |

> 尊重 `prefers-reduced-motion`：检测到用户偏好减少动画时，所有动画降为 0ms。

---

## 13. 响应式策略

中台以桌面端为主（≥1024px），但需兼容平板。

| 断点 | 布局调整 |
|------|---------|
| < 768px | 侧边栏默认折叠为图标栏，表格横向滚动 |
| 768-1024px | 侧边栏可展开/折叠，表格横向滚动 |
| ≥ 1024px | **标准布局**，侧边栏展开 |
| ≥ 1280px | 内容区可展示更多列 |
| ≥ 1536px | 内容区居中，最大宽 1600px |

> 移动端（< 768px）非主要场景，保证可用即可，不追求最优体验。

---

## 14. CSS 变量架构

所有色值和尺寸通过 CSS 变量驱动，实现主题/密度/暗色切换零重渲染：

```css
:root {
  /* 主题色（随色系切换） */
  --c-primary: #2563EB;
  --c-primary-50: #EFF6FF;
  --c-primary-100: #DBEAFE;
  --c-primary-600: #1D4ED8;
  --c-primary-700: #1E40AF;

  /* 语义色（固定） */
  --c-success: #16A34A;
  --c-warning: #D97706;
  --c-danger: #DC2626;
  --c-info: #0891B2;

  /* 中性色（随亮/暗切换） */
  --c-bg: #F8FAFC;
  --c-card: #FFFFFF;
  --c-text: #0F172A;
  --c-text-sec: #475569;
  --c-text-muted: #94A3B8;
  --c-border: #E2E8F0;
  --c-border-light: #F1F5F9;
  --c-hover: #F8FAFC;
  --c-header-bg: #F8FAFC;
  --c-code-bg: #F1F5F9;

  /* 尺寸（随密度切换） */
  --row-height: 44px;
  --header-height: 38px;
  --btn-height: 34px;
  --input-height: 34px;
  --card-padding: 16px;
  --card-radius: 10px;
  --gap: 16px;
  --gap-sm: 12px;
  --font-size: 13px;
  --font-size-sm: 12px;
  --font-size-xs: 11px;
  --title-size: 22px;
  --icon-size: 18px;
  --icon-size-sm: 15px;
  --menu-height: 36px;
  --topbar-height: 56px;
  --sidebar-width: 240px;
}

[data-theme="dark"] {
  --c-bg: #0F172A;
  --c-card: #1E293B;
  --c-text: #F1F5F9;
  /* ... */
}
```

---

## 15. 交付前检查清单

- [ ] 无 emoji 作为图标（全部使用 Lucide SVG）
- [ ] 所有可点击元素有 `cursor-pointer`
- [ ] Hover 状态有平滑过渡（150-200ms）
- [ ] 亮色/暗色模式文字对比度 ≥ 4.5:1
- [ ] 键盘导航有可见的 focus 状态
- [ ] 尊重 `prefers-reduced-motion`
- [ ] 响应式断点验证：375px / 768px / 1024px / 1440px
- [ ] 所有表单输入有关联 label
- [ ] 表单提交有 loading → success/error 反馈
- [ ] 表格移动端横向滚动（`overflow-x-auto`）
- [ ] 空状态有引导操作
- [ ] 所有尺寸使用 CSS 变量或 densityMap，无硬编码
- [ ] 所有色值使用 CSS 变量，无硬编码
- [ ] 三档密度切换验证：紧凑/标准/宽松
- [ ] 7 套主题色系切换验证
- [ ] 亮/暗模式切换验证

---

## 变更记录

| 日期 | 版本 | 变更 | 负责人 |
|------|------|------|--------|
| 2026-07-25 | v1.0 | 初版创建 | PM/UI |
| 2026-07-25 | v2.0 | 新增尺寸系统（三档密度）、7 套主题色系、暗色模式正式规范、CSS 变量架构、更新布局图与组件规范 | PM/UI |
