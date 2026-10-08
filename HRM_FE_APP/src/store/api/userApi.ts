import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { UserProfileData } from '../../types/user';

// Re-export for backward compatibility
export type { UserProfileData } from '../../types/user';

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const userApi = createApi({
  reducerPath: 'userApi',
  baseQuery,
  tagTypes: ['User'],
  endpoints: (builder) => ({
    getUserById: builder.query<ApiResponse<UserProfileData>, string>({
      query: (userId) => ({
        url: `/users/${userId}`,
        method: 'GET',
      }),
      providesTags: (result, error, userId) => [{ type: 'User', id: userId }],
    }),

    /** PUT /users/change-password */
    changePassword: builder.mutation<ApiResponse<null>, ChangePasswordRequest>({
      query: (body) => ({
        url: '/users/change-password',
        method: 'PUT',
        body,
      }),
    }),
  }),
});

export const {
  useGetUserByIdQuery,
  useChangePasswordMutation,
} = userApi;
