import { configureStore } from '@reduxjs/toolkit';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { restoreAccessToken, clearTokens, getAccessToken } from '../services/tokenStorage';
import { authApi } from './api/authApi';
import { userApi } from './api/userApi';
import { attendanceApi } from './api/attendanceApi';
import { leaveApi } from './api/leaveApi';
import { payrollApi } from './api/payrollApi';
import { employeeApi } from './api/employeeApi';
import { departmentApi } from './api/departmentApi';
import { workshiftApi } from './api/workshiftApi';
import { notificationApi } from './api/notificationApi';
import { contractApi } from './api/contractApi';
import { degreeApi } from './api/degreeApi';
import authReducer, { restoreAuth, logout, mapToUserInfo } from './slices/authSlice';
import type { UserInfo } from './slices/authSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]: authApi.reducer,
    [userApi.reducerPath]: userApi.reducer,
    [attendanceApi.reducerPath]: attendanceApi.reducer,
    [leaveApi.reducerPath]: leaveApi.reducer,
    [payrollApi.reducerPath]: payrollApi.reducer,
    [employeeApi.reducerPath]: employeeApi.reducer,
    [departmentApi.reducerPath]: departmentApi.reducer,
    [workshiftApi.reducerPath]: workshiftApi.reducer,
    [notificationApi.reducerPath]: notificationApi.reducer,
    [contractApi.reducerPath]: contractApi.reducer,
    [degreeApi.reducerPath]: degreeApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware()
      .concat(authApi.middleware)
      .concat(userApi.middleware)
      .concat(attendanceApi.middleware)
      .concat(leaveApi.middleware)
      .concat(payrollApi.middleware)
      .concat(employeeApi.middleware)
      .concat(departmentApi.middleware)
      .concat(workshiftApi.middleware)
      .concat(notificationApi.middleware)
      .concat(contractApi.middleware)
      .concat(degreeApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

// Initialize auth state from SecureStore + AsyncStorage
export const initializeAuth = async () => {
  try {
    console.log('[Store] Initializing auth from SecureStore...');
    
    // Restore access token from SecureStore → memory
    const authToken = await restoreAccessToken();
    // User info is non-sensitive, kept in AsyncStorage
    const userInfo = await AsyncStorage.getItem('userInfo');
    
    console.log('[Store] Auth data check:', {
      hasToken: !!authToken,
      hasUserInfo: !!userInfo,
    });
    
    if (authToken && userInfo) {
      try {
        const rawUser = JSON.parse(userInfo);
        // Map backend format (snake_case) → UserInfo (camelCase)
        const user = mapToUserInfo(rawUser);
        store.dispatch(restoreAuth({ accessToken: authToken, user }));
        console.log('[Store] Auth restored from storage for user:', user.email);
        console.log('[Store] Restored roles/perms:', { roles: user.roles, perms: user.permissions });

        // Proactively validate token & refresh user data in background.
        // If access token expired, baseQuery will auto-refresh via refresh token.
        store.dispatch(
          authApi.endpoints.getUserProfile.initiate(undefined, { forceRefetch: true })
        )
          .unwrap()
          .then(async (result: any) => {
            if (result?.success && result?.data) {
              const freshUser = mapToUserInfo(result.data);
              const currentToken = getAccessToken() || authToken;
              
              store.dispatch(restoreAuth({ accessToken: currentToken, user: freshUser }));
              
              // Persist the correctly-mapped user info
              await AsyncStorage.setItem('userInfo', JSON.stringify(freshUser));
              
              console.log('[Store] User profile validated & refreshed for:', freshUser.email);
              console.log('[Store] Fresh roles/perms:', { roles: freshUser.roles, perms: freshUser.permissions });
            } else {
              console.warn('[Store] Profile refresh returned unsuccessful or empty data:', result);
            }
          })
          .catch((err: any) => {
            console.warn('[Store] Token validation failed (may need re-login):', err);
          });
      } catch (parseError) {
        console.error('[Store] Error parsing user info:', parseError);
        // Clear invalid data
        await clearTokens();
        await AsyncStorage.removeItem('userInfo');
      }
    } else {
      console.log('[Store] No auth data found - user is not authenticated');
    }
  } catch (error) {
    console.error('[Store] Error restoring auth:', error);
  }
};

// Reset all RTK Query cache
export const resetAllApiStates = () => {
  store.dispatch(authApi.util.resetApiState());
  store.dispatch(userApi.util.resetApiState());
  store.dispatch(attendanceApi.util.resetApiState());
  store.dispatch(leaveApi.util.resetApiState());
  store.dispatch(payrollApi.util.resetApiState());
  store.dispatch(employeeApi.util.resetApiState());
  store.dispatch(departmentApi.util.resetApiState());
  store.dispatch(workshiftApi.util.resetApiState());
  store.dispatch(notificationApi.util.resetApiState());
  store.dispatch(contractApi.util.resetApiState());
  store.dispatch(degreeApi.util.resetApiState());
  console.log('[Store] All RTK Query states have been reset');
};

// Perform complete logout without reload (let navigation handle it)
export const performCompleteLogout = async () => {
  try {
    console.log('[Store] Starting complete logout...');
    
    // 1. Clear tokens from SecureStore (Keychain/Keystore)
    console.log('[Store] Clearing SecureStore tokens...');
    await clearTokens();
    
    // 2. Clear non-sensitive data from AsyncStorage
    console.log('[Store] Clearing AsyncStorage...');
    await AsyncStorage.removeItem('userInfo');
    
    // Verify AsyncStorage is cleared
    const verifyUser = await AsyncStorage.getItem('userInfo');
    console.log('[Store] Storage cleared. Verification:', { 
      userCleared: verifyUser === null 
    });
    
    // 3. Dispatch Redux logout
    store.dispatch(logout());
    
    // 4. Reset all RTK Query cache
    resetAllApiStates();
    
    console.log('[Store] Logout completed successfully. App state is now cleared.');
  } catch (error) {
    console.error('[Store] Error during complete logout:', error);
    // Fallback: still reset states even if there's an error
    store.dispatch(logout());
    resetAllApiStates();
  }
};
