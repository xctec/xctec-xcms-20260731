# API Design: Shared Services

> 消息中心 + 配置中心 + 文件存储 + 任务调度 + 审计中心 API 接口定义

---

## 1. message-api

### 1.1 MessageService

```java
package com.xcms.message.api;

public interface MessageService {

    /**
     * 发送消息（使用模板）
     */
    void send(MessageSendRequest request);

    /**
     * 批量发送消息
     */
    void sendBatch(List<MessageSendRequest> requests);

    /**
     * 获取站内信列表
     */
    PageResult<MessageDTO> listMessages(Long userId, MessageQuery query);

    /**
     * 获取未读消息数
     */
    UnreadCountDTO getUnreadCount(Long userId);

    /**
     * 标记消息已读
     */
    void markAsRead(List<Long> messageIds);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId, String msgType);
}
```

### 1.2 MessageTemplateService（管理面）

```java
package com.xcms.message.api;

public interface MessageTemplateService {
    MessageTemplateDTO createTemplate(TemplateCreateRequest request);
    MessageTemplateDTO updateTemplate(Long templateId, TemplateUpdateRequest request);
    void deleteTemplate(Long templateId);
    List<MessageTemplateDTO> listTemplates(String channelType);
}
```

### 1.3 DTO

```java
public class MessageSendRequest {
    private String templateCode;
    private Long receiverId;
    private List<Long> receiverIds;      // 批量
    private Map<String, Object> variables; // 模板变量
    private String channelType;           // 指定渠道，不填则用模板配置
}

public class MessageDTO {
    private Long id;
    private String templateCode;
    private String channelType;
    private String title;
    private String content;
    private Long senderId;
    private String status;     // PENDING/SENT/FAILED/READ
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}

public class MessageQuery extends PageQuery {
    private String msgType;    // FLOW/SYSTEM/BUSINESS
    private String status;     // UNREAD/READ
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

public class UnreadCountDTO {
    private int flow;          // 流程通知
    private int system;        // 系统通知
    private int business;      // 业务通知
    private int total;
}
```

---

## 2. config-api

### 2.1 ConfigService

```java
package com.xcms.config.api;

public interface ConfigService {

    /**
     * 获取参数值（带配置继承：租户级→父租户→全局）
     */
    String getParam(String key);

    /**
     * 获取参数值（带类型转换）
     */
    <T> T getParam(String key, Class<T> type, T defaultValue);

    /**
     * 设置参数
     */
    void setParam(String key, String value, ConfigType type);

    /**
     * 获取参数列表
     */
    List<ConfigParamDTO> listParams(String keyword);
}
```

### 2.2 FeatureFlagService

```java
package com.xcms.config.api;

public interface FeatureFlagService {

    /**
     * 检查功能是否启用（带继承）
     */
    boolean isEnabled(String featureCode);

    /**
     * 启用/禁用功能
     */
    void toggle(String featureCode, boolean enabled);

    /**
     * 获取功能配置
     */
    String getConfig(String featureCode);

    /**
     * 获取所有功能开关
     */
    List<FeatureFlagDTO> listFeatures();
}
```

### 2.3 DictionaryService

```java
package com.xcms.config.api;

public interface DictionaryService {

    /**
     * 获取字典项列表
     */
    List<DictItemDTO> getItems(String dictCode);

    /**
     * 获取字典项值
     */
    String getItemValue(String dictCode, String itemCode);

    /**
     * 获取树形字典
     */
    List<DictItemTreeDTO> getTree(String dictCode);
}
```

### 2.4 CodeRuleService

```java
package com.xcms.config.api;

public interface CodeRuleService {

    /**
     * 生成下一个编码
     */
    String nextCode(String ruleCode);

    /**
     * 预览下一个编码（不消耗序号）
     */
    String previewCode(String ruleCode);
}
```

---

## 3. file-api

### 3.1 FileService

```java
package com.xcms.file.api;

public interface FileService {

    /**
     * 上传文件
     */
    FileDTO upload(FileUploadRequest request);

    /**
     * 下载文件
     */
    FileDownloadResult download(Long fileId);

    /**
     * 获取文件信息
     */
    FileDTO getFileInfo(Long fileId);

    /**
     * 删除文件（软删除）
     */
    void deleteFile(Long fileId);

    /**
     * 创建分享链接
     */
    FileShareDTO createShareLink(Long fileId, ShareCreateRequest request);

    /**
     * 通过分享链接获取文件
     */
    FileDownloadResult downloadByShare(String shareToken, String password);

    /**
     * 获取文件列表
     */
    PageResult<FileDTO> listFiles(FileQuery query);

    /**
     * 获取存储使用情况
     */
    StorageUsageDTO getStorageUsage(Long userId);
}
```

### 3.2 FileFolderService

```java
package com.xcms.file.api;

public interface FileFolderService {
    FileFolderDTO createFolder(FolderCreateRequest request);
    void deleteFolder(Long folderId);
    void moveFile(Long fileId, Long targetFolderId);
    List<FileFolderDTO> listFolders(Long parentId);
}
```

