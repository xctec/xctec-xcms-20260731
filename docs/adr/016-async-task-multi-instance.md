# ADR-016: 异步任务多实例就绪

- **Status**: Accepted（优先级降级，暂缓实施）
- **Date**: 2026-07-27

## Context

`TaskSchedulerEngine` 用 `ConcurrentHashMap<Long, ScheduledFuture>` 内存句柄，仅 leader 实例有运行态，宕机丢句柄、无跨实例接管（复盘 S9）。当前为单实例运行，单体阶段够用。

**优先级调整**：任务调度开发优先级降到最低，**暂不引入 XXL-Job/PowerJob**，不自研多实例调度。待真正拆分/集群时再按本 ADR 升级。

## Decision

未来就绪方案（暂缓实施）：运行态落 DB + 保留 DB 锁 leader 选举，不引入外部调度器。

1. **任务元数据已落 DB**（`task_definition`/`task_schedule`），保留
2. **运行态落 DB**：新增 `task_runs` 表（`task_id, started_at, finished_at, status, instance_id, result`），每次执行写记录；内存 `futures` 仅作本实例取消句柄，不作跨实例可见性依据
3. **leader 选举已 DB 化**（`TaskLockService` 行锁）：多实例只有 leader 调度，leader 宕机后 DB 锁释放，备实例接管。补健康探活 + 锁续约
4. **`@Async` 业务任务**：线程池 + `TaskDecorator` 传播 `ActorContext`（系统触发用 service principal，见 ADR-012）
5. **幂等**：`task_runs` 按 `(task_id, scheduled_fire_time)` 唯一约束防重复执行
6. **跨服务任务回调**：任务完成后发 `TaskCompletedEvent`（经 ADR-014 Outbox），业务模块订阅，不在引擎内直接调业务

## Alternatives

### 方案 B：引入 XXL-Job/PowerJob

- **优点**：现成可视化调度中心，多实例天然支持
- **缺点**：引入外部依赖与运维成本；当前任务规模小，过度设计

### 方案 C：保持单实例，不演进

- **优点**：零成本
- **缺点**：拆分/集群后 leader 宕机无接管，任务中断

## Consequences

### 正面（未来实施后）

- 多实例可接管，leader 宕机不丢任务
- 运行态可观测（`task_runs` 查历史）
- 不引入外部调度器，依赖可控

### 负面

- 暂缓实施期间，单实例宕机任务中断（单体阶段可接受）
- 未来实施需补 `task_runs` 表 + 健康探活 + 锁续约

### 缓解措施

- 当前单实例 + DB 锁 leader 已满足单体阶段
- 真正拆分/集群前按本 ADR 升级，预留 `task_runs` 表设计
- 优先级最低，不阻塞当前迭代
