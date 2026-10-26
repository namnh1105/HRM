/**
 * Cross-platform Token Storage Service
 *
 * - Native (iOS/Android): SecureStore (Keychain / Keystore) with AsyncStorage fallback
 * - Web: localStorage / AsyncStorage (SecureStore not available on web)
 */
import { Platform } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import AsyncStorage from '@react-native-async-storage/async-storage';

const SECURE_KEYS = {
  ACCESS_TOKEN: 'secure_access_token',
  REFRESH_TOKEN: 'secure_refresh_token',
} as const;

const isWeb = Platform.OS === 'web';

const safeSetItem = async (key: string, value: string): Promise<void> => {
  if (isWeb) {
    try {
      localStorage.setItem(key, value);
    } catch {}
    try {
      await AsyncStorage.setItem(key, value);
    } catch {}
  } else {
    try {
      await SecureStore.setItemAsync(key, value);
    } catch (e) {
      console.warn(`[TokenStorage] SecureStore setItem failed, falling back to AsyncStorage`, e);
      try {
        await AsyncStorage.setItem(key, value);
      } catch {}
    }
  }
};

const safeGetItem = async (key: string): Promise<string | null> => {
  if (isWeb) {
    try {
      const val = localStorage.getItem(key);
      if (val !== null) return val;
    } catch {}
    return await AsyncStorage.getItem(key);
  } else {
    try {
      const val = await SecureStore.getItemAsync(key);
      if (val !== null) return val;
    } catch (e) {
      console.warn(`[TokenStorage] SecureStore getItem failed, falling back to AsyncStorage`, e);
    }
    return await AsyncStorage.getItem(key);
  }
};

const safeDeleteItem = async (key: string): Promise<void> => {
  if (isWeb) {
    try {
      localStorage.removeItem(key);
    } catch {}
  }
  try {
    await SecureStore.deleteItemAsync(key);
  } catch {}
  try {
    await AsyncStorage.removeItem(key);
  } catch {}
};

// ─── In-memory token (fast, no I/O) ───────────────────────────────────
let inMemoryAccessToken: string | null = null;

// ─── Access Token ─────────────────────────────────────────────────────

/** Get access token from memory (instant, no async) */
export const getAccessToken = (): string | null => inMemoryAccessToken;

/** Set access token in memory + persist for restore */
export const setAccessToken = async (token: string): Promise<void> => {
  inMemoryAccessToken = token;
  try {
    await safeSetItem(SECURE_KEYS.ACCESS_TOKEN, token);
  } catch (error) {
    console.error('[TokenStorage] Failed to persist access token:', error);
  }
};

/** Restore access token into memory (call on app start) */
export const restoreAccessToken = async (): Promise<string | null> => {
  try {
    const token = await safeGetItem(SECURE_KEYS.ACCESS_TOKEN);
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

/** Get refresh token */
export const getRefreshToken = async (): Promise<string | null> => {
  try {
    return await safeGetItem(SECURE_KEYS.REFRESH_TOKEN);
  } catch (error) {
    console.error('[TokenStorage] Failed to get refresh token:', error);
    return null;
  }
};

/** Save refresh token */
export const setRefreshToken = async (token: string): Promise<void> => {
  try {
    await safeSetItem(SECURE_KEYS.REFRESH_TOKEN, token);
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
  try {
    await Promise.all([
      safeSetItem(SECURE_KEYS.ACCESS_TOKEN, accessToken),
      safeSetItem(SECURE_KEYS.REFRESH_TOKEN, refreshToken),
    ]);
    console.log('[TokenStorage] Tokens saved successfully');
  } catch (error) {
    console.warn('[TokenStorage] Error saving tokens:', error);
  }
};

/** Clear all tokens (logout) */
export const clearTokens = async (): Promise<void> => {
  inMemoryAccessToken = null;
  try {
    await Promise.all([
      safeDeleteItem(SECURE_KEYS.ACCESS_TOKEN),
      safeDeleteItem(SECURE_KEYS.REFRESH_TOKEN),
    ]);
    console.log('[TokenStorage] Tokens cleared');
  } catch (error) {
    console.error('[TokenStorage] Failed to clear tokens:', error);
  }
};
