import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { WorkShift, EmployeeWorkShift } from '../../types/workshift';

export const workshiftApi = createApi({
  reducerPath: 'workshiftApi',
  baseQuery,
  tagTypes: ['WorkShift', 'EmployeeWorkShift'],
  endpoints: (builder) => ({
    /** GET /work-shifts/enabled */
    getActiveWorkShifts: builder.query<ApiResponse<WorkShift[]>, void>({
      query: () => '/work-shifts/enabled',
      providesTags: ['WorkShift'],
    }),

    /** GET /work-shifts */
    getAllWorkShifts: builder.query<ApiResponse<WorkShift[]>, void>({
      query: () => '/work-shifts',
      providesTags: ['WorkShift'],
    }),

    /** GET /work-shifts/:id */
    getWorkShiftById: builder.query<ApiResponse<WorkShift>, string>({
      query: (id) => `/work-shifts/${id}`,
      providesTags: (_result, _error, id) => [{ type: 'WorkShift', id }],
    }),

    /** GET /employee-work-shifts/my-shifts - current user's shifts for today */
    getMyShiftsToday: builder.query<ApiResponse<EmployeeWorkShift[]>, void>({
      query: () => '/employee-work-shifts/my-shifts',
      providesTags: ['EmployeeWorkShift'],
    }),

    /** GET /employee-work-shifts/my-shifts/all - all shift assignments for current user */
    getMyAllShifts: builder.query<ApiResponse<EmployeeWorkShift[]>, void>({
      query: () => '/employee-work-shifts/me',
      providesTags: ['EmployeeWorkShift'],
    }),

    /** GET /employee-work-shifts/employee/:employeeId - all assignments for employee */
    getEmployeeWorkShifts: builder.query<ApiResponse<EmployeeWorkShift[]>, string>({
      query: (employeeId) => `/employee-work-shifts/employee/${employeeId}`,
      providesTags: (_result, _error, employeeId) => [
        { type: 'EmployeeWorkShift', id: employeeId },
      ],
    }),

    /** GET /employee-work-shifts/employee/:employeeId/date?date=YYYY-MM-DD */
    getEmployeeShiftsByDate: builder.query<
      ApiResponse<EmployeeWorkShift[]>,
      { employeeId: string; date: string }
    >({
      query: ({ employeeId, date }) =>
        `/employee-work-shifts/employee/${employeeId}/date?date=${date}`,
      providesTags: ['EmployeeWorkShift'],
    }),
    
    /** GET /employee-work-shifts/store/:storeId - all shifts in a store */
    getStoreWorkShifts: builder.query<ApiResponse<EmployeeWorkShift[]>, string>({
      query: (storeId) => `/employee-work-shifts/store/${storeId}`,
      providesTags: ['EmployeeWorkShift'],
    }),

    /** GET /employee-work-shifts - all active shift assignments */
    getAllActiveAssignments: builder.query<ApiResponse<EmployeeWorkShift[]>, void>({
      query: () => '/employee-work-shifts',
      providesTags: ['EmployeeWorkShift'],
    }),

    /** POST /employee-work-shifts - Assign shift */
    assignWorkShift: builder.mutation<
      ApiResponse<EmployeeWorkShift>,
      { employeeId: string; workShiftId: string; date: string }
    >({
      query: (body) => ({
        url: '/employee-work-shifts',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['EmployeeWorkShift'],
    }),

    /** DELETE /employee-work-shifts/:id - Delete assignment */
    deleteWorkShift: builder.mutation<ApiResponse<void>, string>({
      query: (id) => ({
        url: `/employee-work-shifts/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['EmployeeWorkShift'],
    }),
  }),
});

export const {
  useGetActiveWorkShiftsQuery,
  useGetAllWorkShiftsQuery,
  useGetWorkShiftByIdQuery,
  useGetMyShiftsTodayQuery,
  useGetMyAllShiftsQuery,
  useGetEmployeeWorkShiftsQuery,
  useGetEmployeeShiftsByDateQuery,
  useGetStoreWorkShiftsQuery,
  useGetAllActiveAssignmentsQuery,
  useAssignWorkShiftMutation,
  useDeleteWorkShiftMutation,
} = workshiftApi;
