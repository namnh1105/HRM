import { baseApi } from './baseApi';
import {
    AuthResponseDTO,
    ApiResponse,
    LoginDTO,
    RegisterDTO, RefreshTokenDTO,
} from '@/types';

export const authApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({        
        login: builder.mutation<ApiResponse<AuthResponseDTO>, LoginDTO>({
            query: (body) => ({
                url: 'auth/login',
                method: 'POST',
                body,
            }),
        }),

        register: builder.mutation<ApiResponse<AuthResponseDTO>, RegisterDTO>({
            query: (body) => ({
                url: 'auth/register',
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'User', id: 'LIST' }, { type: 'User', id: 'STATS' }],
        }),

        refresh: builder.mutation<ApiResponse<AuthResponseDTO>, RefreshTokenDTO>({
            query: (body) => ({
                url: 'auth/refresh-token',
                method: 'POST',
                body,
            }),
        }),

        logout: builder.mutation<ApiResponse<null>, void>({
            query: () => ({
                url: 'auth/logout',
                method: 'POST',
            }),
        }),
    }),

    overrideExisting: false,
});

export const {
    useLoginMutation,
    useRegisterMutation,
    useRefreshMutation,
    useLogoutMutation,
} = authApi;
