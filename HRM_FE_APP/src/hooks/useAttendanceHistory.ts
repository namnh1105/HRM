import { useMemo, useState } from 'react';
import { useNavigation } from '@react-navigation/native';
import { useGetAttendanceHistoryQuery } from '../store/api/attendanceApi';
import type { AttendanceRecord } from '../types/attendance';

export const useAttendanceHistory = () => {
  const navigation = useNavigation<any>();

  const dateRange = useMemo(() => {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 90);
    return {
      startDate: start.toISOString().split('T')[0],
      endDate: end.toISOString().split('T')[0],
    };
  }, []);

  const { data, isLoading, isError, refetch } = useGetAttendanceHistoryQuery(dateRange);
  const history = data?.data ?? [];

  const getStatusInfo = (record: AttendanceRecord) => {
    if (record.checkOutTime) {
      return { label: 'Hoàn thành', color: '#10B981', bg: '#ECFDF5' };
    }
    if (record.checkInTime) {
      return { label: 'Đang làm', color: '#3B82F6', bg: '#EFF6FF' };
    }
    return { label: 'Chưa chấm', color: '#F59E0B', bg: '#FEF3C7' };
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    const dayName = dayNames[date.getDay()];
    return {
      dayName,
      day: String(date.getDate()).padStart(2, '0'),
      month: String(date.getMonth() + 1).padStart(2, '0'),
    };
  };

  // Summary stats
  const presentCount = history.filter((r) => r.status === 'PRESENT').length;
  const totalHours = history.reduce((sum, r) => sum + (r.workingHours || 0), 0).toFixed(1);
  const absentCount = history.filter((r) => r.status === 'ABSENT').length;

  const goBack = () => navigation.goBack();

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await refetch();
    } finally {
      setRefreshing(false);
    }
  };

  return {
    history,
    isLoading,
    isError,
    refetch,
    getStatusInfo,
    formatDate,
    presentCount,
    totalHours,
    absentCount,
    goBack,
    refreshing,
    onRefresh,
  };
};
