import { createBrowserRouter, Navigate } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { useAuthStore } from '@/stores/auth';
import AdminLayout from '@/layouts/AdminLayout';
import PortalLayout from '@/layouts/PortalLayout';
import AuthLayout from '@/layouts/AuthLayout';
import type { JSX } from 'react';

// 懒加载页面组件
const LoginPage = lazy(() => import('@/pages/auth/Login'));
const RegisterPage = lazy(() => import('@/pages/auth/Register'));
const ForgotPasswordPage = lazy(() => import('@/pages/auth/ForgotPassword'));
const NotFoundPage = lazy(() => import('@/pages/error/404'));
const ForbiddenPage = lazy(() => import('@/pages/error/403'));

// Portal pages
const WorkbenchPage = lazy(() => import('@/pages/portal/Workbench'));
const PortalWorkflowPage = lazy(() => import('@/pages/portal/Workflow'));
const PortalMessagePage = lazy(() => import('@/pages/portal/Message'));
const PortalFilePage = lazy(() => import('@/pages/portal/FileManagement'));
const ProfilePage = lazy(() => import('@/pages/portal/Profile'));

// Admin pages
const TenantListPage = lazy(() => import('@/pages/admin/TenantList'));
const OrganizationPage = lazy(() => import('@/pages/admin/Organization'));
const UserListPage = lazy(() => import('@/pages/admin/UserList'));
const PermissionPage = lazy(() => import('@/pages/admin/Permission'));
const WorkflowPage = lazy(() => import('@/pages/admin/Workflow'));
const ConfigPage = lazy(() => import('@/pages/admin/Config'));
const MessagePage = lazy(() => import('@/pages/admin/Message'));
const FileManagementPage = lazy(() => import('@/pages/admin/FileManagement'));
const TaskSchedulePage = lazy(() => import('@/pages/admin/TaskSchedule'));
const AuditPage = lazy(() => import('@/pages/admin/Audit'));
const OperationPage = lazy(() => import('@/pages/admin/Operation'));

function RequireAuth({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((s) => !!s.token);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

function Lazy({ children }: { children: JSX.Element }) {
  return <Suspense fallback={<div className="flex h-40 items-center justify-center text-sm text-gray-400">加载中...</div>}>{children}</Suspense>;
}

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <Lazy><LoginPage /></Lazy> },
      { path: '/register', element: <Lazy><RegisterPage /></Lazy> },
      { path: '/forgot-password', element: <Lazy><ForgotPasswordPage /></Lazy> },
    ],
  },
  {
    path: '/admin',
    element: <RequireAuth><AdminLayout /></RequireAuth>,
    children: [
      { index: true, element: <Navigate to="/admin/tenant" replace /> },
      { path: 'tenant', element: <Lazy><TenantListPage /></Lazy> },
      { path: 'organization', element: <Lazy><OrganizationPage /></Lazy> },
      { path: 'user', element: <Lazy><UserListPage /></Lazy> },
      { path: 'permission', element: <Lazy><PermissionPage /></Lazy> },
      { path: 'workflow', element: <Lazy><WorkflowPage /></Lazy> },
      { path: 'config', element: <Lazy><ConfigPage /></Lazy> },
      { path: 'message', element: <Lazy><MessagePage /></Lazy> },
      { path: 'file', element: <Lazy><FileManagementPage /></Lazy> },
      { path: 'task', element: <Lazy><TaskSchedulePage /></Lazy> },
      { path: 'audit', element: <Lazy><AuditPage /></Lazy> },
      { path: 'operation', element: <Lazy><OperationPage /></Lazy> },
    ],
  },
  {
    path: '/portal',
    element: <RequireAuth><PortalLayout /></RequireAuth>,
    children: [
      { index: true, element: <Navigate to="/portal/workbench" replace /> },
      { path: 'workbench', element: <Lazy><WorkbenchPage /></Lazy> },
      { path: 'workflow', element: <Lazy><PortalWorkflowPage /></Lazy> },
      { path: 'message', element: <Lazy><PortalMessagePage /></Lazy> },
      { path: 'file', element: <Lazy><PortalFilePage /></Lazy> },
      { path: 'profile', element: <Lazy><ProfilePage /></Lazy> },
    ],
  },
  { path: '/403', element: <Lazy><ForbiddenPage /></Lazy> },
  { path: '/404', element: <Lazy><NotFoundPage /></Lazy> },
  { path: '/', element: <Navigate to="/admin" replace /> },
  { path: '*', element: <Lazy><NotFoundPage /></Lazy> },
]);
