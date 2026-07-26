import type { ReactNode } from 'react';
import { useAuthStore } from '@/stores/auth';

interface CanProps {
  /** 需要的权限码，拥有其一即可（空数组视为通过） */
  permission?: string | string[];
  /** 需要的角色，拥有其一即可（空数组视为通过） */
  role?: string | string[];
  /** 无权限时的兜底渲染（默认不渲染任何内容） */
  fallback?: ReactNode;
  children: ReactNode;
}

/**
 * 按钮/片段级权限控制：无权限时不渲染 children（可指定 fallback）。
 * 示例：
 *   <Can permission="user:create"><Button>新增</Button></Can>
 *   <Can role="SYSTEM_ADMIN"><DangerAction/></Can>
 */
export function Can({ permission, role, fallback = null, children }: CanProps) {
  const hasPermission = useAuthStore((s) => s.hasPermission);
  const hasRole = useAuthStore((s) => s.hasRole);

  const perms = Array.isArray(permission) ? permission : permission ? [permission] : [];
  const roles = Array.isArray(role) ? role : role ? [role] : [];

  if (perms.length > 0 && !perms.some(hasPermission)) return <>{fallback}</>;
  if (roles.length > 0 && !roles.some(hasRole)) return <>{fallback}</>;
  return <>{children}</>;
}
