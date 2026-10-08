import { useSelector } from 'react-redux';
import { RootState } from '../store';

/**
 * Hook to get the current user's role and store information.
 */
export const useRole = () => {
  const { user, roles, storeId } = useSelector((state: RootState) => state.auth);

  const isManager = roles.includes('MANAGER') || roles.includes('ADMIN');
  const isEmployee = roles.includes('EMPLOYEE');
  
  // For managers, we often need to filter data by their assigned store
  const currentStoreId = storeId || user?.storeId || user?.employee?.storeId;

  return {
    isManager,
    isEmployee,
    roles,
    user,
    storeId: currentStoreId,
  };
};

/**
 * Helper function to check if a user has a specific role (non-hook version)
 */
export const hasRole = (roles: string[], targetRole: string | string[]) => {
  if (Array.isArray(targetRole)) {
    return targetRole.some((r) => roles.includes(r));
  }
  return roles.includes(targetRole);
};
