import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { Contract } from '../../types/contract';

export const contractApi = createApi({
  reducerPath: 'contractApi',
  baseQuery,
  tagTypes: ['Contract', 'MyContract'],
  endpoints: (builder) => ({
    /** GET /contracts */
    getAllContracts: builder.query<ApiResponse<Contract[]>, void>({
      query: () => '/contracts',
      providesTags: ['Contract'],
    }),

    /** GET /contracts/:id */
    getContractById: builder.query<ApiResponse<Contract>, string>({
      query: (id) => `/contracts/${id}`,
      providesTags: (_result, _error, id) => [{ type: 'Contract', id }],
    }),

    /** GET /contracts/employee/:employeeId */
    getContractsByEmployee: builder.query<ApiResponse<Contract[]>, string>({
      query: (employeeId) => `/contracts/employee/${employeeId}`,
      providesTags: ['MyContract'],
    }),

    /** GET /contracts/active */
    getActiveContracts: builder.query<ApiResponse<Contract[]>, void>({
      query: () => '/contracts/active',
      providesTags: ['Contract'],
    }),
  }),
});

export const {
  useGetAllContractsQuery,
  useGetContractByIdQuery,
  useGetContractsByEmployeeQuery,
  useGetActiveContractsQuery,
} = contractApi;