import type { RouteMenuItem } from '@/types/menu';

/**
 * 管理面菜单树（顶层为分组/目录，children 为菜单项）。
 * 每项可携带 permission（权限码）与 roles（角色）用于前端二次校验；
 * 后端实际返回时已完成按权限过滤，此处仅作为演示数据。
 */
export const mockAdminMenus: RouteMenuItem[] = [
  {
    key: 'group-tenant',
    label: '租户与组织',
    icon: 'Building2',
    path: '',
    component: '',
    sort: 1,
    visible: true,
    children: [
      { key: 'tenant', label: '租户管理', icon: 'Building2', path: '/admin/tenant', component: 'admin/TenantList', sort: 1, visible: true, permission: 'tenant:view' },
      { key: 'organization', label: '组织架构', icon: 'Network', path: '/admin/organization', component: 'admin/Organization', sort: 2, visible: true, permission: 'organization:view' },
      { key: 'user', label: '用户管理', icon: 'Users', path: '/admin/user', component: 'admin/UserList', sort: 3, visible: true, permission: 'user:view' },
      { key: 'btn-user-create', label: '新建用户', path: '', sort: 0, visible: true, permission: 'user:create', type: 'BUTTON' },
      { key: 'btn-user-reset-pwd', label: '重置密码', path: '', sort: 0, visible: true, permission: 'user:reset-pwd', type: 'BUTTON' },
    ],
  },
  {
    key: 'group-security',
    label: '安全与权限',
    icon: 'Shield',
    path: '',
    component: '',
    sort: 2,
    visible: true,
    children: [
      { key: 'permission', label: '权限管理', icon: 'Shield', path: '/admin/permission', component: 'admin/Permission', sort: 1, visible: true, permission: 'permission:view' },
    ],
  },
  {
    key: 'group-biz',
    label: '业务流程',
    icon: 'Workflow',
    path: '',
    component: '',
    sort: 3,
    visible: true,
    children: [
      { key: 'workflow', label: '流程管理', icon: 'Workflow', path: '/admin/workflow', component: 'admin/Workflow', sort: 1, visible: true, permission: 'workflow:view' },
    ],
  },
  {
    key: 'group-ops',
    label: '运营',
    icon: 'BarChart3',
    path: '',
    component: '',
    sort: 4,
    visible: true,
    children: [
      { key: 'operation', label: '运营看板', icon: 'BarChart3', path: '/admin/operation', component: 'admin/Operation', sort: 1, visible: true, permission: 'operation:view' },
      { key: 'message', label: '消息中心', icon: 'Bell', path: '/admin/message', component: 'admin/Message', sort: 2, visible: true, permission: 'message:view' },
      { key: 'file', label: '文件管理', icon: 'FolderOpen', path: '/admin/file', component: 'admin/FileManagement', sort: 3, visible: true, permission: 'file:view' },
    ],
  },
  {
    key: 'group-system',
    label: '系统',
    icon: 'Settings',
    path: '',
    component: '',
    sort: 5,
    visible: true,
    children: [
      { key: 'config', label: '配置管理', icon: 'Settings', path: '/admin/config', component: 'admin/Config', sort: 1, visible: true, permission: 'config:view' },
      { key: 'task', label: '任务调度', icon: 'Clock', path: '/admin/task', component: 'admin/TaskSchedule', sort: 2, visible: true, permission: 'task:view' },
      { key: 'audit', label: '审计日志', icon: 'FileText', path: '/admin/audit', component: 'admin/Audit', sort: 3, visible: true, permission: 'audit:view' },
    ],
  },
];

/** 业务面菜单树 */
export const mockPortalMenus: RouteMenuItem[] = [
  {
    key: 'group-workbench',
    label: '工作台',
    icon: 'LayoutDashboard',
    path: '',
    component: '',
    sort: 1,
    visible: true,
    children: [
      { key: 'workbench', label: '我的工作台', icon: 'LayoutDashboard', path: '/portal/workbench', component: 'portal/Workbench', sort: 1, visible: true, permission: 'workbench:view' },
    ],
  },
  {
    key: 'group-portal-biz',
    label: '业务流程',
    icon: 'Workflow',
    path: '',
    component: '',
    sort: 2,
    visible: true,
    children: [
      { key: 'portal-workflow', label: '流程办理', icon: 'Workflow', path: '/portal/workflow', component: 'portal/Workflow', sort: 1, visible: true, permission: 'portal:workflow:view' },
    ],
  },
  {
    key: 'group-portal-ops',
    label: '信息中心',
    icon: 'Bell',
    path: '',
    component: '',
    sort: 3,
    visible: true,
    children: [
      { key: 'portal-message', label: '消息中心', icon: 'Bell', path: '/portal/message', component: 'portal/Message', sort: 1, visible: true, permission: 'portal:message:view' },
      { key: 'portal-file', label: '文件管理', icon: 'FolderOpen', path: '/portal/file', component: 'portal/FileManagement', sort: 2, visible: true, permission: 'portal:file:view' },
    ],
  },
  {
    key: 'group-profile',
    label: '个人中心',
    icon: 'User',
    path: '',
    component: '',
    sort: 4,
    visible: true,
    children: [
      { key: 'profile', label: '个人资料', icon: 'User', path: '/portal/profile', component: 'portal/Profile', sort: 1, visible: true, permission: 'portal:profile:view' },
    ],
  },
];
