import { createBrowserRouter, Navigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import AdminLayout from '@/layouts/AdminLayout';
import PortalLayout from '@/layouts/PortalLayout';
import AuthLayout from '@/layouts/AuthLayout';
import LoginPage from '@/pages/auth/Login';
import RegisterPage from '@/pages/auth/Register';
import NotFoundPage from '@/pages/error/404';
import ForbiddenPage from '@/pages/error/403';
// Portal pages
import WorkbenchPage from '@/pages/portal/Workbench';
import PortalWorkflowPage from '@/pages/portal/Workflow';
import PortalMessagePage from '@/pages/portal/Message';
import PortalFilePage from '@/pages/portal/FileManagement';
import ProfilePage from '@/pages/portal/Profile';
// Admin pages
import TenantListPage from '@/pages/admin/TenantList';
import OrganizationPage from '@/pages/admin/Organization';
import UserListPage from '@/pages/admin/UserList';
import PermissionPage from '@/pages/admin/Permission';
import WorkflowPage from '@/pages/admin/Workflow';
import ConfigPage from '@/pages/admin/Config';
import MessagePage from '@/pages/admin/Message';
import FileManagementPage from '@/pages/admin/FileManagement';
import TaskSchedulePage from '@/pages/admin/TaskSchedule';
import AuditPage from '@/pages/admin/Audit';
import OperationPage from '@/pages/admin/Operation';
import type { JSX } from 'react';

function RequireAuth({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((s) => !!s.token);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
  {
    path: '/admin',
    element: <RequireAuth><AdminLayout /></RequireAuth>,
    children: [
      { index: true, element: <Navigate to="/admin/tenant" replace /> },
      { path: 'tenant', element: <TenantListPage /> },
      { path: 'organization', element: <OrganizationPage /> },
      { path: 'user', element: <UserListPage /> },
      { path: 'permission', element: <PermissionPage /> },
      { path: 'workflow', element: <WorkflowPage /> },
      { path: 'config', element: <ConfigPage /> },
      { path: 'message', element: <MessagePage /> },
      { path: 'file', element: <FileManagementPage /> },
      { path: 'task', element: <TaskSchedulePage /> },
      { path: 'audit', element: <AuditPage /> },
      { path: 'operation', element: <OperationPage /> },
    ],
  },
  {
    path: '/portal',
    element: <RequireAuth><PortalLayout /></RequireAuth>,
    children: [
      { index: true, element: <Navigate to="/portal/workbench" replace /> },
      { path: 'workbench', element: <WorkbenchPage /> },
      { path: 'workflow', element: <PortalWorkflowPage /> },
      { path: 'message', element: <PortalMessagePage /> },
      { path: 'file', element: <PortalFilePage /> },
      { path: 'profile', element: <ProfilePage /> },
    ],
  },
  { path: '/403', element: <ForbiddenPage /> },
  { path: '/404', element: <NotFoundPage /> },
  { path: '/', element: <Navigate to="/admin" replace /> },
  { path: '*', element: <NotFoundPage /> },
]);
