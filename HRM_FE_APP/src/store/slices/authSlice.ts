import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { UserInfo } from '../../types/auth';

export interface AuthState {
  accessToken: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
  roles: string[];
  permissions: string[];
  storeId: string | null;
}

const initialState: AuthState = {
  accessToken: null,
  user: null,
  isAuthenticated: false,
  roles: [],
  permissions: [],
  storeId: null,
};

/**
 * Map raw backend user → UserInfo.
 */
export const mapToUserInfo = (raw: any): UserInfo => ({
  id: raw.id,
  email: raw.email,
  avatarUrl: raw.avatarUrl || raw.avatar_url || undefined,
  roles: raw.roles || raw.role_codes || [],
  permissions: raw.permissions || raw.permission_codes || [],
  employee: raw.employee || null,
  isActive: raw.isActive !== undefined ? raw.isActive : raw.is_active,
  createdAt: raw.createdAt || raw.created_at,
  updatedAt: raw.updatedAt || raw.updated_at,
  createdBy: raw.createdBy || raw.created_by,
  updatedBy: raw.updatedBy || raw.updated_by,
  isDeleted: raw.isDeleted !== undefined ? raw.isDeleted : raw.is_deleted,
  deletedAt: raw.deletedAt || raw.deleted_at,
  deletedBy: raw.deletedBy || raw.deleted_by,
  storeId: raw.storeId || raw.store_id || raw.employee?.storeId || raw.employee?.store_id,
});

const authSlice = createSlice({
  name: 'auth',
  initialState, 
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{ accessToken: string; user: UserInfo }>
    ) => {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.isAuthenticated = true;
      state.roles = action.payload.user.roles || [];
      state.permissions = action.payload.user.permissions || [];
      state.storeId = action.payload.user.storeId || action.payload.user.employee?.storeId || null;
      
      console.log('[authSlice] setCredentials - isAuthenticated:', true);
      console.log('[authSlice] User:', action.payload.user.email);
      console.log('[authSlice] Roles:', state.roles);
      console.log('[authSlice] StoreID:', state.storeId);
      console.log('[authSlice] Permissions:', state.permissions);
    },
    setAccessTokenInStore: (state, action: PayloadAction<string>) => {
      state.accessToken = action.payload;
    },
    logout: (state) => {
      state.accessToken = null;
      state.user = null;
      state.isAuthenticated = false;
      state.roles = [];
      state.permissions = [];
      state.storeId = null;
    },
    restoreAuth: (
      state,
      action: PayloadAction<{ accessToken: string; user: UserInfo }>
    ) => {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.isAuthenticated = true;
      state.roles = action.payload.user.roles || [];
      state.permissions = action.payload.user.permissions || [];
      state.storeId = action.payload.user.storeId || action.payload.user.employee?.storeId || null;
      
      console.log('[authSlice] restoreAuth - isAuthenticated:', true);
      console.log('[authSlice] Restored user:', action.payload.user.email);
      console.log('[authSlice] Restored roles:', state.roles);
      console.log('[authSlice] Restored StoreID:', state.storeId);
      console.log('[authSlice] Restored permissions:', state.permissions);
    },
  },
});

export const { setCredentials, setAccessTokenInStore, logout, restoreAuth } = authSlice.actions;

export default authSlice.reducer;

import type { RootState } from '../index';

export const selectRoles = (state: RootState) => state.auth.roles;
export const selectPermissions = (state: RootState) => state.auth.permissions;
export const selectHasRole = (role: string) => (state: RootState) =>
  state.auth.roles.includes(role);
export const selectHasPermission = (permission: string) => (state: RootState) =>
  state.auth.permissions.includes(permission);
export const selectHasAnyRole = (roles: string[]) => (state: RootState) =>
  roles.some((role) => state.auth.roles.includes(role));
export const selectHasAllPermissions = (permissions: string[]) => (state: RootState) =>
  permissions.every((perm) => state.auth.permissions.includes(perm));
