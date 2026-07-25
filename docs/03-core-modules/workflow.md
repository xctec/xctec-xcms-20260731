# 5 流程中心\n
### 3.5 流程中心

#### 3.5.1 功能概述

流程中心基于 Flowable BPM 引擎，提供完整的流程设计、发起、审批、监控能力。支持可视化拖拽设计流程、跨租户流程流转、流程模板继承。

#### 3.5.2 引擎集成

```
┌─────────────────────────────────────────────┐
│              单体应用                         │
│                                             │
│  ┌───────────────┐                          │
│  │  workflow 模块  │                          │
│  │               │                          │
│  │  ┌──────────┐ │                          │
│  │  │ Flowable │ │  ← 一个引擎实例，内嵌      │
│  │  │ Engine   │ │     服务所有租户           │
│  │  │(单例)     │ │                          │
│  │  └──────────┘ │                          │
│  └───────────────┘                          │
│                                             │
│  其他模块：tenant / auth / message / ...     │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│              一个数据库                        │
│                                             │
│  Flowable 表（自带 TENANT_ID_ 列）：           │
│  ├── ACT_RE_DEPLOYMENT   (TENANT_ID_)       │
│  ├── ACT_RE_PROCDEF      (TENANT_ID_)       │
│  ├── ACT_RU_EXECUTION    (TENANT_ID_)       │
│  ├── ACT_RU_TASK         (TENANT_ID_)       │
│  ├── ACT_RU_VARIABLE     (TENANT_ID_)       │
│  ├── ACT_HI_PROCINST     (TENANT_ID_)       │
│  ├── ACT_HI_TASKINST     (TENANT_ID_)       │
│  └── ...                                     │
│                                             │
│  租户ID映射：                                  │
│  ├── "global"     → 集团级流程模板            │
│  └── "{tenantId}" → 具体租户的流程实例/任务    │
└─────────────────────────────────────────────┘
```

**为什么共享引擎**：跨租户流程需要同一引擎内流转，共享引擎让跨租户查询和流转变得简单。

#### 3.5.3 流程模板管理

```
流程模板层级：
├── 全局模板（TENANT_ID_ = "global"）
│   ├── 集团管理员维护
│   ├── 所有租户可用
│   └── 子租户可继承并可覆盖
│
└── 租户模板（TENANT_ID_ = "{tenantId}"）
    ├── 本租户管理员维护
    ├── 仅本租户可用
    └── 可继承全局模板
```

#### 3.5.4 可视化流程设计器

```
┌─────────────────────────────────────────┐
│  流程设计器（拖拽式）                      │
│                                         │
│  节点类型：                               │
│  ├── ○ 开始节点                          │
│  ├── □ 审批节点（单人/多人/会签）          │
│  ├── □ 抄送节点                          │
│  ├── ◇ 条件网关（分支）                   │
│  ├── ◇ 并行网关（并行执行）                │
│  ├── □ 子流程节点（同引擎内）              │
│  ├── □ 桥接节点（预留，跨引擎联动）         │
│  └── ○ 结束节点                          │
│                                         │
│  每个节点可配置：                          │
│  ├── 参与租户：发起方/指定租户/上级/动态    │
│  ├── 角色：申请人/审批人/执行人            │
│  ├── 数据权限：全部/指定字段/仅摘要         │
│  ├── 操作权限：审批/驳回/转办/加签          │
│  ├── 超时策略：提醒/自动通过/自动驳回       │
│  └── 表单字段：可见/可编辑                 │
└─────────────────────────────────────────┘
```

#### 3.5.5 跨租户流程机制

**核心原则：流程归属权与任务参与权分离。**

```
流程实例（Process Instance）
  ├── 归属租户：发起方租户（数据属于该租户，不转移）
  ├── 任务1：分配给 发起方租户 用户 → 正常
  ├── 任务2：分配给 其他租户 用户 → 跨租户参与
  └── 任务3：分配给 上级租户 用户 → 跨租户参与
```

**三层控制**：

```
第一层：流程定义（模板设计时）
├── 每个节点配置参与租户范围
├── 配置数据权限（可见哪些字段）
└── 配置操作权限（可做什么操作）

第二层：任务分配（流程运行时）
├── 流程到达节点时，根据配置确定目标租户
├── 在目标租户内查找候选人
├── 分配任务并创建跨租户授权令牌
└── 令牌包含可访问的数据范围

第三层：数据访问控制（用户处理任务时）
├── 校验用户参与权
├── 按节点配置过滤可见数据
├── 限制操作范围
└── 全程审计
```

**跨租户待办查询**：

```java
// 用户的待办任务（跨租户查询）
List<Task> myTasks = taskService.createTaskQuery()
    .taskAssignee(userId)
    // 不加 tenantId 过滤 → 查所有租户中分配给该用户的任务
    .list();
// 结果包含：
// - 本租户任务（正常）
// - 跨租户任务（标记 CrossTenantFlag）
```

**跨租户任务处理**：

```java
public TaskDetail openTask(String taskId, Long userId) {
    Task task = taskService.createTaskQuery()
        .taskId(taskId)
        .taskAssignee(userId.toString())
        .singleResult();
    
    ProcessInstance instance = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(task.getProcessInstanceId())
        .singleResult();
    
    Long ownerTenantId = Long.valueOf(instance.getTenantId());
    Long userTenantId = TenantContext.getTenantId();
    
    if (!ownerTenantId.equals(userTenantId)) {
        // 跨租户任务
        NodeConfig config = getNodeConfig(task);
        Map<String, Object> formData = runtimeService
            .getVariables(task.getProcessInstanceId());
        // 按节点配置过滤可见字段
        Map<String, Object> filtered = filterByPermission(
            formData, config.getDataPermission());
        // 记录跨租户访问审计
        auditService.logCrossTenantAccess(
            userTenantId, ownerTenantId,
            task.getProcessInstanceId(), "read");
        return new TaskDetail(task, filtered, true);
    }
    
    // 同租户任务，正常返回
    return new TaskDetail(task, formData, false);
}
```

#### 3.5.6 流程干预

管理员可对运行中的流程实例进行干预：

| 操作 | 说明 | 权限 |
|------|------|------|
| 挂起 | 暂停流程执行 | 租户管理员 |
| 恢复 | 恢复已挂起的流程 | 租户管理员 |
| 终止 | 强制终止流程 | 租户管理员 |
| 转办 | 将任务转给他人 | 当前处理人/管理员 |
| 加签 | 增加审批人 | 当前处理人/管理员 |
| 减签 | 减少审批人 | 管理员 |
| 回退 | 回退到上一节点 | 管理员 |

#### 3.5.7 流程桥接（预留）

为独立部署的租户预留跨引擎流程联动能力，现阶段不实现：

```
共享引擎流程 ──桥接节点──→ 独立引擎流程
     │                          │
     │ 暂停等待                  │ 启动独立流程
     │                          │
     │ ←─── 回调结果 ──────────┘
     │ 恢复继续
     ▼
  继续执行
```

在流程设计器中预留 `BridgeNode`（桥接节点）类型，接口定义好，实现留空。

---

