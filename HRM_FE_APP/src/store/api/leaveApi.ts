import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type {
  LeaveRequest,
  CreateLeaveRequestPayload,
  ReviewLeaveRequestPayload,
} from '../../types/leave';

export const leaveApi = createApi({
  reducerPath: 'leaveApi',
  baseQuery,
  tagTypes: ['MyLeaveRequests', 'PendingLeaveRequests'],
  endpoints: (builder) => ({
    /** GET /leave-requests/me */
    getMyLeaveRequests: builder.query<ApiResponse<LeaveRequest[]>, void>({
      query: () => '/leave-requests/me',
      providesTags: ['MyLeaveRequests'],
    }),

    /** GET /leave-requests/pending (for managers) */
    getPendingLeaveRequests: builder.query<ApiResponse<LeaveRequest[]>, void>({
      query: () => '/leave-requests/pending',
      providesTags: ['PendingLeaveRequests'],
    }),

    /** GET /leave-requests (all) */
    getAllLeaveRequests: builder.query<ApiResponse<LeaveRequest[]>, void>({
      query: () => '/leave-requests',
      providesTags: ['MyLeaveRequests'],
    }),

    createLeaveRequest: builder.mutation<ApiResponse<LeaveRequest>, CreateLeaveRequestPayload>({
      query: (body) => ({
        url: '/leave-requests',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['MyLeaveRequests'],
    }),


    cancelLeaveRequest: builder.mutation<ApiResponse<LeaveRequest>, string>({
      query: (id) => ({
        url: `/leave-requests/${id}/cancel`,
        method: 'PUT',
      }),
      invalidatesTags: ['MyLeaveRequests'],
    }),

    reviewLeaveRequest: builder.mutation<
      ApiResponse<LeaveRequest>,
      { id: string; body: ReviewLeaveRequestPayload }
    >({
      query: ({ id, body }) => ({
        url: `/leave-requests/${id}/approve`,
        method: 'PUT',
        body,
      }),
      invalidatesTags: ['MyLeaveRequests', 'PendingLeaveRequests'],
    }),
  }),
});

export const {
  useGetMyLeaveRequestsQuery,
  useGetPendingLeaveRequestsQuery,
  useGetAllLeaveRequestsQuery,
  useCreateLeaveRequestMutation,
  useCancelLeaveRequestMutation,
  useReviewLeaveRequestMutation,
} = leaveApi;
