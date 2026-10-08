import { useState } from 'react';
import { Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useCreateLeaveRequestMutation } from '../store/api/leaveApi';
import type { LeaveType, CreateLeaveRequestPayload } from '../types/leave';

/**
 * Format date to DD/MM/YYYY for display.
 */
export const formatDateDisplay = (date: Date): string => {
  const d = date.getDate().toString().padStart(2, '0');
  const m = (date.getMonth() + 1).toString().padStart(2, '0');
  const y = date.getFullYear();
  return `${d}/${m}/${y}`;
};

/**
 * Format date to YYYY-MM-DD for API.
 */
const formatDateApi = (date: Date): string => {
  return date.toISOString().split('T')[0];
};

export const useCreateLeaveRequest = () => {
  const navigation = useNavigation<any>();
  const [createLeaveRequest, { isLoading }] = useCreateLeaveRequestMutation();

  const [selectedType, setSelectedType] = useState<LeaveType>('ANNUAL_LEAVE');
  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(new Date());
  const [reason, setReason] = useState('');

  const validate = (): boolean => {
    if (!startDate) {
      Alert.alert('Lỗi', 'Vui lòng chọn ngày bắt đầu');
      return false;
    }
    if (endDate < startDate) {
      Alert.alert('Lỗi', 'Ngày kết thúc không thể trước ngày bắt đầu');
      return false;
    }
    if (!reason.trim()) {
      Alert.alert('Lỗi', 'Vui lòng nhập lý do');
      return false;
    }
    return true;
  };

  const handleSubmit = async () => {
    if (!validate()) return;

    const payload: CreateLeaveRequestPayload = {
      leaveType: selectedType,
      startDate: formatDateApi(startDate),
      endDate: formatDateApi(endDate),
      reason: reason.trim(),
    };

    try {
      await createLeaveRequest(payload).unwrap();
      Alert.alert('Thành công', 'Đơn đã được gửi, chờ duyệt.', [
        { text: 'OK', onPress: () => navigation.goBack() },
      ]);
    } catch (err: any) {
      const message =
        err?.data?.message || err?.data?.error || 'Gửi đơn thất bại. Vui lòng thử lại.';
      Alert.alert('Lỗi', message);
    }
  };

  const goBack = () => navigation.goBack();

  return {
    selectedType,
    setSelectedType,
    startDate,
    setStartDate,
    endDate,
    setEndDate,
    reason,
    setReason,
    isLoading,
    handleSubmit,
    goBack,
    formatDateDisplay,
  };
};
