// Contract types (matching backend DTOs)
export type ContractType = 'FULL_TIME' | 'PART_TIME' | 'TEMPORARY' | 'INTERN' | 'FREELANCE';
export type ContractStatus = 'DRAFT' | 'ACTIVE' | 'EXPIRED' | 'TERMINATED' | 'PENDING';

export interface Contract {
  id: string;
  contractCode: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  contractType: ContractType;
  startDate: string;
  endDate: string | null;
  signingDate: string | null;
  baseSalary: number;
  salaryCoefficient: number;
  status: ContractStatus;
  note: string | null;
  attachmentUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export const CONTRACT_TYPE_LABELS: Record<ContractType, string> = {
  FULL_TIME: 'Toàn thời gian',
  PART_TIME: 'Bán thời gian',
  TEMPORARY: 'Tạm thời',
  INTERN: 'Thực tập',
  FREELANCE: 'Tự do',
};

export const CONTRACT_STATUS_LABELS: Record<ContractStatus, string> = {
  DRAFT: 'Nháp',
  ACTIVE: 'Đang hiệu lực',
  EXPIRED: 'Hết hạn',
  TERMINATED: 'Đã chấm dứt',
  PENDING: 'Chờ duyệt',
};

export const CONTRACT_STATUS_COLORS: Record<ContractStatus, string> = {
  DRAFT: '#94A3B8',
  ACTIVE: '#10B981',
  EXPIRED: '#EF4444',
  TERMINATED: '#F59E0B',
  PENDING: '#8B5CF6',
};