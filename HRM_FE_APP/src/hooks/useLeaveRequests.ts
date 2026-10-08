import { useState } from 'react';
import { Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useGetMyLeaveRequestsQuery, useCancelLeaveRequestMutation } from '../store/api/leaveApi';
import type { LeaveStatus } from '../types/leave';

export const useLeaveRequests = () => {
  const navigation = useNavigation<any>();
  const [filterStatus, setFilterStatus] = useState<LeaveStatus | 'all'>('all');

  const { data, isLoading, isError, refetch } = useGetMyLeaveRequestsQuery(undefined);
  const [cancelLeaveRequest] = useCancelLeaveRequestMutation();

  const allRequests = data?.data ?? [];
  const filteredRequests =
    filterStatus === 'all'
      ? allRequests
      : allRequests.filter((r) => r.status === filterStatus);

  const getStatusColor = (status: LeaveStatus) => {
    switch (status) {
      case 'PENDING':
        return { bg: '#FEF3C7', text: '#D97706' };
      case 'APPROVED':
        return { bg: '#ECFDF5', text: '#10B981' };
      case 'REJECTED':
        return { bg: '#FEE2E2', text: '#EF4444' };
      case 'CANCELLED':
        return { bg: '#F1F5F9', text: '#94A3B8' };
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'ANNUAL_LEAVE':
        return 'sunny';
      case 'SICK_LEAVE':
        return 'medkit';
      case 'COMPENSATORY_LEAVE':
        return 'swap-horizontal';
      case 'UNPAID_LEAVE':
        return 'cash-outline';
      default:
        return 'document-text';
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  const handleCancel = (id: string) => {
    Alert.alert('Xác nhận', 'Bạn có chắc muốn hủy đơn này?', [
      { text: 'Không', style: 'cancel' },
      {
        text: 'Hủy đơn',
        style: 'destructive',
        onPress: async () => {
          try {
            await cancelLeaveRequest(id).unwrap();
          } catch {
            Alert.alert('Lỗi', 'Không thể hủy đơn. Vui lòng thử lại.');
          }
        },
      },
    ]);
  };

  const navigateToCreate = () => navigation.navigate('CreateLeaveRequest');

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
    filterStatus,
    setFilterStatus,
    filteredRequests,
    isLoading,
    isError,
    refetch,
    getStatusColor,
    getTypeIcon,
    formatDate,
    handleCancel,
    navigateToCreate,
    refreshing,
    onRefresh,
  };
};
