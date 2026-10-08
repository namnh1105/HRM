/**
 * Secure Token Storage Service
 *
 * - accessToken  → kept in memory (fast) + SecureStore (restore on app restart)
 * - refreshToken → SecureStore only (Keychain on iOS, Keystore on Android)
 *
 * ❌ NEVER use AsyncStorage / SharedPreferences for tokens.
 */
import * as SecureStore from 'expo-secure-store';

const SECURE_KEYS = {
  ACCESS_TOKEN: 'secure_access_token',
  REFRESH_TOKEN: 'secure_refresh_token',
} as const;

// ─── In-memory token (fast, no I/O) ───────────────────────────────────
let inMemoryAccessToken: string | null = null;

// ─── Access Token ─────────────────────────────────────────────────────

/** Get access token from memory (instant, no async) */
export const getAccessToken = (): string | null => inMemoryAccessToken;

/** Set access token in memory + persist to SecureStore for restore */
export const setAccessToken = async (token: string): Promise<void> => {
  inMemoryAccessToken = token;
  try {
    await SecureStore.setItemAsync(SECURE_KEYS.ACCESS_TOKEN, token);
  } catch (error) {
    console.error('[TokenStorage] Failed to persist access token:', error);
  }
};

/** Restore access token from SecureStore into memory (call on app start) */
export const restoreAccessToken = async (): Promise<string | null> => {
  try {
    const token = await SecureStore.getItemAsync(SECURE_KEYS.ACCESS_TOKEN);
    if (token) {
      inMemoryAccessToken = token;
    }
    return token;
  } catch (error) {
    console.error('[TokenStorage] Failed to restore access token:', error);
    return null;
  }
};

// ─── Refresh Token ────────────────────────────────────────────────────

/** Get refresh token from SecureStore */
export const getRefreshToken = async (): Promise<string | null> => {
  try {
    return await SecureStore.getItemAsync(SECURE_KEYS.REFRESH_TOKEN);
  } catch (error) {
    console.error('[TokenStorage] Failed to get refresh token:', error);
    return null;
  }
};

/** Save refresh token to SecureStore */
export const setRefreshToken = async (token: string): Promise<void> => {
  try {
    await SecureStore.setItemAsync(SECURE_KEYS.REFRESH_TOKEN, token);
  } catch (error) {
    console.error('[TokenStorage] Failed to save refresh token:', error);
  }
};

// ─── Bulk Operations ──────────────────────────────────────────────────

/** Save both tokens after login or refresh */
export const saveTokens = async (
  accessToken: string,
  refreshToken: string
): Promise<void> => {
  inMemoryAccessToken = accessToken;
  await Promise.all([
    SecureStore.setItemAsync(SECURE_KEYS.ACCESS_TOKEN, accessToken),
    SecureStore.setItemAsync(SECURE_KEYS.REFRESH_TOKEN, refreshToken),
  ]);
  console.log('[TokenStorage] Tokens saved to SecureStore');
};

/** Clear all tokens (logout) */
export const clearTokens = async (): Promise<void> => {
  inMemoryAccessToken = null;
  await Promise.all([
    SecureStore.deleteItemAsync(SECURE_KEYS.ACCESS_TOKEN),
    SecureStore.deleteItemAsync(SECURE_KEYS.REFRESH_TOKEN),
  ]).catch((error) => {
    console.error('[TokenStorage] Failed to clear tokens:', error);
  });
  console.log('[TokenStorage] Tokens cleared');
};
