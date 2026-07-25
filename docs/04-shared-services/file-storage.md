# 3 文件存储中心

### 4.3 文件存储中心

#### 4.3.1 功能概述

提供统一的文件存储服务，支持多种存储后端，按租户隔离，支持配额管控。

#### 4.3.2 数据模型

```
file_metadata（文件元数据表）
├── id
├── tenant_id            租户ID（@TenantId）
├── file_name            文件名
├── file_path            存储路径
├── file_size            文件大小（字节）
├── file_type            文件类型（MIME类型）
├── storage_type         存储类型（LOCAL/OSS/MINIO）
├── storage_bucket       存储桶
├── storage_key          存储Key
├── md5                  文件MD5
├── owner_id             所属用户
├── folder_id            所属文件夹
├── share_token          分享令牌（可空）
├── share_expire         分享过期时间（可空）
├── status               状态（NORMAL/DELETED）
├── created_at
└── deleted_at

file_folder（文件夹表）
├── id
├── tenant_id            租户ID（@TenantId）
├── folder_name          文件夹名称
├── parent_id            父文件夹
├── owner_id             所属用户
├── path                 路径
└── created_at

file_storage_config（存储配置表，系统级）
├── id
├── storage_type         类型（LOCAL/OSS/MINIO）
├── config               配置（JSON）
├── enabled
└── is_default           是否默认
```

#### 4.3.3 核心功能

- **文件上传/下载**：支持单个/批量，支持分片上传大文件
- **文件预览**：图片/PDF/Office 文档在线预览
- **文件分享**：生成分享链接，支持密码保护和有效期
- **文件夹管理**：树形目录，支持移动/复制
- **回收站**：软删除，可恢复
- **配额管控**：上传时检查租户/个人配额
- **存储后端可切换**：本地/MinIO/对象存储，通过配置切换

---

