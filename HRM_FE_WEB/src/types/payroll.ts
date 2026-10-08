export type PayrollStatus = 'DRAFT' | 'CONFIRMED' | 'PAID' | 'CANCELLED';

export type PayrollSummary = {
    id: string;
    employeeId: string;
    employeeName: string;
    employeeCode?: string;
    month: number;
    year: number;
    baseSalary: number;
    salaryCoefficient: number;
    workingDays: number;
    actualWorkingDays: number;
    workingHours?: number;
    overtimeHours: number;
    overtimePay: number;
    allowance: number;
    bonus: number;
    latePenalty: number;
    lateCount: number;
    socialInsurance: number;
    healthInsurance: number;
    unemploymentInsurance: number;
    personalIncomeTax: number;
    totalDeductions: number;
    totalIncome: number;
    netSalary: number;
    status: PayrollStatus;
    note?: string | null;
    paymentDate?: string | null;
    createdAt?: string;
    updatedAt?: string;
};

export type AutoGeneratePayrollRequest = {
    month: number;
    year: number;
    latePenaltyPerShift?: number;
    latePenaltyPerHour?: number;
    allowance?: number;
    overwriteExisting?: boolean;
};
