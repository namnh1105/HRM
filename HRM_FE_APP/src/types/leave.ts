// Leave Request types (matching backend LeaveRequestResponse / DTOs)

export type LeaveType =
  | 'ANNUAL_LEAVE'
  | 'SICK_LEAVE'
  | 'MATERNITY_LEAVE'
  | 'PATERNITY_LEAVE'
  | 'WEDDING_LEAVE'
  | 'BEREAVEMENT_LEAVE'
  | 'UNPAID_LEAVE'
  | 'COMPENSATORY_LEAVE'
  | 'OTHER';

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface LeaveRequest {
  id: string;
  employeeId: string;
  employeeName: string;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  totalDays: number;
  reason: string;
  status: LeaveStatus;
  approverId: string | null;
  approverName: string | null;
  approvedAt: string | null;
  approverComment: string | null;
  attachmentUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLeaveRequestPayload {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason: string;
  attachmentUrl?: string;
}

export interface ReviewLeaveRequestPayload {
  approved: boolean;
  comment?: string;
}

// --- Vietnamese label maps ---
export const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  ANNUAL_LEAVE: 'Nghỉ phép năm',
  SICK_LEAVE: 'Nghỉ ốm',
  MATERNITY_LEAVE: 'Nghỉ thai sản',
  PATERNITY_LEAVE: 'Nghỉ cha',
  WEDDING_LEAVE: 'Nghỉ cưới',
  BEREAVEMENT_LEAVE: 'Nghỉ tang',
  UNPAID_LEAVE: 'Nghỉ không lương',
  COMPENSATORY_LEAVE: 'Nghỉ bù',
  OTHER: 'Khác',
};

export const LEAVE_STATUS_LABELS: Record<LeaveStatus, string> = {
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy',
};

export const LEAVE_STATUS_COLORS: Record<LeaveStatus, string> = {
  PENDING: '#F59E0B',
  APPROVED: '#10B981',
  REJECTED: '#EF4444',
  CANCELLED: '#94A3B8',
};
