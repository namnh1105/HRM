// Attendance types (matching backend AttendanceResponse / DTOs)

export type AttendanceStatus =
  | 'PRESENT'
  | 'ABSENT'
  | 'LATE'
  | 'EARLY_LEAVE'
  | 'HALF_DAY'
  | 'ON_LEAVE'
  | 'HOLIDAY'
  | 'WEEKEND';

/** Frontend-only status for today's check-in flow */
export type CheckInFlowStatus = 'not_checked' | 'checked_in' | 'checked_out';

export interface AttendanceRecord {
  id: string;
  employeeId: string;
  employeeName: string;
  workDate: string;
  checkInTime: string | null;
  checkOutTime: string | null;
  checkInIp: string | null;
  checkOutIp: string | null;
  status: AttendanceStatus;
  workingHours: number;
  overtimeHours: number;
  lateMinutes: number;
  earlyLeaveMinutes: number;
  note: string | null;
  workShiftId: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  isDeleted: boolean;
  deletedAt: string | null;
  deletedBy: string | null;
}

export interface CheckInRequest {
  latitude?: number;
  longitude?: number;
  note?: string;
}

export interface CheckOutRequest {
  latitude?: number;
  longitude?: number;
  note?: string;
}
