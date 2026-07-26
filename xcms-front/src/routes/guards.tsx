import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';

interface GuardProps {
  children: ReactNode;
}

/** 已登录判断（无 token 跳登录） */
export function RequireAuth({ children }: GuardProps) {
  const token = useAuthStore((s) => s.token);
  const location = useLocation();
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}

/** 角色路由守卫：需拥有其中之一角色，否则跳 403 */
export function RequireRole({ roles, children }: GuardProps & { roles: string[] }) {
  const token = useAuthStore((s) => s.token);
  const hasRole = useAuthStore((s) => s.hasRole);
  const location = useLocation();
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (!roles.some(hasRole)) {
    return <Navigate to="/403" replace />;
  }
  return <>{children}</>;
}

/** 权限路由守卫：需拥有其中之一权限，否则跳 403 */
export function RequirePermission({ permissions, children }: GuardProps & { permissions: string[] }) {
  const token = useAuthStore((s) => s.token);
  const hasPermission = useAuthStore((s) => s.hasPermission);
  const location = useLocation();
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (!permissions.some(hasPermission)) {
    return <Navigate to="/403" replace />;
  }
  return <>{children}</>;
}
