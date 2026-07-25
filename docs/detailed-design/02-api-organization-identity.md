# API Design: Organization + Identity

> 组织架构 + 身份认证 API 接口定义

## 1. org-api 接口定义

### 1.1 OrganizationService

```java
package com.xcms.org.api;

public interface OrganizationService {

    /**
     * 创建部门
     */
    DeptDTO createDepartment(DeptCreateRequest request);

    /**
     * 更新部门
     */
    DeptDTO updateDepartment(Long deptId, DeptUpdateRequest request);

    /**
     * 删除部门（软删除）
     */
    void deleteDepartment(Long deptId);

    /**
     * 获取部门详情
     */
    DeptDTO getDepartment(Long deptId);

    /**
     * 获取部门树（当前租户）
     */
    List<DeptTreeDTO> getDepartmentTree();

    /**
     * 获取子部门列表
     */
    List<DeptDTO> listSubDepartments(Long parentId);

    /**
     * 移动部门（变更父部门）
     */
    void moveDepartment(Long deptId, Long newParentId);

    /**
     * 获取部门下的人员
     */
    PageResult<UserBriefDTO> listDepartmentUsers(Long deptId, PageQuery query);
}
```

### 1.2 PositionService

```java
package com.xcms.org.api;

public interface PositionService {

    PositionDTO createPosition(PositionCreateRequest request);
    PositionDTO updatePosition(Long positionId, PositionUpdateRequest request);
    void deletePosition(Long positionId);
    List<PositionDTO> listPositionsByDept(Long deptId);
}
```

### 1.3 UserOrgService（组织关系管理）

```java
package com.xcms.org.api;

public interface UserOrgService {

    /**
     * 分配用户到部门/岗位
     */
    void assignUserToPosition(Long userId, Long deptId, Long positionId, boolean isPrimary);

    /**
     * 从部门/岗位移除用户
     */
    void removeUserFromPosition(Long userId, Long positionId);

    /**
     * 获取用户的部门/岗位列表
     */
    List<UserPositionDTO> getUserPositions(Long userId);

    /**
     * 获取用户的主部门
     */
    DeptDTO getUserPrimaryDept(Long userId);

    /**
     * 获取用户所属的所有部门路径（用于数据权限）
     */
    List<String> getUserDeptPaths(Long userId);
}
```

### 1.4 UserGroupService

```java
package com.xcms.org.api;

public interface UserGroupService {
    UserGroupDTO createGroup(UserGroupCreateRequest request);
    void deleteGroup(Long groupId);
    void addMembers(Long groupId, List<Long> userIds);
    void removeMembers(Long groupId, List<Long> userIds);
    List<UserBriefDTO> listGroupMembers(Long groupId);
    List<UserGroupDTO> listGroups();
}
```

### 1.5 DTO 定义

```java
package com.xcms.org.api.dto;

public class DeptDTO {
    private Long id;
    private Long tenantId;
    private String deptCode;
    private String deptName;
    private Long parentId;
    private Integer level;
    private String path;
    private Long managerId;
    private String managerName;
    private Integer sortOrder;
    private String status;
}

public class DeptTreeDTO {
    private Long id;
    private String deptName;
    private String deptCode;
    private Long managerId;
    private List<DeptTreeDTO> children;
}

public class DeptCreateRequest {
    private String deptCode;
    private String deptName;
    private Long parentId;
    private Long managerId;
    private Integer sortOrder;
}

public class DeptUpdateRequest {
    private String deptName;
    private Long managerId;
    private Integer sortOrder;
}

public class PositionDTO {
    private Long id;
    private Long deptId;
    private String positionCode;
    private String positionName;
    private Integer level;
    private Integer sortOrder;
}

public class UserPositionDTO {
    private Long id;
    private Long userId;
    private Long deptId;
    private String deptName;
    private Long positionId;
    private String positionName;
    private Boolean isPrimary;
}

public class UserBriefDTO {
    private Long id;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
}

public class UserGroupDTO {
    private Long id;
    private String groupName;
    private String description;
    private String type;
    private Integer memberCount;
}
```

### 1.6 事件定义

```java
package com.xcms.org.api.event;

public class DepartmentCreatedEvent implements Serializable {
    private Long deptId;
    private Long tenantId;
    private Long parentId;
    private String path;
}

public class DepartmentMovedEvent implements Serializable {
    private Long deptId;
    private Long oldParentId;
    private Long newParentId;
    private String oldPath;
    private String newPath;
}
```

---

## 2. identity-api 接口定义

### 2.1 UserService

