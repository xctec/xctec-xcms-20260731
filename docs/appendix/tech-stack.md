# Tech Stack

> XCMS 技术栈汇总

| 项 | 选型 |
|---|---|
| 语言 | Java 21+ |
| 框架 | Spring Boot 4 (Spring Framework 7) |
| ORM | JPA (Hibernate 7) |
| 查询构建 | QueryDSL 7+ |
| 对象映射 | MapStruct 1.6+ |
| 工具 | Lombok |
| 多租户 | @TenantId + Specification + QueryDSL |
| 流程引擎 | Flowable (Discriminator 多租户) |
| 数据库 | 数据库无关（JPA抽象） |
| 缓存 | Redis |
| 构建 | Maven/Gradle 多模块 |
| 部署 | JAR + 虚拟机/物理机 |
