import { useMemo, useState } from 'react';
import { useNavigation } from '@react-navigation/native';
import {
  useGetActiveWorkShiftsQuery,
  useGetMyAllShiftsQuery,
  useGetStoreWorkShiftsQuery,
  useGetAllActiveAssignmentsQuery,
  useAssignWorkShiftMutation,
  useDeleteWorkShiftMutation,
} from '../store/api/workshiftApi';
import { useRole } from './useRole';
import { formatShiftTime } from '../utils';
import type { WorkShift, EmployeeWorkShift } from '../types/workshift';

const SHIFT_COLORS = ['#3B82F6', '#8B5CF6', '#F59E0B', '#10B981', '#EF4444', '#EC4899'];
const DAY_NAMES = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];

export const getShiftColor = (index: number) => SHIFT_COLORS[index % SHIFT_COLORS.length];

/** Map shift id to a consistent color */
const getShiftColorByName = (shiftId: string, allShifts: WorkShift[]) => {
  const idx = allShifts.findIndex((s) => s.id === shiftId);
  return getShiftColor(idx >= 0 ? idx : 0);
};

/** Format local date as YYYY-MM-DD without timezone issues */
const toLocalDateStr = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

/** Parse date string (may contain 'T') to YYYY-MM-DD only */
export const normaliseDate = (s: string | null): string | null => {
  if (!s) return null;
  return s.split('T')[0];
};

export interface WeekDayShift {
  id: string;
  name: string;
  time: string;
  color: string;
  employees: string[];
  isUnderstaffed: boolean;
}

export interface WeekDay {
  dayName: string;
  date: string;
  fullDate: string;
  isWeekend: boolean;
  isToday: boolean;
  shifts: WeekDayShift[];
}

const buildWeekSchedule = (
  allShifts: WorkShift[],
  allAssignments: EmployeeWorkShift[],
  currentDate: Date,
): WeekDay[] => {
  const days: WeekDay[] = [];
  const today = new Date();
  const todayStr = toLocalDateStr(today);

  // Calculate Monday of the given currentDate week
  const startOfWeek = new Date(currentDate);
  const jsDay = currentDate.getDay(); // 0=Sun, 1=Mon, …, 6=Sat
  const mondayOffset = jsDay === 0 ? -6 : 1 - jsDay;
  startOfWeek.setDate(currentDate.getDate() + mondayOffset);

  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek);
    date.setDate(startOfWeek.getDate() + i);
    const dateStr = toLocalDateStr(date);
    const isWeekend = i >= 5;
    const isToday = dateStr === todayStr;

    // Find assignments that apply to this day (match by date)
    const dayAssignments = allAssignments.filter((a) => {
      const assignmentDate = normaliseDate(a.date);
      return assignmentDate === dateStr;
    });

    // Group by shiftId
    const shiftGroups: Record<string, WeekDayShift> = {};

    dayAssignments.forEach((a) => {
      if (!shiftGroups[a.workShiftId]) {
        shiftGroups[a.workShiftId] = {
          id: a.workShiftId,
          name: a.workShiftName,
          time: `${formatShiftTime(a.shiftStartTime)} - ${formatShiftTime(a.shiftEndTime)}`,
          color: getShiftColorByName(a.workShiftId, allShifts),
          employees: [],
          isUnderstaffed: false,
        };
      }
      shiftGroups[a.workShiftId].employees.push(a.employeeName);
    });

    // Mark understaffed (example logic: < 2 people)
    Object.values(shiftGroups).forEach(s => {
      s.isUnderstaffed = s.employees.length < 2;
    });

    days.push({
      dayName: DAY_NAMES[i],
      date: date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }),
      fullDate: dateStr,
      isWeekend,
      isToday,
      shifts: Object.values(shiftGroups),
    });
  }
  return days;
};

// Upcoming holidays (static)
export const HOLIDAYS = [
];

export const useWorkSchedule = () => {
  const navigation = useNavigation<any>();
  const { isManager, storeId } = useRole();

  const {
    data: shiftsData,
    isLoading: isLoadingShifts,
    error: shiftsError,
    refetch: refetchShifts,
  } = useGetActiveWorkShiftsQuery();

  const {
    data: myShiftsData,
    isLoading: isLoadingMyShifts,
    error: myShiftsError,
    refetch: refetchMyShifts,
  } = useGetMyAllShiftsQuery(undefined, { skip: isManager });

  const {
    data: storeShiftsData,
    isLoading: isLoadingStoreShifts,
    error: storeShiftsError,
    refetch: refetchStoreShifts,
  } = useGetStoreWorkShiftsQuery(storeId || '', { skip: !isManager || !storeId });

  const {
    data: allStoreShiftsData,
    isLoading: isLoadingAllStoreShifts,
    error: allStoreShiftsError,
    refetch: refetchAllStoreShifts,
  } = useGetAllActiveAssignmentsQuery(undefined, { skip: !isManager || !!storeId });

  console.log('[useWorkSchedule] isManager:', isManager, 'storeId:', storeId);

  const [assignShift, { isLoading: isAssigning }] = useAssignWorkShiftMutation();
  const [deleteShift, { isLoading: isDeleting }] = useDeleteWorkShiftMutation();

  const shifts = shiftsData?.data ?? [];
  const allAssignments = isManager
    ? (storeId ? (storeShiftsData?.data ?? []) : (allStoreShiftsData?.data ?? []))
    : (myShiftsData?.data ?? []);

  const isLoading = isLoadingShifts ||
    (isManager
      ? (storeId ? isLoadingStoreShifts : isLoadingAllStoreShifts)
      : isLoadingMyShifts);

  const error = shiftsError ||
    (isManager
      ? (storeId ? storeShiftsError : allStoreShiftsError)
      : myShiftsError);

  const [currentDate, setCurrentDate] = useState(new Date());

  const weekSchedule = useMemo(
    () => buildWeekSchedule(shifts, allAssignments, currentDate),
    [shifts, allAssignments, currentDate],
  );

  const todayShifts = useMemo(
    () => weekSchedule.find((d) => d.isToday)?.shifts ?? [],
    [weekSchedule],
  );

  const refetch = async () => {
    const promises = [refetchShifts()];
    if (isManager) {
      if (storeId) {
        promises.push(refetchStoreShifts());
      } else {
        promises.push(refetchAllStoreShifts());
      }
    } else {
      promises.push(refetchMyShifts());
    }
    await Promise.all(promises);
  };

  const goBack = () => navigation.goBack();

  const assignWorkShift = async (employeeId: string, workShiftId: string, date: string) => {
    return assignShift({ employeeId, workShiftId, date }).unwrap();
  };

  const deleteWorkShift = async (id: string) => {
    return deleteShift(id).unwrap();
  };

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      const promises = [refetchShifts()];
      if (isManager) {
        if (storeId) {
          promises.push(refetchStoreShifts());
        } else {
          promises.push(refetchAllStoreShifts());
        }
      } else {
        promises.push(refetchMyShifts());
      }
      await Promise.all(promises);
    } finally {
      setRefreshing(false);
    }
  };

  return {
    shifts,
    todayShifts,
    isLoading,
    isAssigning,
    error,
    refetch,
    weekSchedule,
    assignWorkShift,
    deleteWorkShift,
    isManager,
    storeId,
    goBack,
    refreshing,
    onRefresh,
    allAssignments,
    currentDate,
    setCurrentDate,
    isDeleting,
  };
};
