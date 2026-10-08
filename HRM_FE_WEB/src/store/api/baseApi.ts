import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';

export const baseApi = createApi({
    reducerPath: 'api',
    baseQuery,
    tagTypes: [
        'User',
        'Auth',
        'Role',
        'Permission',
        'UserRole',
        'Employee',
        'WorkShift',
        'Contract',
        'LeaveRequest',
        'Attendance',
        'Store',
        'Degree',
        'Insurance',
        'Payroll',
    ],
    endpoints: () => ({}),
});

