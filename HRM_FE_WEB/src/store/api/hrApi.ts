import { baseApi } from './baseApi';
import type {
    CreateEmployeeRequest,
    CreateStoreRequest,
    CreateWorkShiftRequest,
    UpdateEmployeeRequest,
    UpdateStoreRequest,
    UpdateWorkShiftRequest,
} from '@/types/hr';
import type { ApiResponse } from '@/types/api-response';
import type { PayrollSummary, AutoGeneratePayrollRequest } from '@/types';

export const hrApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getEmployees: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `employees?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Employee', id: 'LIST' }],
        }),
        getEmployeesByStore: builder.query<unknown, { storeId: string; page?: number; size?: number }>({
            query: ({ storeId, page = 0, size = 50 }) => ({
                url: `employees/store/${storeId}?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: (_res, _err, arg) => [{ type: 'Employee', id: `STORE_${arg.storeId}` }],
        }),
        searchEmployees: builder.query<unknown, { keyword: string; page?: number; size?: number }>({
            query: ({ keyword, page = 0, size = 10 }) => ({
                url: `employees/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Employee', id: 'SEARCH' }],
        }),
        searchStores: builder.query<unknown, { keyword: string }>({
            query: ({ keyword }) => ({
                url: `stores/search?keyword=${encodeURIComponent(keyword)}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Store', id: 'SEARCH' }],
        }),
        searchDepartments: builder.query<unknown, { keyword: string }>({
            query: ({ keyword }) => ({
                url: `departments/search?keyword=${encodeURIComponent(keyword)}`,
                method: 'GET',
            }),
        }),
        getEmployeeStats: builder.query<unknown, void>({
            query: () => ({ url: 'employees/stats', method: 'GET' }),
            providesTags: [{ type: 'Employee', id: 'STATS' }],
        }),
        getEmployeeById: builder.query<unknown, string>({
            query: (employeeId) => ({
                url: `employees/${employeeId}`,
                method: 'GET',
            }),
            providesTags: (_, __, id) => [{ type: 'Employee', id }],
        }),
        createEmployee: builder.mutation<unknown, CreateEmployeeRequest>({
            query: (body) => ({
                url: `employees`,
                method: 'POST',
                body,
            }),
            invalidatesTags: [
                { type: 'Employee', id: 'LIST' },
                { type: 'Employee', id: 'STATS' },
                { type: 'Employee', id: 'SEARCH' },
            ],
        }),
        updateEmployee: builder.mutation<unknown, { id: string; body: UpdateEmployeeRequest }>({
            query: ({ id, body }) => ({
                url: `employees/${id}`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: (_, __, { id }) => [
                { type: 'Employee', id: 'LIST' },
                { type: 'Employee', id },
                { type: 'Employee', id: 'STATS' },
                { type: 'Employee', id: 'SEARCH' },
            ],
        }),
        getMyAttendanceHistory: builder.query<unknown, { startDate?: string; endDate?: string; page?: number; size?: number }>({
            query: ({ startDate, endDate, page = 0, size = 10 } = {}) => ({
                url: `attendances/me/history?${[
                    startDate ? `startDate=${encodeURIComponent(startDate)}` : null,
                    endDate ? `endDate=${encodeURIComponent(endDate)}` : null,
                    `page=${page}`,
                    `size=${size}`,
                ].filter(Boolean).join('&')}`,
                method: 'GET',
            }),
        }),
            getAttendanceHistoryByEmployee: builder.query<unknown, { employeeId: string; startDate?: string; endDate?: string; page?: number; size?: number }>({
                query: ({ employeeId, startDate, endDate, page = 0, size = 10 }) => ({
                    url: `attendances/employee/${employeeId}/history?${[
                        startDate ? `startDate=${encodeURIComponent(startDate)}` : null,
                        endDate ? `endDate=${encodeURIComponent(endDate)}` : null,
                        `page=${page}`,
                        `size=${size}`,
                    ].filter(Boolean).join('&')}`,
                    method: 'GET',
                }),
                providesTags: (_res, _err, arg) => [{ type: 'Attendance', id: `EMPLOYEE_${arg.employeeId}` }],
            }),
        getMyAttendanceToday: builder.query<unknown, void>({
            query: () => ({
                url: `attendances/me/today`,
                method: 'GET',
            }),
        }),
        getAttendanceStats: builder.query<unknown, void>({
            query: () => ({ url: 'attendances/stats', method: 'GET' }),
            providesTags: [{ type: 'Attendance', id: 'STATS' }],
        }),
        checkIn: builder.mutation<unknown, { workShiftId?: string } | void>({
            query: (body) => ({
                url: `attendances/check-in`,
                method: 'POST',
                body: body ?? {},
            }),
            invalidatesTags: [{ type: 'Attendance', id: 'STATS' }],
        }),
        checkOut: builder.mutation<unknown, { workShiftId?: string } | void>({
            query: (body) => ({
                url: `attendances/check-out`,
                method: 'POST',
                body: body ?? {},
            }),
            invalidatesTags: [{ type: 'Attendance', id: 'STATS' }],
        }),
        getWorkShifts: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `work-shifts?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'WorkShift', id: 'LIST' }],
        }),
        getEnabledWorkShifts: builder.query<unknown, void>({
            query: () => ({
                url: `work-shifts/enabled`,
                method: 'GET',
            }),
        }),
        createWorkShift: builder.mutation<unknown, CreateWorkShiftRequest>({
            query: (body) => ({
                url: `work-shifts`,
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'WorkShift', id: 'LIST' }],
        }),
        updateWorkShift: builder.mutation<unknown, { id: string; body: UpdateWorkShiftRequest }>({
            query: ({ id, body }) => ({
                url: `work-shifts/${id}`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: [{ type: 'WorkShift', id: 'LIST' }, { type: 'WorkShift', id: 'STATS' }],
        }),
        deleteWorkShift: builder.mutation<unknown, string>({
            query: (id) => ({
                url: `work-shifts/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: [{ type: 'WorkShift', id: 'LIST' }, { type: 'WorkShift', id: 'STATS' }],
        }),
        getWorkShiftStats: builder.query<unknown, void>({
            query: () => ({ url: 'work-shifts/stats', method: 'GET' }),
            providesTags: [{ type: 'WorkShift', id: 'STATS' }],
        }),
        getMyLeaveRequests: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `leave-requests/me?page=${page}&size=${size}`,
                method: 'GET',
            }),
        }),
        getLeaveRequests: builder.query<unknown, { page?: number; size?: number; status?: string }>({
            query: ({ page = 0, size = 10, status } = {}) => ({
                url: `leave-requests?page=${page}&size=${size}${status ? `&status=${status}` : ''}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'LeaveRequest', id: 'LIST' }],
        }),
        getLeaveRequestStats: builder.query<unknown, void>({
            query: () => ({ url: 'leave-requests/stats', method: 'GET' }),
            providesTags: [{ type: 'LeaveRequest', id: 'STATS' }],
        }),
        getPendingLeaveRequests: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `leave-requests/pending?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'LeaveRequest', id: 'PENDING' }],
        }),
        approveLeaveRequest: builder.mutation<unknown, { id: string; body: { approved: boolean; comment?: string } }>({
            query: ({ id, body }) => ({
                url: `leave-requests/${id}/approve`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: [
                { type: 'LeaveRequest', id: 'LIST' },
                { type: 'LeaveRequest', id: 'PENDING' },
                { type: 'LeaveRequest', id: 'STATS' },
            ],
        }),
        cancelLeaveRequest: builder.mutation<unknown, string>({
            query: (id) => ({
                url: `leave-requests/${id}/cancel`,
                method: 'PUT',
            }),
            invalidatesTags: [
                { type: 'LeaveRequest', id: 'LIST' },
                { type: 'LeaveRequest', id: 'PENDING' },
                { type: 'LeaveRequest', id: 'STATS' },
            ],
        }),
        getAttendancesByStoreHistory: builder.query<unknown, { storeId: string; startDate: string; endDate: string; page?: number; size?: number }>({
            query: ({ storeId, startDate, endDate, page = 0, size = 10 }) => ({
                url: `attendances/store/${storeId}/history?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}&page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Attendance', id: 'LIST' }],
        }),
        getContracts: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `contracts?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Contract', id: 'LIST' }],
        }),
        getContractsByEmployee: builder.query<unknown, { employeeId: string; page?: number; size?: number }>({
            query: ({ employeeId, page = 0, size = 10 }) => ({
                url: `contracts/employee/${employeeId}?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: (_res, _err, arg) => [{ type: 'Contract', id: `EMPLOYEE_${arg.employeeId}` }],
        }),
        getContractStats: builder.query<unknown, void>({
            query: () => ({ url: 'contracts/stats', method: 'GET' }),
            providesTags: [{ type: 'Contract', id: 'STATS' }],
        }),
        getActiveContracts: builder.query<unknown, { page?: number; size?: number }>({
            query: ({ page = 0, size = 10 } = {}) => ({
                url: `contracts/active?page=${page}&size=${size}`,
                method: 'GET',
            }),
        }),
        createContract: builder.mutation<unknown, FormData>({
            query: (body) => ({
                url: `contracts`,
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'Contract', id: 'LIST' }, { type: 'Contract', id: 'STATS' }],
        }),
        updateContract: builder.mutation<unknown, { id: string; body: FormData }>({
            query: ({ id, body }) => ({
                url: `contracts/${id}`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: [{ type: 'Contract', id: 'LIST' }, { type: 'Contract', id: 'STATS' }],
        }),
        getDegreesByEmployee: builder.query<unknown, { employeeId: string; page?: number; size?: number }>({
            query: ({ employeeId, page = 0, size = 10 }) => ({
                url: `degrees/employee/${employeeId}?page=${page}&size=${size}`,
                method: 'GET',
            }),
            providesTags: (_res, _err, arg) => [{ type: 'Degree', id: `EMPLOYEE_${arg.employeeId}` }],
        }),
        createDegree: builder.mutation<unknown, FormData>({
            query: (body) => ({
                url: 'degrees',
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'Degree', id: 'LIST' }],
        }),
        getInsuranceRegistrationsByEmployee: builder.query<unknown, string>({
            query: (employeeId) => ({
                url: `insurances/registrations/employee/${employeeId}`,
                method: 'GET',
            }),
            providesTags: (_res, _err, id) => [{ type: 'Insurance', id: `EMPLOYEE_${id}` }],
        }),
        getStores: builder.query<unknown, void>({
            query: () => ({ url: 'stores', method: 'GET' }),
            providesTags: [{ type: 'Store', id: 'LIST' }],
        }),
        createStore: builder.mutation<unknown, CreateStoreRequest>({
            query: (body) => ({
                url: 'stores',
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'Store', id: 'LIST' }, { type: 'Store', id: 'SEARCH' }],
        }),
        updateStore: builder.mutation<unknown, { id: string; body: UpdateStoreRequest }>({
            query: ({ id, body }) => ({
                url: `stores/${id}`,
                method: 'PUT',
                body,
            }),
            invalidatesTags: [{ type: 'Store', id: 'LIST' }, { type: 'Store', id: 'SEARCH' }],
        }),
        deleteStore: builder.mutation<unknown, string>({
            query: (id) => ({
                url: `stores/${id}`,
                method: 'DELETE',
                body: {},
            }),
            invalidatesTags: [{ type: 'Store', id: 'LIST' }, { type: 'Store', id: 'SEARCH' }],
        }),
        getPayrollsByMonthYear: builder.query<ApiResponse<PayrollSummary[]>, { month: number; year: number }>({
            query: ({ month, year }) => ({
                url: `payrolls/month/${month}/year/${year}`,
                method: 'GET',
            }),
            providesTags: [{ type: 'Payroll', id: 'LIST' }],
        }),
        autoGeneratePayrolls: builder.mutation<ApiResponse<PayrollSummary[]>, AutoGeneratePayrollRequest>({
            query: (body) => ({
                url: 'payrolls/auto-generate',
                method: 'POST',
                body,
            }),
            invalidatesTags: [{ type: 'Payroll', id: 'LIST' }],
        }),
    }),
    overrideExisting: false,
});

export const {
    useGetEmployeesQuery,
    useGetEmployeesByStoreQuery,
    useSearchEmployeesQuery,
    useSearchStoresQuery,
    useSearchDepartmentsQuery,
    useGetEmployeeByIdQuery,
    useCreateEmployeeMutation,
    useUpdateEmployeeMutation,
    useGetMyAttendanceHistoryQuery,
        useGetAttendanceHistoryByEmployeeQuery,
    useGetMyAttendanceTodayQuery,
    useCheckInMutation,
    useCheckOutMutation,
    useGetWorkShiftsQuery,
    useGetEnabledWorkShiftsQuery,
    useCreateWorkShiftMutation,
    useUpdateWorkShiftMutation,
    useDeleteWorkShiftMutation,
    useGetMyLeaveRequestsQuery,
    useGetLeaveRequestsQuery,
    useGetPendingLeaveRequestsQuery,
    useApproveLeaveRequestMutation,
    useCancelLeaveRequestMutation,
    useGetContractsQuery,
    useGetContractsByEmployeeQuery,
    useGetActiveContractsQuery,
    useCreateContractMutation,
    useUpdateContractMutation,
    useGetEmployeeStatsQuery,
    useGetContractStatsQuery,
    useGetLeaveRequestStatsQuery,
    useGetAttendanceStatsQuery,
    useGetAttendancesByStoreHistoryQuery,
    useGetWorkShiftStatsQuery,
    useGetDegreesByEmployeeQuery,
    useCreateDegreeMutation,
    useGetInsuranceRegistrationsByEmployeeQuery,
    useGetStoresQuery,
    useCreateStoreMutation,
    useUpdateStoreMutation,
    useDeleteStoreMutation,
    useGetPayrollsByMonthYearQuery,
    useAutoGeneratePayrollsMutation,
} = hrApi;
