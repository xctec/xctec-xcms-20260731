# 1 消息中心

### 4.1 消息中心

#### 4.1.1 功能概述

提供统一的消息服务，支持站内信、通知消息、邮件、短信、Webhook 等多渠道消息发送。各业务模块通过消息中心统一发送通知。

#### 4.1.2 数据模型

```
msg_channel（消息渠道配置表，系统级）
├── id
├── channel_type         类型（IN_APP/EMAIL/SMS/WEBHOOK）
├── channel_name         渠道名称
├── config               渠道配置（JSON，如SMTP服务器、短信网关等）
├── enabled              是否启用
└── tenant_scope         适用范围（GLOBAL/TENANT_SPECIFIC）

msg_template（消息模板表）
├── id
├── tenant_id            租户ID（@TenantId，global为系统级）
├── template_code        模板编码
├── template_name        模板名称
├── channel_type         渠道类型
├── title                标题模板
├── content              内容模板（支持变量占位符）
├── variables            变量定义（JSON）
└── status

msg_record（消息发送记录表）
├── id
├── tenant_id            租户ID（@TenantId）
├── template_code        使用的模板
├── channel_type         渠道
├── sender_id            发送人
├── receiver_id          接收人
├── receiver_info        接收信息（邮箱/手机号等）
├── title                标题
├── content              内容
├── status               状态（PENDING/SENT/FAILED/READ）
├── sent_at              发送时间
├── read_at              阅读时间
├── retry_count          重试次数
└── error_msg            错误信息

msg_user_setting（用户消息偏好表）
├── id
├── tenant_id            租户ID（@TenantId）
├── user_id              用户ID
├── msg_type             消息类型（FLOW/SYSTEM/BUSINESS）
├── channel_type         渠道
├── enabled              是否接收
└── quiet_hours          免打扰时段（JSON）
```

#### 4.1.3 核心功能

**多渠道发送**：

```
业务模块调用消息中心
    │
    ├── 站内信 → 写入 msg_record，用户在工作台查看
    ├── 邮件   → 调用 SMTP 发送
    ├── 短信   → 调用短信网关 API
    └── Webhook → POST 到配置的 URL
```

**消息模板**：
- 支持变量占位符：`{{userName}}`、`{{processName}}`、`{{taskName}}`
- 系统级模板 + 租户级模板
- 模板支持多渠道（同一消息可发多个渠道）

**用户偏好设置**：
- 按消息类型设置接收渠道
- 免打扰时段
- 消息分类订阅/退订

**消息统计**：
- 发送量、到达率、阅读率
- 按渠道、按类型统计
- 异常消息重发

---