### 3.3 DTO

```java
public class FileDTO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Long ownerId;
    private Long folderId;
    private String shareToken;
    private LocalDateTime shareExpire;
    private String status;
    private LocalDateTime createdAt;
}

public class FileUploadRequest {
    private MultipartFile file;
    private Long folderId;
}

public class FileQuery extends PageQuery {
    private Long folderId;
    private Long ownerId;
    private String fileName;
    private String fileType;
}

public class StorageUsageDTO {
    private Long usedBytes;
    private Long quotaBytes;
    private Double usagePercentage;
}
```

---

## 4. task-api

### 4.1 ScheduleTaskService

```java
package com.xcms.task.api;

public interface ScheduleTaskService {

    /**
     * 创建定时任务
     */
    ScheduleTaskDTO createTask(ScheduleTaskCreateRequest request);

    /**
     * 更新定时任务
     */
    ScheduleTaskDTO updateTask(Long taskId, ScheduleTaskUpdateRequest request);

    /**
     * 启用/禁用任务
     */
    void toggleTask(Long taskId, boolean enabled);

    /**
     * 手动触发任务
     */
    void triggerTask(Long taskId);

    /**
     * 删除任务
     */
    void deleteTask(Long taskId);

    /**
     * 获取任务列表
     */
    PageResult<ScheduleTaskDTO> listTasks(TaskQuery query);

    /**
     * 获取执行日志
     */
    PageResult<TaskExecutionLogDTO> getExecutionLogs(Long taskId, PageQuery query);
}
```

### 4.2 AsyncTaskService

```java
package com.xcms.task.api;

public interface AsyncTaskService {

    /**
     * 提交异步任务
     */
    String submitAsync(String taskType, Map<String, Object> payload);

    /**
     * 提交延时异步任务
     */
    String submitAsync(String taskType, Map<String, Object> payload, LocalDateTime executeAt);

    /**
     * 获取异步任务状态
     */
    AsyncTaskDTO getAsyncTaskStatus(String taskId);

    /**
     * 重试失败的异步任务
     */
    void retryAsyncTask(String taskId);
}
```

---

## 5. audit-api

### 5.1 AuditService（其他模块调用）

```java
package com.xcms.audit.api;

public interface AuditService {

    /**
     * 记录操作审计
     */
    void log(AuditLogRequest request);

    /**
     * 记录跨租户访问审计
     */
    void logCrossTenantAccess(Long fromTenantId, Long toTenantId, 
                              String resourceType, String resourceId, String action);

    /**
     * 记录登录审计
     */
    void logLogin(Long userId, Long tenantId, String ip, 
                  String deviceType, boolean success);

    /**
     * 记录数据访问审计
     */
    void logDataAccess(Long userId, Long tenantId, String resourceType, 
                       String resourceId, String action);
}
```

### 5.2 AuditQueryService（管理面调用）

```java
package com.xcms.audit.api;

public interface AuditQueryService {

    /**
     * 查询审计日志
     */
    PageResult<AuditLogDTO> queryLogs(AuditLogQuery query);

    /**
     * 查询跨租户访问审计
     */
    PageResult<AuditLogDTO> queryCrossTenantLogs(AuditLogQuery query);

    /**
     * 导出审计日志
     */
    String exportLogs(AuditLogQuery query);
}
```

### 5.3 AuditPolicyService（管理面调用）

```java
package com.xcms.audit.api;

public interface AuditPolicyService {
    AuditPolicyDTO createPolicy(AuditPolicyCreateRequest request);
    void updatePolicy(Long policyId, AuditPolicyUpdateRequest request);
    void deletePolicy(Long policyId);
    List<AuditPolicyDTO> listPolicies();
}
```

### 5.4 DTO

```java
public class AuditLogRequest {
    private String auditType;       // MANAGEMENT/CROSS_TENANT/LOGIN/DATA_ACCESS/BUSINESS_VISIBLE
    private Long userId;
    private String module;
    private String action;          // CREATE/UPDATE/DELETE/READ/EXPORT/LOGIN/LOGOUT
    private String resourceType;
    private String resourceId;
    private String description;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;   // 脱敏后
    private Integer responseStatus;
    private String ip;
    private String userAgent;
    private Long tokenId;
}

public class AuditLogDTO {
    private Long id;
    private Long tenantId;
    private String auditType;
    private Long userId;
    private String userName;
    private String module;
    private String action;
    private String resourceType;
    private String resourceId;
    private String description;
    private String ip;
    private LocalDateTime createdAt;
}

public class AuditLogQuery extends PageQuery {
    private String auditType;
    private Long userId;
    private String module;
    private String action;
    private String resourceType;
    private Long targetTenantId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

---

## 6. DDL

参见 [db/ddl/05-shared-services.sql](../../db/ddl/05-shared-services.sql)
