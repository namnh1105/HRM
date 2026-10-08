export type EmploymentStatus = 'ACTIVE' | 'INACTIVE';

export type CreateEmployeeRequest = {
    fullName: string;
    email: string;
    employmentStatus?: EmploymentStatus;
    phone?: string;
    userId?: string;
    firstName?: string;
    lastName?: string;
    employeeCode?: string;
    dateOfBirth?: string;
    gender?: string;
    idCardNumber?: string;
    permanentAddress?: string;
    currentAddress?: string;
    departmentId?: string;
    storeId?: string;
    position?: string;
    joinDate?: string;
    leaveDate?: string;
    bankAccountNumber?: string;
    bankName?: string;
    taxCode?: string;
    socialInsuranceNumber?: string;
    healthInsuranceNumber?: string;
};

export type UpdateEmployeeRequest = Partial<CreateEmployeeRequest>;

export type CreateWorkShiftRequest = {
    name: string;
    startTime: string;
    endTime: string;
    enabled?: boolean;
};

export type UpdateWorkShiftRequest = Partial<CreateWorkShiftRequest>;

export type CreateContractRequest = {
    employeeId: string;
    contractType: string;
    startDate: string;
    endDate?: string;
    signingDate?: string;
    baseSalary: number;
    salaryCoefficient?: number;
    note?: string;
    attachmentUrl?: string;
};

export type UpdateContractRequest = Partial<{
    endDate: string;
    baseSalary: number;
    salaryCoefficient: number;
    status: string;
    note: string;
    attachmentUrl: string;
}>;

export type CreateDegreeRequest = {
    employeeId: string;
    degreeName: string;
    degreeLevel?: string;
    major?: string;
    institution?: string;
    graduationDate?: string;
    gpa?: number;
    attachmentUrl?: string;
    note?: string;
};

export type CreateStoreRequest = {
    name: string;
    code: string;
    address?: string;
    phone?: string;
    email?: string;
};

export type UpdateStoreRequest = Partial<
    Omit<CreateStoreRequest, 'code'> & {
        isActive?: boolean;
    }
>;
