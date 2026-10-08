import { useState, useRef, useCallback } from 'react';
import {
  useRegisterFaceMutation,
  useLazyGetRegisterFaceJobStatusQuery,
  type JobStatus,
  type FaceRegisterJobResult,
} from '../store/api/attendanceApi';

/** Polling interval in ms */
const POLL_INTERVAL = 2000;
/** Maximum polling time before timeout (ms) */
const POLL_TIMEOUT = 120_000; // 2 phút

export interface RegisterFacePollingState {
  /** Bước hiện tại */
  phase: 'idle' | 'uploading' | 'polling' | 'completed' | 'failed';
  /** Trạng thái job từ server */
  jobStatus: JobStatus | null;
  /** Job ID để FE theo dõi */
  jobId: string | null;
  /** Kết quả khi completed */
  result: FaceRegisterJobResult | null;
  /** Thông báo lỗi */
  error: string | null;
  /** Mô tả tiến trình từ server */
  progress: string | null;
}

const INITIAL_STATE: RegisterFacePollingState = {
  phase: 'idle',
  jobStatus: null,
  jobId: null,
  result: null,
  error: null,
  progress: null,
};

/**
 * Hook xử lý đăng ký khuôn mặt với Redis queue polling.
 *
 * Flow:
 * 1. Gọi PUT /register-face → nhận jobId ngay lập tức
 * 2. Polling GET /register-face/status/{jobId} mỗi 2s
 * 3. Dừng khi status = completed | failed | timeout
 */
export function useRegisterFacePolling() {
  const [state, setState] = useState<RegisterFacePollingState>(INITIAL_STATE);
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const [registerFace] = useRegisterFaceMutation();
  const [fetchJobStatus] = useLazyGetRegisterFaceJobStatusQuery();

  /** Dọn dẹp timer */
  const clearTimers = useCallback(() => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, []);

  /** Check job status manually */
  const checkJobStatus = useCallback(async () => {
    if (!state.jobId) return;

    try {
      setState((prev) => ({ ...prev, progress: 'Đang kiểm tra...' }));
      const res = await fetchJobStatus(state.jobId, false).unwrap();
      const data = res.data;

      if (!data) return;

      const { status, result, error, progress } = data;

      if (status === 'completed') {
        clearTimers();
        setState({
          phase: 'completed',
          jobStatus: 'completed',
          jobId: state.jobId,
          result: result ?? null,
          error: null,
          progress: null,
        });
        return;
      }

      if (status === 'failed') {
        clearTimers();
        setState({
          phase: 'failed',
          jobStatus: 'failed',
          jobId: state.jobId,
          result: null,
          error: error ?? 'Đăng ký khuôn mặt thất bại.',
          progress: null,
        });
        return;
      }

      // pending | processing
      setState((prev) => ({
        ...prev,
        jobStatus: status,
        progress: progress ?? (status === 'processing' ? 'Đang phân tích khuôn mặt...' : 'Đang chờ xử lý...'),
      }));
    } catch {
      setState((prev) => ({ ...prev, progress: 'Lỗi mạng, vui lòng thử lại.' }));
    }
  }, [fetchJobStatus, state.jobId, clearTimers]);

  /** Bắt đầu theo dõi thủ công */
  const startTracking = useCallback(
    (jobId: string) => {
      clearTimers();

      setState((prev) => ({
        ...prev,
        phase: 'polling',
        jobStatus: 'pending',
        progress: 'Đã gửi yêu cầu, vui lòng chờ...',
      }));

      // Gọi lần đầu để lấy status
      fetchJobStatus(jobId, false).unwrap().then(res => {
        const data = res.data;
        if (data && (data.status === 'completed' || data.status === 'failed')) {
          checkJobStatus();
        }
      }).catch(() => {});
    },
    [fetchJobStatus, clearTimers, checkJobStatus],
  );

  /** Hàm chính: upload video → nhận jobId → bắt đầu tracking thủ công */
  const submitRegistration = useCallback(
    async (videoUri: string) => {
      try {
        // Reset
        setState({
          ...INITIAL_STATE,
          phase: 'uploading',
        });

        // 1. Upload video → nhận jobId ngay
        const res = await registerFace({ videoUri }).unwrap();

        if (!res.success || !res.data?.jobId) {
          setState({
            ...INITIAL_STATE,
            phase: 'failed',
            error: res.message || 'Không thể gửi yêu cầu đăng ký.',
          });
          return;
        }

        const jobId = res.data.jobId;

        setState((prev) => ({
          ...prev,
          jobId,
        }));

        // 2. Bắt đầu tracking
        startTracking(jobId);
      } catch (error: any) {
        const msg =
          error?.data?.message ??
          error?.data?.errors?.[0] ??
          error?.message ??
          'Có lỗi xảy ra khi gửi yêu cầu.';
        setState({
          ...INITIAL_STATE,
          phase: 'failed',
          error: msg,
        });
      }
    },
    [registerFace, startTracking, checkJobStatus],
  );

  /** Reset về trạng thái ban đầu */
  const reset = useCallback(() => {
    clearTimers();
    setState(INITIAL_STATE);
  }, [clearTimers]);

  return {
    ...state,
    submitRegistration,
    checkJobStatus,
    reset,
    /** true khi đang upload hoặc theo dõi */
    isProcessing: state.phase === 'uploading' || state.phase === 'polling',
  };
}
