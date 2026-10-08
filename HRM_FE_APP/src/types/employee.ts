// Employee types (matching backend EmployeeResponse DTO)

export interface EmployeeProfile {
  id: string;
  employeeCode: string;
  userId: string | null;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  idCardNumber: string | null;
  permanentAddress: string | null;
  currentAddress: string | null;
  avatarUrl: string | null;
  departmentId: string | null;
  departmentName: string | null;
  storeId: string | null;
  storeName: string | null;
  position: string | null;
  joinDate: string | null;
  leaveDate: string | null;
  employmentStatus: string;
  bankAccountNumber: string | null;
  bankName: string | null;
  taxCode: string | null;
  socialInsuranceNumber: string | null;
  healthInsuranceNumber: string | null;
  baseSalary: number;
  createdAt: string;
  updatedAt: string;
}
