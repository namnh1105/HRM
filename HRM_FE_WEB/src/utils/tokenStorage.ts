const ACCESS_TOKEN_KEY = 'ws_access_token';
const REFRESH_TOKEN_KEY = 'ws_refresh_token';
const USER_KEY = 'ws_user_info';

let inMemoryAccessToken: string | null = null;

const isBrowser = () => typeof window !== 'undefined';

export const getAccessToken = (): string | null => {
    if (inMemoryAccessToken) return inMemoryAccessToken;
    if (!isBrowser()) return null;
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (token) inMemoryAccessToken = token;
    return token;
};

export const setAccessToken = (token: string) => {
    inMemoryAccessToken = token;
    if (!isBrowser()) return;
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
};

export const restoreAccessToken = (): string | null => {
    if (!isBrowser()) return null;
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (token) inMemoryAccessToken = token;
    return token;
};

export const getRefreshToken = (): string | null => {
    if (!isBrowser()) return null;
    return localStorage.getItem(REFRESH_TOKEN_KEY);
};

export const setRefreshToken = (token: string) => {
    if (!isBrowser()) return;
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
};

export const saveTokens = (accessToken: string, refreshToken: string) => {
    setAccessToken(accessToken);
    setRefreshToken(refreshToken);
};

export const clearTokens = () => {
    inMemoryAccessToken = null;
    if (!isBrowser()) return;
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
};

export const setStoredUser = (user: unknown) => {
    if (!isBrowser()) return;
    localStorage.setItem(USER_KEY, JSON.stringify(user));
};

export const getStoredUser = (): unknown | null => {
    if (!isBrowser()) return null;
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
};

export const clearStoredUser = () => {
    if (!isBrowser()) return;
    localStorage.removeItem(USER_KEY);
};
