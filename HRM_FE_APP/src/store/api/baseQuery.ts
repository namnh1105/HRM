import {
  fetchBaseQuery,
  type BaseQueryFn,
  type FetchArgs,
  type FetchBaseQueryError,
} from '@reduxjs/toolkit/query/react';
import { Mutex } from 'async-mutex';
import { API_BASE_URL } from '../../utils/constants';
import {
  getAccessToken,
  getRefreshToken,
  saveTokens,
  clearTokens,
} from '../../services/tokenStorage';
import { logout } from '../slices/authSlice';

// ─── Raw base query (reads token from memory) ────────────────────────
const rawBaseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  prepareHeaders: (headers) => {
    // Sync read from memory — no AsyncStorage overhead
    const token = getAccessToken();
    if (token) {
      headers.set('authorization', `Bearer ${token}`);
    }
    return headers;
  },
});

// Mutex prevents multiple concurrent refresh calls
const refreshMutex = new Mutex();

// ─── Base query with silent 401 re-auth ───────────────────────────────
export const baseQuery: BaseQueryFn<
  string | FetchArgs,
  unknown,
  FetchBaseQueryError
> = async (args, api, extraOptions) => {
  // Wait if another request is already refreshing
  await refreshMutex.waitForUnlock();

  let result = await rawBaseQuery(args, api, extraOptions);

  if (result.error && result.error.status === 401) {
    // Only one request should attempt refresh at a time
    if (!refreshMutex.isLocked()) {
      const release = await refreshMutex.acquire();
      try {
        const refreshToken = await getRefreshToken();

        if (refreshToken) {
          console.log('[baseQuery] 401 detected — attempting silent refresh…');

          // Call backend refresh endpoint
          const refreshResult = await rawBaseQuery(
            {
              url: '/auth/refresh-token',
              method: 'POST',
              body: { refreshToken },
            },
            api,
            extraOptions
          );

          if (refreshResult.data) {
            const data = refreshResult.data as {
              success: boolean;
              data: {
                accessToken: string;
                tokenType: string;
                expiresIn: number;
                user?: any;
              };
            };

            if (data.success && data.data?.accessToken) {
              // Save new tokens securely
              // Backend may not rotate refresh token — keep existing if not provided
              await saveTokens(data.data.accessToken, refreshToken);

              // Update Redux in-memory state
              const { setAccessTokenInStore, setCredentials, mapToUserInfo } = await import('../slices/authSlice');
              api.dispatch(setAccessTokenInStore(data.data.accessToken));
              
              // If backend returned user data during refresh, update it too
              if (data.data.user) {
                const freshUser = mapToUserInfo(data.data.user);
                api.dispatch(setCredentials({ 
                  accessToken: data.data.accessToken, 
                  user: freshUser 
                }));
                // Keep storage in sync
                const AsyncStorage = (await import('@react-native-async-storage/async-storage')).default;
                await AsyncStorage.setItem('userInfo', JSON.stringify(freshUser));
                console.log('[baseQuery] User profile updated during silent refresh');
              }

              console.log('[baseQuery] Silent refresh OK — retrying original request');

              // Retry the original request with the new token
              result = await rawBaseQuery(args, api, extraOptions);
            } else {
              console.warn('[baseQuery] Refresh response invalid — logging out');
              await clearTokens();
              api.dispatch(logout());
            }
          } else {
            console.warn('[baseQuery] Refresh failed — logging out');
            await clearTokens();
            api.dispatch(logout());
          }
        } else {
          console.warn('[baseQuery] No refresh token available — logging out');
          await clearTokens();
          api.dispatch(logout());
        }
      } finally {
        release();
      }
    } else {
      // Another request is refreshing — wait for it, then retry
      await refreshMutex.waitForUnlock();
      result = await rawBaseQuery(args, api, extraOptions);
    }
  }

  return result;
};
