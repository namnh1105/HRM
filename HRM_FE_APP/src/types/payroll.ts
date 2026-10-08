// Payroll / Salary types (matching backend PayrollResponse / DTOs)

export type PayrollStatus = 'DRAFT' | 'CONFIRMED' | 'PAID' | 'CANCELLED';

export interface SalaryDetail {
  id: string;
  employeeId: string;
  employeeName: string;
  employeeCode: string;
  month: number;
  year: number;
  baseSalary: number;
  salaryCoefficient: number;
  workingDays: number;
  actualWorkingDays: number;
  overtimeHours: number;
  overtimePay: number;
  allowance: number;
  bonus: number;
  socialInsurance: number;
  healthInsurance: number;
  unemploymentInsurance: number;
  personalIncomeTax: number;
  totalDeductions: number;
  totalIncome: number;
  netSalary: number;
  status: PayrollStatus;
  note: string | null;
  paymentDate: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
}

// --- Vietnamese label maps ---
export const PAYROLL_STATUS_LABELS: Record<PayrollStatus, string> = {
  DRAFT: 'Nháp',
  CONFIRMED: 'Đã xác nhận',
  PAID: 'Đã thanh toán',
  CANCELLED: 'Đã hủy',
};
