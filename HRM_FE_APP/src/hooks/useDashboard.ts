import { useState } from 'react';
import { useNavigation } from '@react-navigation/native';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { useGetAttendanceTodayQuery } from '../store/api/attendanceApi';
import { useGetMyLeaveRequestsQuery } from '../store/api/leaveApi';
import { useGetMyPayrollsQuery } from '../store/api/payrollApi';
import { useGetMyNotificationsQuery, useGetUnreadCountQuery } from '../store/api/notificationApi';

export const useDashboard = () => {
  const navigation = useNavigation<any>();
  const { user } = useSelector((state: RootState) => state.auth);

  const { data: notificationRes, refetch: refetchNotifications } = useGetMyNotificationsQuery({ page: 0, size: 5 });
  const { data: unreadCountRes, refetch: refetchUnreadCount } = useGetUnreadCountQuery();
  
  const allNotifications = notificationRes?.data?.content ?? [];
  const unreadCount = unreadCountRes?.data ?? 0;
  const notifications = allNotifications;

  const { data: attendanceRes, isLoading: isAttendanceLoading, refetch: refetchAttendance } =
    useGetAttendanceTodayQuery(undefined);
  const { data: leaveRes, isLoading: isLeaveLoading, refetch: refetchLeave } =
    useGetMyLeaveRequestsQuery(undefined);
  const { data: payrollRes, isLoading: isPayrollLoading, refetch: refetchPayroll } =
    useGetMyPayrollsQuery(undefined);

  const todayRecord = attendanceRes?.data ?? null;

  const derivedStatus = todayRecord
    ? todayRecord.checkOutTime
      ? 'checked_out'
      : todayRecord.checkInTime
        ? 'checked_in'
        : 'not_checked'
    : 'not_checked';

  const getStatusLabel = () => {
    switch (derivedStatus) {
      case 'checked_in':
        return 'Đang làm việc';
      case 'checked_out':
        return 'Đã tan ca';
      default:
        return 'Chưa chấm công';
    }
  };

  const getStatusColor = () => {
    switch (derivedStatus) {
      case 'checked_in':
        return '#10B981';
      case 'checked_out':
        return '#6B7280';
      default:
        return '#F59E0B';
    }
  };

  

  const recentNotifications = notifications.filter((n) => !n.isRead).slice(0, 3);

  const isManager = user?.roles?.includes('MANAGER');

  const allActions = [
    {
      icon: 'finger-print' as const,
      label: 'Chấm công',
      color: '#3B82F6',
      bg: '#EFF6FF',
      onPress: () => navigation.navigate('Attendance'),
    },
    {
      icon: 'document-text' as const,
      label: 'Xin nghỉ',
      color: '#8B5CF6',
      bg: '#F5F3FF',
      onPress: () => navigation.navigate('CreateLeaveRequest'),
    },
    {
      id: 'salary',
      icon: 'wallet' as const,
      label: 'Xem lương',
      color: '#10B981',
      bg: '#ECFDF5',
      onPress: () => navigation.navigate('Salary'),
    },
    {
      icon: 'calendar' as const,
      label: 'Lịch làm việc',
      color: '#F59E0B',
      bg: '#FFFBEB',
      onPress: () => navigation.navigate('WorkSchedule'),
    },
  ];

  const quickActions = allActions;

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'attendance':
        return 'time';
      case 'leave':
        return 'document-text';
      case 'salary':
        return 'wallet';
      case 'hr':
        return 'people';
      default:
        return 'notifications';
    }
  };

  const storeName = user?.employee?.storeName || 'Hệ thống';
  const navigateToNotifications = () => navigation.navigate('Notifications');

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await Promise.all([
        refetchNotifications(),
        refetchUnreadCount(),
        refetchAttendance(),
        refetchLeave(),
        refetchPayroll(),
      ]);
    } finally {
      setRefreshing(false);
    }
  };

  return {
    user,
    todayRecord,
    isAttendanceLoading,
    unreadCount,
    recentNotifications,
    quickActions,
    storeName,
    getStatusLabel,
    getStatusColor,
    getNotificationIcon,
    navigateToNotifications,
    refreshing,
    onRefresh,
  };
};
