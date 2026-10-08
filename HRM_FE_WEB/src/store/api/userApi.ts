import { baseApi } from './baseApi';
import {
    ApiResponse,
    PaginatedApiResponse,
    UserResponse,
} from '@/types';

type RawUserLike = Partial<UserResponse> & {
    role_codes?: unknown;
    roleCodes?: unknown;
    permission_codes?: unknown;
    permissionCodes?: unknown;
};

const toStringArray = (value: unknown): string[] =>
    Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];

const normalizeUser = (user: RawUserLike): UserResponse => ({
    ...(user as UserResponse),
    roles: toStringArray(user.roles).length
        ? toStringArray(user.roles)
        : toStringArray(user.role_codes).length
            ? toStringArray(user.role_codes)
            : toStringArray(user.roleCodes),
    permissions: toStringArray(user.permissions).length
        ? toStringArray(user.permissions)
        : toStringArray(user.permission_codes).length
            ? toStringArray(user.permission_codes)
            : toStringArray(user.permissionCodes),
});

export const userApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({

        // GET /api/v1/users?page=&size=&includeDeleted=
        getAllUsers: builder.query<PaginatedApiResponse<UserResponse>, { page?: number; size?: number; includeDeleted?: boolean }>({
            query: ({ page = 0, size = 10, includeDeleted = true } = {}) => ({
                url: `users?page=${page}&size=${size}&includeDeleted=${includeDeleted}`,
                method: 'GET',
            }),
            transformResponse: (response: PaginatedApiResponse<RawUserLike>) => ({
                ...response,
                data: (response.data ?? []).map(normalizeUser),
            }),
            providesTags: (result) =>
                result?.data
                    ? [
                          ...result.data.map(({ id }) => ({ type: 'User' as const, id })),
                          { type: 'User', id: 'LIST' },
                      ]
                    : [{ type: 'User', id: 'LIST' }],
        }),

        // GET /api/v1/users/search
        searchUsers: builder.query<PaginatedApiResponse<UserResponse>, { keyword: string; includeDeleted?: boolean; page?: number; size?: number }>({
            query: ({ keyword, includeDeleted = false, page = 0, size = 10 }) => ({
                url: `users/search?keyword=${encodeURIComponent(keyword)}&includeDeleted=${includeDeleted}&page=${page}&size=${size}`,
                method: 'GET',
            }),
            transformResponse: (response: PaginatedApiResponse<RawUserLike>) => ({
                ...response,
                data: (response.data ?? []).map(normalizeUser),
            }),
            providesTags: [{ type: 'User', id: 'SEARCH' }],
        }),

        // GET /api/v1/users/me
        getMe: builder.query<ApiResponse<UserResponse>, void>({
            query: () => ({ url: 'users/me', method: 'GET' }),
            transformResponse: (response: ApiResponse<RawUserLike>) => ({
                ...response,
                data: normalizeUser(response.data),
            }),
            providesTags: [{ type: 'User', id: 'ME' }],
        }),

        // GET /api/v1/users/:id
        getUserById: builder.query<ApiResponse<UserResponse>, string>({
            query: (userId) => ({ url: `users/${userId}`, method: 'GET' }),
            transformResponse: (response: ApiResponse<RawUserLike>) => ({
                ...response,
                data: normalizeUser(response.data),
            }),
            providesTags: (_, __, id) => [{ type: 'User', id }],
        }),

        // DELETE /api/v1/users/:id  (soft delete)
        deleteUser: builder.mutation<ApiResponse<null>, string>({
            query: (userId) => ({ url: `users/${userId}`, method: 'DELETE' }),
            invalidatesTags: (_, __, id) => [{ type: 'User', id }, { type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }, { type: 'User', id: 'SEARCH' }],
        }),

        // POST /api/v1/users/:id/activate
        activateUser: builder.mutation<ApiResponse<UserResponse>, string>({
            query: (userId) => ({ url: `users/${userId}/activate`, method: 'POST' }),
            invalidatesTags: (_, __, id) => [{ type: 'User', id }, { type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }, { type: 'User', id: 'SEARCH' }],
        }),

        // POST /api/v1/users/:id/deactivate
        deactivateUser: builder.mutation<ApiResponse<UserResponse>, string>({
            query: (userId) => ({ url: `users/${userId}/deactivate`, method: 'POST' }),
            invalidatesTags: (_, __, id) => [{ type: 'User', id }, { type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }, { type: 'User', id: 'SEARCH' }],
        }),

        // POST /api/v1/users/:id/restore
        restoreUser: builder.mutation<ApiResponse<UserResponse>, string>({
            query: (userId) => ({ url: `users/${userId}/restore`, method: 'POST' }),
            invalidatesTags: (_, __, id) => [{ type: 'User', id }, { type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }, { type: 'User', id: 'SEARCH' }],
        }),

        // DELETE /api/v1/users/:id/permanent
        permanentDeleteUser: builder.mutation<ApiResponse<null>, string>({
            query: (userId) => ({ url: `users/${userId}/permanent`, method: 'DELETE' }),
            invalidatesTags: [{ type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }, { type: 'User', id: 'SEARCH' }],
        }),

        // GET /api/v1/users/:id/roles
        getUserRoles: builder.query<ApiResponse<import('@/types').UserRoleAssignmentResponse[]>, string>({
            query: (userId) => ({ url: `users/${userId}/roles`, method: 'GET' }),
            providesTags: (_, __, id) => [{ type: 'UserRole', id }],
        }),

        // POST /api/v1/users/:userId/roles/:roleId
        assignRoleToUser: builder.mutation<ApiResponse<null>, { userId: string; roleId: string }>({
            query: ({ userId, roleId }) => ({
                url: `users/${userId}/roles/${roleId}`,
                method: 'POST',
            }),
            invalidatesTags: (_, __, { userId }) => [{ type: 'UserRole', id: userId }, { type: 'User', id: userId }],
        }),

        // DELETE /api/v1/users/:userId/roles/:roleId
        removeRoleFromUser: builder.mutation<ApiResponse<null>, { userId: string; roleId: string }>({
            query: ({ userId, roleId }) => ({
                url: `users/${userId}/roles/${roleId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (_, __, { userId }) => [{ type: 'UserRole', id: userId }, { type: 'User', id: userId }],
        }),

        // GET /api/v1/users/stats
        getUserStats: builder.query<ApiResponse<{
            total: number;
            active: number;
            inactive: number;
            deleted: number;
        }>, void>({
            query: () => ({ url: 'users/stats', method: 'GET' }),
            providesTags: [{ type: 'User', id: 'STATS' }],
        }),
    }),
    overrideExisting: false,
});

export const {
    useGetAllUsersQuery,
    useSearchUsersQuery,
    useGetMeQuery,
    useGetUserByIdQuery,
    useDeleteUserMutation,
    useActivateUserMutation,
    useDeactivateUserMutation,
    useRestoreUserMutation,
    usePermanentDeleteUserMutation,
    useGetUserRolesQuery,
    useAssignRoleToUserMutation,
    useRemoveRoleFromUserMutation,
    useGetUserStatsQuery,
} = userApi;
