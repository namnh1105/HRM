import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { Degree } from '../../types/degree';

export const degreeApi = createApi({
  reducerPath: 'degreeApi',
  baseQuery,
  tagTypes: ['Degree', 'MyDegree'],
  endpoints: (builder) => ({
    /** GET /degrees */
    getAllDegrees: builder.query<ApiResponse<Degree[]>, void>({
      query: () => '/degrees',
      providesTags: ['Degree'],
    }),

    /** GET /degrees/:id */
    getDegreeById: builder.query<ApiResponse<Degree>, string>({
      query: (id) => `/degrees/${id}`,
      providesTags: (_result, _error, id) => [{ type: 'Degree', id }],
    }),

    /** GET /degrees/employee/:employeeId */
    getDegreesByEmployee: builder.query<ApiResponse<Degree[]>, string>({
      query: (employeeId) => `/degrees/employee/${employeeId}`,
      providesTags: ['MyDegree'],
    }),
  }),
});

export const {
  useGetAllDegreesQuery,
  useGetDegreeByIdQuery,
  useGetDegreesByEmployeeQuery,
} = degreeApi;