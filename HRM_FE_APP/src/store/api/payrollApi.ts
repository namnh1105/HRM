import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { SalaryDetail } from '../../types/payroll';

export const payrollApi = createApi({
  reducerPath: 'payrollApi',
  baseQuery,
  tagTypes: ['MyPayrolls', 'Payroll'],
  endpoints: (builder) => ({
    /** GET /payrolls/me */
    getMyPayrolls: builder.query<ApiResponse<SalaryDetail[]>, void>({
      query: () => '/payrolls/me',
      providesTags: ['MyPayrolls'],
    }),

    /** GET /payrolls/:id */
    getPayrollById: builder.query<ApiResponse<SalaryDetail>, string>({
      query: (id) => `/payrolls/${id}`,
      providesTags: (_result, _error, id) => [{ type: 'Payroll', id }],
    }),

    /** GET /payrolls */
    getAllPayrolls: builder.query<ApiResponse<SalaryDetail[]>, void>({
      query: () => '/payrolls',
      providesTags: ['MyPayrolls'],
    }),

    /** GET /payrolls/employee/:employeeId */
    getPayrollsByEmployee: builder.query<ApiResponse<SalaryDetail[]>, string>({
      query: (employeeId) => `/payrolls/employee/${employeeId}`,
      providesTags: ['MyPayrolls'],
    }),

    /** GET /payrolls/month/:month/year/:year */
    getPayrollsByMonthYear: builder.query<
      ApiResponse<SalaryDetail[]>,
      { month: number; year: number }
    >({
      query: ({ month, year }) => `/payrolls/month/${month}/year/${year}`,
      providesTags: ['MyPayrolls'],
    }),

    /** GET /payrolls/store/:storeId */
    getPayrollsByStore: builder.query<ApiResponse<SalaryDetail[]>, string>({
      query: (storeId) => `/payrolls/store/${storeId}`,
      providesTags: ['MyPayrolls'],
    }),
  }),
});

export const {
  useGetMyPayrollsQuery,
  useGetPayrollByIdQuery,
  useGetAllPayrollsQuery,
  useGetPayrollsByEmployeeQuery,
  useGetPayrollsByMonthYearQuery,
  useGetPayrollsByStoreQuery,
} = payrollApi;
