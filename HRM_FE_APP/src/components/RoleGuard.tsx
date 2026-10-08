import React from 'react';
import { useRole } from '../hooks/useRole';

interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles?: string[];
  fallback?: React.ReactNode;
  requireManager?: boolean;
}

/**
 * A component that conditionally renders its children based on the user's role.
 */
export const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  allowedRoles,
  fallback = null,
  requireManager = false,
}) => {
  const { roles, isManager } = useRole();

  if (requireManager && !isManager) {
    return <>{fallback}</>;
  }

  if (allowedRoles && !allowedRoles.some((role) => roles.includes(role))) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};

export default RoleGuard;
