# 3 统一身份与认证\n
### 3.3 统一身份与认证

#### 3.3.1 功能概述

提供统一的用户管理、身份认证、单点登录（SSO）能力。支持多种认证协议，预留第三方登录和多端支持。

#### 3.3.2 数据模型

```
identity_user（用户表）
├── id
├── tenant_id           租户ID（@TenantId）
├── username            用户名（租户内唯一）
├── password            密码（加密存储）
├── real_name           真实姓名
├── employee_no         工号
├── email               邮箱
├── phone               手机号
├── avatar              头像
├── status              状态（ACTIVE/DISABLED/LOCKED）
├── lock_until          锁定截止时间
├── last_login_at       最后登录时间
├── last_login_ip       最后登录IP
├── password_changed_at 密码修改时间
├── created_at
└── updated_at

identity_role（角色表）
├── id
├── tenant_id           租户ID（@TenantId）
├── role_code           角色编码
├── role_name           角色名称
├── role_type           类型（SYSTEM_ADMIN/TENANT_ADMIN/BUSINESS）
├── description
├── parent_id           父角色（角色继承）
└── status

identity_user_role（用户角色关联表）
├── id
├── tenant_id           租户ID（@TenantId）
├── user_id             用户ID
├── role_id             角色ID
├── scope_type          范围类型（TENANT/DEPT/CUSTOM）
├── scope_value         范围值（部门ID等）
└── granted_by          授权人

identity_session（会话表）
├── id
├── tenant_id           租户ID（@TenantId）
├── user_id             用户ID
├── token               会话令牌
├── device_type         设备类型（PC/MOBILE/MINI_PROGRAM）
├── device_info         设备信息
├── login_ip            登录IP
├── login_at            登录时间
├── expire_at           过期时间
└── status              状态（ACTIVE/EXPIRED/REVOKED）

identity_sso_config（SSO配置表，系统级）
├── id
├── tenant_id           租户ID（可配全局或租户级）
├── protocol            协议（OAUTH2/OIDC/SAML）
├── client_id           客户端ID
├── client_secret       客户端密钥
├── issuer_url          签发方URL
├── authorize_url       授权URL
├── token_url           令牌URL
├── userinfo_url        用户信息URL
├── redirect_url        回调URL
├── enabled             是否启用
└── config              扩展配置（JSON）

identity_third_party_config（第三方登录配置，预留）
├── id
├── tenant_id
├── provider            提供方（DINGTALK/WECOM/FEISHU）
├── app_id              应用ID
├── app_secret          应用密钥
├── enabled
└── config
```

#### 3.3.3 认证流程

**SSO 登录流程（OAuth2/OIDC）**：

```
用户                     XCMS 门户             SSO提供方
 │                         │                     │
 │── 访问 XCMS ──────────→│                     │
 │                         │                     │
 │←── 重定向到SSO ─────────│                     │
 │                         │                     │
 │── 登录认证 ──────────────────────────────────→│
 │                         │                     │
 │←── 授权码回调 ────────────────────────────────│
 │                         │                     │
 │── 携带授权码访问 ──────→│                     │
 │                         │── 换取Token ───────→│
 │                         │←── Access Token ────│
 │                         │── 获取用户信息 ────→│
 │                         │←── 用户信息 ────────│
 │                         │                     │
 │                         │ 创建/更新本地用户     │
 │                         │ 创建会话             │
 │←── XCMS Token + 跳转 ──│                     │
```

**本地登录流程**：
1. 用户提交用户名密码
2. 校验密码（加密比对）
3. 检查账户状态（ACTIVE/LOCKED）
4. 检查登录策略（IP白名单、密码策略）
5. 创建会话，返回 Token
6. 记录登录日志

#### 3.3.4 登录策略

| 策略 | 说明 |
|------|------|
| 密码策略 | 最小长度、复杂度要求、定期更换 |
| 登录失败锁定 | 连续失败N次锁定账户 |
| IP白名单 | 限制可登录IP范围 |
| 会话超时 | 闲置超时自动登出 |
| 多端登录 | 允许/禁止多设备同时登录 |
| 强制改密 | 首次登录/密码过期强制修改 |

#### 3.3.5 多端支持

| 端 | 说明 |
|------|------|
| PC Web | 主端，完整功能 |
| 移动端 | H5/APP，核心功能 |
| 小程序 | 预留，轻量功能 |

预留第三方登录（钉钉/企微/飞书），通过配置启用，不默认实现。

---

