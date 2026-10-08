import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQuery } from './baseQuery';
import type { ApiResponse } from '../../types/common';
import type { AttendanceRecord } from '../../types/attendance';

// ── Face registration response types ─────────────────────────────────
// Response khi enqueue job (PUT /register trả về ngay)  
export interface FaceRegisterEnqueueData {
  jobId: string;
  employeeId: string;
  message: string;
}

// Response khi polling job status (GET /register-face/status/{jobId})
export type JobStatus = 'pending' | 'processing' | 'completed' | 'failed';

export interface FaceRegisterJobResult {
  message: string;
  employeeId: string;
  indexUpdated: boolean;
  anglesCaptured: string[];
}

export interface FaceRegisterJobStatusData {
  jobId: string;
  status: JobStatus;
  created_at?: string;
  updated_at?: string;
  result?: FaceRegisterJobResult;
  error?: string;
  progress?: string;
}

export interface FaceStatusData {
  registered: boolean;
  employeeId: string;
}

// ── Attendance check-in/out params (sent to Java with face photo) ────
export interface AttendanceWithFaceParams {
  photoUri: string;
  latitude: number;
  longitude: number;
  note?: string;
}

export const attendanceApi = createApi({
  reducerPath: 'attendanceApi',
  baseQuery,
  tagTypes: ['AttendanceToday', 'AttendanceHistory', 'FaceStatus'],
  endpoints: (builder) => ({
    getAttendanceToday: builder.query<ApiResponse<AttendanceRecord | null>, void>({
      query: () => '/attendances/me/today',
      providesTags: ['AttendanceToday'],
    }),

    getAttendanceHistory: builder.query<
      ApiResponse<AttendanceRecord[]>,
      { startDate: string; endDate: string }
    >({
      query: ({ startDate, endDate }) =>
        `/attendances/me/history?startDate=${startDate}&endDate=${endDate}`,
      providesTags: ['AttendanceHistory'],
    }),

    // ── Check-in with face verification (Java handles everything) ────
    checkIn: builder.mutation<ApiResponse<AttendanceRecord>, AttendanceWithFaceParams>({
      query: ({ photoUri, latitude, longitude, note }) => {
        const formData = new FormData();
        formData.append('photo', {
          uri: photoUri,
          type: 'image/jpeg',
          name: 'face.jpg',
        } as any);
        formData.append('latitude', latitude.toString());
        formData.append('longitude', longitude.toString());
        if (note) formData.append('note', note);
        return {
          url: '/attendances/check-in',
          method: 'POST',
          body: formData,
        };
      },
      invalidatesTags: ['AttendanceToday', 'AttendanceHistory'],
    }),

    // ── Check-out with face verification (Java handles everything) ───
    checkOut: builder.mutation<ApiResponse<AttendanceRecord>, AttendanceWithFaceParams>({
      query: ({ photoUri, latitude, longitude, note }) => {
        const formData = new FormData();
        formData.append('photo', {
          uri: photoUri,
          type: 'image/jpeg',
          name: 'face.jpg',
        } as any);
        formData.append('latitude', latitude.toString());
        formData.append('longitude', longitude.toString());
        if (note) formData.append('note', note);
        return {
          url: '/attendances/check-out',
          method: 'POST',
          body: formData,
        };
      },
      invalidatesTags: ['AttendanceToday', 'AttendanceHistory'],
    }),

    // ── Face registration (Python internal API via reverse proxy) ────
    getFaceStatus: builder.query<ApiResponse<FaceStatusData>, void>({
      query: () => `/face/status`,
      providesTags: ['FaceStatus'],
    }),

    registerFace: builder.mutation<ApiResponse<FaceRegisterEnqueueData>, { videoUri: string }>({
      query: ({ videoUri }) => {
        const formData = new FormData();
        formData.append('video', {
          uri: videoUri,
          type: 'video/mp4',
          name: 'face_register.mp4',
        } as any);
        return {
          url: '/face/register',
          method: 'PUT',
          body: formData,
        };
      },
    }),

    // ── Job status polling ───────────────────────────────────────────
    getRegisterFaceJobStatus: builder.query<ApiResponse<FaceRegisterJobStatusData>, string>({
      query: (jobId) => `/face/register/status/${jobId}`,
    }),

    /** GET /attendances/store/:storeId/today */
    getStoreAttendanceToday: builder.query<ApiResponse<AttendanceRecord[]>, string>({
      query: (storeId) => `/attendances/store/${storeId}/today`,
      providesTags: ['AttendanceHistory'],
    }),
  }),
});

export const {
  useGetAttendanceTodayQuery,
  useGetAttendanceHistoryQuery,
  useCheckInMutation,
  useCheckOutMutation,
  useGetFaceStatusQuery,
  useRegisterFaceMutation,
  useLazyGetRegisterFaceJobStatusQuery,
  useGetStoreAttendanceTodayQuery,
} = attendanceApi;
