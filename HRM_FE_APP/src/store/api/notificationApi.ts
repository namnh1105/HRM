import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse, Page } from '../../types/common';
import type { HrmNotification } from '../../types/notification';

export const notificationApi = createApi({
  reducerPath: 'notificationApi',
  baseQuery,
  tagTypes: ['Notification'],
  endpoints: (builder) => ({
    /** GET /notifications */
    getMyNotifications: builder.query<ApiResponse<Page<HrmNotification>>, { page?: number; size?: number } | void>({
      query: (params) => {
        let url = '/notifications';
        if (params && (params.page !== undefined || params.size !== undefined)) {
          const qs = new URLSearchParams();
          if (params.page !== undefined) qs.append('page', params.page.toString());
          if (params.size !== undefined) qs.append('size', params.size.toString());
          url += `?${qs.toString()}`;
        }
        return url;
      },
      providesTags: ['Notification'],
    }),

    /** GET /notifications/unread-count */
    getUnreadCount: builder.query<ApiResponse<number>, void>({
      query: () => '/notifications/unread-count',
      providesTags: ['Notification'],
    }),

    /** PATCH /notifications/:id/read */
    markAsRead: builder.mutation<ApiResponse<null>, string>({
      query: (id) => ({
        url: `/notifications/${id}/read`,
        method: 'PATCH',
      }),
      // Optimistic update: mark read immediately
      async onQueryStarted(id, { dispatch, queryFulfilled }) {
        const patch = dispatch(
          notificationApi.util.updateQueryData('getMyNotifications', undefined, (draft) => {
            const notif = draft.data?.content?.find((n) => n.id === id);
            if (notif) notif.isRead = true;
          }),
        );
        try {
          await queryFulfilled;
        } catch {
          patch.undo();
        }
      },
      invalidatesTags: ['Notification'],
    }),

    /** PATCH /notifications/read-all */
    markAllAsRead: builder.mutation<ApiResponse<null>, void>({
      query: () => ({
        url: '/notifications/read-all',
        method: 'PATCH',
      }),
      // Optimistic update
      async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
        const patch = dispatch(
          notificationApi.util.updateQueryData('getMyNotifications', undefined, (draft) => {
            draft.data?.content?.forEach((n) => {
              n.isRead = true;
            });
          }),
        );
        try {
          await queryFulfilled;
        } catch {
          patch.undo();
        }
      },
      invalidatesTags: ['Notification'],
    }),
  }),
});

export const {
  useGetMyNotificationsQuery,
  useGetUnreadCountQuery,
  useMarkAsReadMutation,
  useMarkAllAsReadMutation,
} = notificationApi;
