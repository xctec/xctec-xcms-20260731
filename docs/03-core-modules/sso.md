# 3.5 单点登录（SSO）设计

### 3.5.1 功能概述

为 XCMS 提供单点登录（SSO）能力，支持 OAuth2/OIDC 协议接入企业统一身份认证。用户通过 SSO 提供方认证后，无需在 XCMS 重复登录。同时预留钉钉/企微/飞书第三方登录扩展点。

### 3.5.2 设计目标

| 目标 | 说明 |
|------|------|
| 协议支持 | OAuth2 Authorization Code / OIDC |
| 多租户配置 | 支持全局 SSO 配置和租户级 SSO 配置（租户级覆盖全局） |
| 用户映射 | SSO 用户与本地用户自动映射（按邮箱/用户名/唯一标识） |
| 自动建号 | 首次 SSO 登录的用户自动创建本地用户（可配置开关） |
| 第三方扩展 | 预留钉钉/企微/飞书登录接口，通过配置启用 |
| 安全性 | state 参数防 CSRF，PKCE 预留，Token 加密存储 |

### 3.5.3 数据模型

```
identity_sso_config（SSO配置表，系统级/租户级）
├── id
├── tenant_id            租户ID（@TenantId，null为全局配置）
├── config_name          配置名称
├── protocol            协议（OAUTH2/OIDC）
├── client_id           客户端ID
├── client_secret       客户端密钥（加密存储）
├── issuer_url          签发方URL（OIDC 用，自动发现 .well-known/openid-configuration）
├── authorize_url       授权端点URL
├── token_url           令牌端点URL
├── userinfo_url        用户信息端点URL
├── redirect_url        回调URL（XCMS 的回调地址）
├── scopes              授权范围（如 openid profile email）
├── enabled             是否启用
├── auto_create_user    首次登录是否自动创建本地用户
├── user_mapping_field  用户映射字段（EMAIL/USERNAME/EMPLOYEE_NO）
├── default_role_id     自动建号时分配的默认角色
├── config              扩展配置（JSON：自定义参数映射等）
├── created_at
└── updated_at

identity_third_party_config（第三方登录配置表，预留）
├── id
├── tenant_id            租户ID（@TenantId）
├── provider            提供方（DINGTALK/WECOM/FEISHU）
├── app_id              应用ID
├── app_secret          应用密钥（加密存储）
├── enabled             是否启用
├── config              扩展配置（JSON）
├── created_at
└── updated_at

identity_sso_login_log（SSO登录日志表）
├── id
├── tenant_id            租户ID（@TenantId）
├── sso_config_id       SSO配置ID
├── external_user_id    外部用户ID（SSO提供方的用户标识）
├── local_user_id       本地用户ID（映射成功时）
├── login_result        登录结果（SUCCESS/USER_NOT_FOUND/MAPPING_FAILED/ERROR）
├── login_ip            登录IP
├── error_msg           错误信息
└── created_at
```

### 3.5.4 SSO 登录流程（OAuth2 Authorization Code）

```
用户                     XCMS 门户             SSO提供方
 │                         │                     │
 │── 访问 XCMS ──────────→│                     │
 │                         │  生成 state 存 session│
 │←── 重定向到SSO ─────────│  携带 client_id,     │
 │                         │  redirect_url, state │
 │── 登录认证 ──────────────────────────────────→│
 │                         │                     │
 │←── 授权码回调 ────────────────────────────────│
 │  redirect_url?code=xxx&state=yyy              │
 │── 携带授权码访问 ──────→│                     │
 │                         │ 校验 state           │
 │                         │── 换取Token ───────→│ POST token_url
 │                         │   client_id+secret  │  grant_type=authorization_code
 │                         │   + code            │
 │                         │←── Access Token ────│
 │                         │── 获取用户信息 ────→│ GET userinfo_url
 │                         │←── 用户信息 ────────│
 │                         │                     │
 │                         │ 用户映射：           │
 │                         │ 按 mapping_field 匹配│
 │                         │ ├─ 匹配成功 → 用本地用户│
 │                         │ └─ 匹配失败：        │
 │                         │    ├─ auto_create → 建号│
 │                         │    └─ 否则 → 拒绝登录 │
 │                         │                     │
 │                         │ 创建 XCMS 会话       │
 │                         │ 记录 SSO 登录日志    │
 │←── XCMS Token + 跳转 ──│                     │
```

### 3.5.5 用户映射策略

| 策略 | 说明 |
|------|------|
| EMAIL 映射 | SSO 用户信息的 email 与本地 user.email 匹配 |
| USERNAME 映射 | SSO 用户信息的 preferred_username 与本地 user.username 匹配 |
| EMPLOYEE_NO 映射 | SSO 用户信息的 employee_number 与本地 user.employee_no 匹配 |

