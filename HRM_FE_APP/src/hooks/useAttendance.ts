import { useState, useEffect, useMemo } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import * as ImagePicker from 'expo-image-picker';
import * as Location from 'expo-location';
import {
  useGetAttendanceTodayQuery,
  useGetAttendanceHistoryQuery,
  useCheckInMutation,
  useCheckOutMutation,
  useGetFaceStatusQuery,
  useGetStoreAttendanceTodayQuery,
} from '../store/api/attendanceApi';
import { useRole } from './useRole';
import { useGetMyProfileQuery } from '../store/api/employeeApi';
import { useGetMyShiftsTodayQuery } from '../store/api/workshiftApi';
import { formatShiftTime } from '../utils';
import type { CheckInFlowStatus } from '../types/attendance';
import type { EmployeeWorkShift } from '../types/workshift';

export interface UpcomingShiftInfo {
  name: string;
  startTime: string;
  endTime: string;
  isActive: boolean;
  minutesUntilStart: number;
}

export const useAttendance = () => {
  const navigation = useNavigation<any>();
  const { isManager, storeId } = useRole();
  const [currentTime, setCurrentTime] = useState(new Date());
  const [isProcessing, setIsProcessing] = useState(false);

  // RTK Query hooks
  const { data: todayData, isLoading: todayLoading, refetch: refetchToday } =
    useGetAttendanceTodayQuery(undefined, { skip: isManager });

  const { data: storeTodayData, isLoading: storeTodayLoading, refetch: refetchStoreToday } =
    useGetStoreAttendanceTodayQuery(storeId || '', { skip: !isManager || !storeId });

  // History: last 30 days
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
  const today = new Date().toISOString().split('T')[0];
  const startDate = thirtyDaysAgo.toISOString().split('T')[0];

  const { data: historyData, isLoading: historyLoading, refetch: refetchHistory } = useGetAttendanceHistoryQuery(
    { startDate: startDate, endDate: today },
    { skip: isManager }
  );

  const { user } = useSelector((state: RootState) => state.auth);
  // Employee profile — needed for more details, but we can get ID from user object
  const { data: profileData, refetch: refetchProfile } = useGetMyProfileQuery();
  const employeeId = user?.employee?.id || profileData?.data?.id;

  // Face registration status
  const { data: faceStatusData, isLoading: faceStatusLoading, refetch: refetchFaceStatus } = useGetFaceStatusQuery(undefined, {
    skip: !employeeId,
  });
  const isFaceRegistered = faceStatusData?.data?.registered ?? false;
  const shouldShowFaceRegistration = Boolean(employeeId) && !faceStatusLoading && !isFaceRegistered;

  // My shift assignments — for upcoming shift display
  const { data: myShiftsData, refetch: refetchShifts } = useGetMyShiftsTodayQuery();
  const myAssignments = myShiftsData?.data ?? [];

  // Face recognition mutations (now go through Java API)
  const [checkInTrigger] = useCheckInMutation();
  const [checkOutTrigger] = useCheckOutMutation();

  // Live clock
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const timeStr = currentTime.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });

  const dateStr = currentTime.toLocaleDateString('vi-VN', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  const todayRecord = todayData?.data;

  // Derive check-in flow status from backend data
  // After checkout, allow check-in again (for multi-shift days)
  const flowStatus: CheckInFlowStatus = !todayRecord
    ? 'not_checked'
    : todayRecord.checkOutTime
      ? 'checked_out'
      : todayRecord.checkInTime
        ? 'checked_in'
        : 'not_checked';

  const isCheckedIn = flowStatus === 'checked_in';
  const isCheckedOut = flowStatus === 'checked_out';

  // ── Upcoming / current shift computation ──────────────────────────
  const upcomingShift = useMemo((): UpcomingShiftInfo | null => {
    if (myAssignments.length === 0) return null;

    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];
    const nowMinutes = now.getHours() * 60 + now.getMinutes();

    // Filter assignments applicable today (match by date)
    const todayAssignments = myAssignments.filter((a: EmployeeWorkShift) => {
      if (a.date && todayStr !== a.date) return false;
      return true;
    });

    if (todayAssignments.length === 0) return null;

    // Sort by shift start time
    const sorted = [...todayAssignments].sort((a, b) => {
      const [ah, am] = a.shiftStartTime.split(':').map(Number);
      const [bh, bm] = b.shiftStartTime.split(':').map(Number);
      return ah * 60 + am - (bh * 60 + bm);
    });

    // Find the next upcoming or currently active shift
    for (const assignment of sorted) {
      const [sh, sm] = assignment.shiftStartTime.split(':').map(Number);
      const [eh, em] = assignment.shiftEndTime.split(':').map(Number);
      const startMin = sh * 60 + sm;
      const endMin = eh * 60 + em;

      // Still relevant if current time < shift end + 30 min buffer
      if (nowMinutes < endMin + 30) {
        const isActive = nowMinutes >= startMin - 30 && nowMinutes <= endMin;
        return {
          name: assignment.workShiftName,
          startTime: formatShiftTime(assignment.shiftStartTime),
          endTime: formatShiftTime(assignment.shiftEndTime),
          isActive,
          minutesUntilStart: Math.max(0, startMin - nowMinutes),
        };
      }
    }

    return null;
  }, [myAssignments, currentTime]);

  // After checkout, allow checkin again if there's an upcoming shift (multi-shift days)
  const canCheckIn = flowStatus === 'not_checked' || (isCheckedOut && upcomingShift != null);
  const canCheckOut = isCheckedIn;
  const isAllDone = isCheckedOut && upcomingShift == null;

  const historyRecords = isManager 
    ? (storeTodayData?.data || [])
    : (historyData?.data || []).slice(0, 5);

  // ── Camera + GPS + Face API flow ──────────────────────────────────
  const captureAndSend = async (mode: 'checkin' | 'checkout') => {
    if (!employeeId) {
      Alert.alert('Lỗi', 'Không thể xác định thông tin nhân viên. Vui lòng thử lại.');
      return;
    }

    try {
      setIsProcessing(true);

      // Request camera permission
      const cameraPerm = await ImagePicker.requestCameraPermissionsAsync();
      if (!cameraPerm.granted) {
        Alert.alert('Quyền truy cập', 'Cần quyền truy cập camera để chấm công bằng khuôn mặt.');
        return;
      }

      // Request location permission
      const { status: locStatus } = await Location.requestForegroundPermissionsAsync();
      if (locStatus !== 'granted') {
        Alert.alert('Quyền truy cập', 'Cần quyền truy cập vị trí để chấm công.');
        return;
      }

      // Launch front camera
      const photo = await ImagePicker.launchCameraAsync({
        mediaTypes: ['images'],
        quality: 0.7,
        cameraType: ImagePicker.CameraType.front,
      });

      if (photo.canceled) return;

      const photoUri = photo.assets[0].uri;

      // Get GPS coordinates
      const location = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.High,
      });
      const { latitude, longitude } = location.coords;

      // Call Java attendance API (face verification is handled server-side)
      const result =
        mode === 'checkin'
          ? await checkInTrigger({ photoUri, latitude, longitude }).unwrap()
          : await checkOutTrigger({ photoUri, latitude, longitude }).unwrap();

      if (!result.success) {
        Alert.alert('Lỗi', result.message || 'Chấm công thất bại');
      } else {
        const shiftInfo = result.data?.workShift
          ? `\nCa: ${result.data.workShift.name}`
          : '';
        Alert.alert(
          'Thành công',
          (result.message || (mode === 'checkin' ? 'Check-in thành công' : 'Check-out thành công')) +
            shiftInfo,
        );
      }
    } catch (error: any) {
      let msg = error?.data?.message ?? error?.data?.errors?.[0] ?? error?.message ?? 'Có lỗi xảy ra khi chấm công';
      if (typeof msg === 'string' && msg.includes(' - ')) {
        // Remove error code prefix like "VALIDATION_ERROR - "
        msg = msg.split(' - ').slice(1).join(' - ');
      }
      Alert.alert('Lỗi', msg);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleCheckIn = () => {
    Alert.alert(
      'Chấm công vào',
      'Bạn sẽ chụp ảnh khuôn mặt để xác thực chấm công. Tiếp tục?',
      [
        { text: 'Hủy', style: 'cancel' },
        { text: 'Tiếp tục', onPress: () => captureAndSend('checkin') },
      ],
    );
  };

  const handleCheckOut = () => {
    Alert.alert(
      'Chấm công ra',
      'Bạn sẽ chụp ảnh khuôn mặt để xác thực chấm công. Tiếp tục?',
      [
        { text: 'Hủy', style: 'cancel' },
        { text: 'Tiếp tục', style: 'destructive', onPress: () => captureAndSend('checkout') },
      ],
    );
  };

  const navigateToHistory = () => navigation.navigate('AttendanceHistory');
  const navigateToFaceRegistration = () => navigation.navigate('FaceRegistration');

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      const promises = [refetchProfile(), refetchShifts(), refetchFaceStatus()];
      if (isManager) {
        promises.push(refetchStoreToday());
      } else {
        promises.push(refetchToday(), refetchHistory());
      }
      await Promise.all(promises);
    } finally {
      setRefreshing(false);
    }
  };

  return {
    timeStr,
    dateStr,
    todayRecord,
    todayLoading: isManager ? storeTodayLoading : todayLoading,
    historyLoading,
    isProcessing,
    canCheckIn,
    canCheckOut,
    isCheckedOut,
    isAllDone,
    shouldShowFaceRegistration,
    historyRecords,
    handleCheckIn,
    handleCheckOut,
    navigateToHistory,
    navigateToFaceRegistration,
    upcomingShift,
    isManager,
    storeAttendance: storeTodayData?.data || [],
    refreshing,
    onRefresh,
  };
};