```java
package com.xcms.identity.api;

public interface UserService {

    /**
     * 创建用户
     */
    UserDTO createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    UserDTO updateUser(Long userId, UserUpdateRequest request);

    /**
     * 获取用户详情
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    UserDTO getByUsername(String username);

    /**
     * 获取当前登录用户
     */
    UserDTO getCurrentUser();

    /**
     * 用户列表（分页）
     */
    PageResult<UserDTO> listUsers(UserQuery query);

    /**
     * 启用/停用/锁定用户
     */
    void changeUserStatus(Long userId, UserStatus status);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 批量导入用户
     */
    BatchImportResult batchImportUsers(List<UserCreateRequest> users);

    /**
     * 删除用户（软删除）
     */
    void deleteUser(Long userId);
}
```

### 2.2 RoleService

```java
package com.xcms.identity.api;

public interface RoleService {

    RoleDTO createRole(RoleCreateRequest request);
    RoleDTO updateRole(Long roleId, RoleUpdateRequest request);
    void deleteRole(Long roleId);
    RoleDTO getRole(Long roleId);
    List<RoleDTO> listRoles(RoleQuery query);

    /** 分配角色给用户 */
    void assignRoleToUser(Long userId, Long roleId, RoleScope scope, String scopeValue);

    /** 移除用户角色 */
    void removeRoleFromUser(Long userId, Long roleId, RoleScope scope, String scopeValue);

    /** 获取用户的角色列表 */
    List<RoleDTO> getUserRoles(Long userId);

    /** 获取角色下的用户列表 */
    PageResult<UserBriefDTO> getRoleUsers(Long roleId, PageQuery query);
}
```

### 2.3 AuthService（认证服务）

```java
package com.xcms.identity.api;

public interface AuthService {

    /**
     * 本地登录（用户名密码）
     */
    LoginResult login(LoginRequest request);

    /**
     * SSO 登录回调
     */
    LoginResult ssoCallback(String code, String state);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 验证 Token 有效性
     */
    TokenInfo validateToken(String token);

    /**
     * 刷新 Token
     */
    LoginResult refreshToken(String refreshToken);

    /**
     * 获取当前会话
     */
    SessionDTO getCurrentSession();

    /**
     * 获取用户在线会话列表
     */
    List<SessionDTO> getUserSessions(Long userId);

    /**
     * 强制下线
     */
    void revokeSession(String token);
}
```

### 2.4 DTO 定义

```java
package com.xcms.identity.api.dto;

public class UserDTO {
    private Long id;
    private Long tenantId;
    private String username;
    private String realName;
    private String employeeNo;
    private String email;
    private String phone;
    private String avatar;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private List<RoleDTO> roles;
}

public class UserCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String employeeNo;
    private String email;
    private String phone;
    private List<Long> roleIds;
    private Long deptId;
    private Long positionId;
}

public class UserUpdateRequest {
    private String realName;
    private String email;
    private String phone;
    private String avatar;
}

public class UserQuery extends PageQuery {
    private String keyword;
    private Long deptId;
    private UserStatus status;
}

public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleType;    // SYSTEM_ADMIN/TENANT_ADMIN/BUSINESS
    private String description;
    private Long parentId;
}

public class RoleCreateRequest {
    private String roleCode;
    private String roleName;
    private String roleType;
    private String description;
    private Long parentId;
}

public class LoginRequest {
    private String username;
    private String password;
    private Long tenantId;
    private String deviceType;  // PC/MOBILE/MINI_PROGRAM
}

public class LoginResult {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserDTO user;
}

public class TokenInfo {
    private Long userId;
    private Long tenantId;
    private String username;
    private LocalDateTime expireAt;
    private boolean valid;
}

public class SessionDTO {
    private Long id;
    private Long userId;
    private String deviceType;
    private String deviceInfo;
    private String loginIp;
    private LocalDateTime loginAt;
    private LocalDateTime expireAt;
    private String status;
}

public class BatchImportResult {
    private int total;
    private int success;
    private int failed;
    private List<String> errors;
}

public enum UserStatus {
    ACTIVE, DISABLED, LOCKED
}

public enum RoleScope {
    TENANT, DEPT, CUSTOM
}
```

### 2.5 事件定义

```java
package com.xcms.identity.api.event;

public class UserCreatedEvent implements Serializable {
    private Long userId;
    private Long tenantId;
    private String username;
    private String realName;
}

public class UserDeletedEvent implements Serializable {
    private Long userId;
    private Long tenantId;
}

public class UserLoginEvent implements Serializable {
    private Long userId;
    private Long tenantId;
    private String ip;
    private String deviceType;
    private LocalDateTime loginAt;
    private boolean success;
}

public class RoleAssignedEvent implements Serializable {
    private Long userId;
    private Long roleId;
    private Long tenantId;
    private String scopeType;
    private String scopeValue;
}
```

---

## 3. DDL

参见 [db/ddl/02-organization-identity.sql](../../db/ddl/02-organization-identity.sql)
