// Work Shift types (matching backend WorkShiftResponse DTO)

export interface WorkShift {
  id: string;
  name: string;
  code: string;
  startTime: string;
  endTime: string;
  breakDuration: number;
  totalHours: number;
  description: string | null;
  isActive: boolean;
  isNightShift: boolean;
  createdAt: string;
  updatedAt: string;
}

// Employee Work Shift Assignment (matching backend EmployeeWorkShiftResponse DTO)
export interface EmployeeWorkShift {
  id: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  workShiftId: string;
  workShiftName: string;
  workShiftCode: string;
  shiftStartTime: string;
  shiftEndTime: string;
  date: string;
  createdAt: string;
  updatedAt: string;
}

/** Frontend-only composite for calendar view */
export interface ScheduleDay {
  date: string;
  shift?: WorkShift;
  isHoliday: boolean;
  holidayName?: string;
  isWeekend: boolean;
}
