import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { AuthState } from '@/types/store';
import { UserInfo } from '@/types/user';

const initialState: AuthState = {
    accessToken: null,
    refreshToken: null,
    user: null,
    isAuthenticated: false,
    roles: [],
    permissions: [],
};

type UnknownRecord = Record<string, unknown>;

const asRecord = (value: unknown): UnknownRecord => (typeof value === 'object' && value !== null ? value as UnknownRecord : {});
const asString = (value: unknown): string | undefined => (typeof value === 'string' ? value : undefined);
const asNullableString = (value: unknown): string | null | undefined =>
    value === null ? null : typeof value === 'string' ? value : undefined;
const asBoolean = (value: unknown): boolean | undefined => (typeof value === 'boolean' ? value : undefined);
const asStringArray = (value: unknown): string[] =>
    Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];

export const mapToUserInfo = (raw: unknown): UserInfo => {
    const source = asRecord(raw);
    const employee = asRecord(source.employee);

    return {
        id: asString(source.id) ?? '',
        email: asString(source.email) ?? asString(source.username),
        username: asString(source.username) ?? asString(source.email),
        givenName: asString(source.givenName) ?? asString(source.given_name),
        familyName: asString(source.familyName) ?? asString(source.family_name),
        avatarUrl: asNullableString(source.avatarUrl) ?? asNullableString(source.avatar_url) ?? null,
        employee: source.employee ?? null,
        roles: asStringArray(source.roles).length ? asStringArray(source.roles) : asStringArray(source.role_codes),
        permissions: asStringArray(source.permissions).length ? asStringArray(source.permissions) : asStringArray(source.permission_codes),
        isActive: asBoolean(source.isActive) ?? asBoolean(source.is_active),
        isDeleted: asBoolean(source.isDeleted) ?? asBoolean(source.is_deleted),
        createdAt: asString(source.createdAt) ?? asString(source.created_at),
        updatedAt: asString(source.updatedAt) ?? asString(source.updated_at),
        createdBy: asNullableString(source.createdBy) ?? asNullableString(source.created_by),
        updatedBy: asNullableString(source.updatedBy) ?? asNullableString(source.updated_by),
        deletedAt: asNullableString(source.deletedAt) ?? asNullableString(source.deleted_at),
        deletedBy: asNullableString(source.deletedBy) ?? asNullableString(source.deleted_by),
        storeId:
            asNullableString(source.storeId) ??
            asNullableString(source.store_id) ??
            asNullableString(employee.storeId) ??
            asNullableString(employee.store_id),
    };
};

export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ accessToken: string; refreshToken?: string | null; user: UserInfo }>
        ) => {
            state.accessToken = action.payload.accessToken;
            state.refreshToken = action.payload.refreshToken ?? state.refreshToken;
            state.user = action.payload.user;
            state.isAuthenticated = true;
            state.roles = action.payload.user.roles || [];
            state.permissions = action.payload.user.permissions || [];
        },
        setAccessTokenInStore: (state, action: PayloadAction<string>) => {
            state.accessToken = action.payload;
        },
        restoreAuth: (
            state,
            action: PayloadAction<{ accessToken: string; refreshToken?: string | null; user: UserInfo }>
        ) => {
            state.accessToken = action.payload.accessToken;
            state.refreshToken = action.payload.refreshToken ?? state.refreshToken;
            state.user = action.payload.user;
            state.isAuthenticated = true;
            state.roles = action.payload.user.roles || [];
            state.permissions = action.payload.user.permissions || [];
        },
        logout: (state) => {
            state.accessToken = null;
            state.refreshToken = null;
            state.user = null;
            state.isAuthenticated = false;
            state.roles = [];
            state.permissions = [];
        },
    },
});

export const { setCredentials, setAccessTokenInStore, restoreAuth, logout } = authSlice.actions;

export default authSlice.reducer;
