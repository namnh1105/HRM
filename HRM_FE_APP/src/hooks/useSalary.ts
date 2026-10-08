import { useState } from 'react';
import { useNavigation } from '@react-navigation/native';
import { useGetMyPayrollsQuery, useGetPayrollsByStoreQuery } from '../store/api/payrollApi';
import { useRole } from './useRole';
import type { SalaryDetail } from '../types/payroll';

const formatVND = (amount: number) => amount.toLocaleString('vi-VN') + ' ₫';

export const useSalary = () => {
  const navigation = useNavigation<any>();
  const { isManager, storeId } = useRole();

  const {
    data: myPayrollsData,
    isLoading,
    isError,
    refetch,
  } = useGetMyPayrollsQuery(undefined);

  const salaryHistory: SalaryDetail[] = myPayrollsData?.data ?? [];
  const [selectedIndex, setSelectedIndex] = useState(0);

  const selected: SalaryDetail | undefined = salaryHistory[selectedIndex];

  const monthLabel = (s: SalaryDetail) =>
    `Tháng ${String(s.month).padStart(2, '0')}/${s.year}`;

  const goBack = () => navigation.goBack();

  const totalPayroll = isManager
    ? salaryHistory.reduce((acc, curr) => acc + curr.netSalary, 0)
    : 0;

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
    salaryHistory,
    isLoading,
    isError,
    selectedIndex,
    setSelectedIndex,
    selected,
    monthLabel,
    formatVND,
    goBack,
    isManager,
    totalPayroll,
    refreshing,
    onRefresh,
  };
};

export { formatVND };