**映射流程**：
1. 从 SSO userinfo 提取映射字段值
2. 按 `tenant_id + mapping_field` 查询本地用户
3. 匹配成功 → 更新用户最后登录信息 → 创建会话
4. 匹配失败 + `auto_create_user=true` → 创建本地用户（分配 `default_role_id`）→ 创建会话
5. 匹配失败 + `auto_create_user=false` → 拒绝登录，记录日志

### 3.5.6 第三方登录扩展（预留）

钉钉/企微/飞书登录通过独立配置 `identity_third_party_config` 实现，不默认开发，通过功能开关控制：

| 提供方 | 协议 | 扩展点 |
|--------|------|--------|
| 钉钉（DINGTALK） | OAuth2 | `DingTalkLoginHandler` |
| 企微（WECOM） | OAuth2 | `WeComLoginHandler` |
| 飞书（FEISHU） | OAuth2 | `FeishuLoginHandler` |

```java
package com.df4j.xctec.xcms.identity.api.sso;

/**
 * 第三方登录处理器接口。各提供方实现此接口。
 */
public interface ThirdPartyLoginHandler {
    String getProvider();                              // DINGTALK/WECOM/FEISHU
    String getAuthorizeUrl(ThirdPartyConfig config, String state);
    UserInfo exchangeToken(ThirdPartyConfig config, String code);
}
```

### 3.5.7 安全机制

| 机制 | 说明 |
|------|------|
| state 参数 | 每次授权请求生成随机 state，存入 session，回调时校验防 CSRF |
| PKCE 预留 | 接口预留 code_verifier/code_challenge 字段，Phase 4 启用 |
| client_secret 加密 | 使用 AES-256 加密存储，运行时解密 |
| Token 不持久化 | SSO Access Token 仅用于本次会话建立，不存 DB |
| 登录日志 | 所有 SSO 登录尝试（含失败）记录 `identity_sso_login_log` |

### 3.5.8 DDL

```sql
-- identity_sso_config
CREATE TABLE identity_sso_config (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id           BIGINT,
    config_name         VARCHAR(128) NOT NULL,
    protocol            VARCHAR(10)  NOT NULL,          -- OAUTH2/OIDC
    client_id           VARCHAR(255) NOT NULL,
    client_secret       VARCHAR(512) NOT NULL,          -- AES-256 加密
    issuer_url          VARCHAR(512),
    authorize_url       VARCHAR(512) NOT NULL,
    token_url           VARCHAR(512) NOT NULL,
    userinfo_url        VARCHAR(512) NOT NULL,
    redirect_url        VARCHAR(512) NOT NULL,
    scopes              VARCHAR(255) DEFAULT 'openid profile email',
    enabled             BOOLEAN      DEFAULT FALSE,
    auto_create_user    BOOLEAN      DEFAULT FALSE,
    user_mapping_field  VARCHAR(20)  DEFAULT 'EMAIL',  -- EMAIL/USERNAME/EMPLOYEE_NO
    default_role_id     BIGINT,
    config              TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- identity_third_party_config
CREATE TABLE identity_third_party_config (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    provider        VARCHAR(20)  NOT NULL,              -- DINGTALK/WECOM/FEISHU
    app_id          VARCHAR(255) NOT NULL,
    app_secret      VARCHAR(512) NOT NULL,             -- AES-256 加密
    enabled         BOOLEAN      DEFAULT FALSE,
    config          TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_third_party UNIQUE (tenant_id, provider)
);

-- identity_sso_login_log
CREATE TABLE identity_sso_login_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT,
    sso_config_id   BIGINT       NOT NULL,
    external_user_id VARCHAR(255),
    local_user_id   BIGINT,
    login_result    VARCHAR(20)  NOT NULL,              -- SUCCESS/USER_NOT_FOUND/MAPPING_FAILED/ERROR
    login_ip        VARCHAR(64),
    error_msg       TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sso_log_config FOREIGN KEY (sso_config_id) REFERENCES identity_sso_config (id)
);
CREATE INDEX idx_sso_log_time ON identity_sso_login_log (tenant_id, created_at);
```

### 3.5.9 模块归属

SSO 功能归属 `xcms-identity` 模块，扩展 `xcms-identity-impl`：
- API 接口：`com.df4j.xctec.xcms.identity.api.sso`
- 实现类：`com.df4j.xctec.xcms.identity.sso.service`
- 配置管理：SSO 配置作为 identity 管理面的子页面

---
