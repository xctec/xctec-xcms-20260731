# 2 配置中心

### 4.2 配置中心

#### 4.2.1 功能概述

提供统一的配置管理，包括系统参数、功能开关、数据字典、编码规则。支持按租户/全局配置。

#### 4.2.2 数据模型

```
cfg_param（系统参数表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局）
├── param_key            参数键
├── param_value          参数值
├── param_type           值类型（STRING/NUMBER/BOOLEAN/JSON）
├── description          描述
├── editable             是否可编辑
└── updated_at

cfg_feature_flag（功能开关表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局）
├── feature_code         功能编码
├── enabled              是否启用
├── config               功能配置（JSON）
└── description

cfg_dictionary（数据字典表）
├── id
├── tenant_id            租户ID（@TenantId，null为全局）
├── dict_code            字典编码
├── dict_name            字典名称
├── description
└── status

cfg_dictionary_item（字典项表）
├── id
├── tenant_id            租户ID（@TenantId）
├── dict_id              字典ID
├── item_code            项编码
├── item_value           项值
├── sort_order           排序
├── parent_id            父项（树形字典）
└── status

cfg_code_rule（编码规则表）
├── id
├── tenant_id            租户ID（@TenantId）
├── rule_code            规则编码
├── rule_name            规则名称
├── pattern              规则模式（如 ${PREFIX}${YYYY}${MM}${SEQ:4}）
├── prefix               前缀
├── seq_length           序列长度
├── reset_cycle          重置周期（NONE/DAILY/MONTHLY/YEARLY）
└── current_seq          当前序列值
```

#### 4.2.3 配置继承机制

```
配置查找顺序：
1. 租户级配置（tenant_id = 当前租户）
2. 父租户配置（逐级向上查找）
3. 全局配置（tenant_id = null）

示例：
租户A(101)查找参数 "max_upload_size"
├── 101的配置 → 有则用
├── 10的配置 → 有则用
├── 1的配置 → 有则用
└── 全局配置 → 兜底
```

#### 4.2.4 功能开关

功能开关控制租户可使用的功能模块：

```
功能开关示例：
├── workflow.enabled         流程中心
├── message.enabled          消息中心
├── file_storage.enabled     文件管理
├── task_scheduling.enabled  任务调度
├── developer_portal.enabled 开发者门户
├── cross_tenant.enabled     跨租户流程
├── audit.export_enabled     审计导出
└── operation.billing.enabled 计量计费
```

---

