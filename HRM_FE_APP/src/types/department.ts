// Department types (matching backend DepartmentResponse DTO)

export interface Department {
  id: string;
  name: string;
  code: string;
  description: string | null;
  phone: string | null;
  email: string | null;
  managerId: string | null;
  managerName: string | null;
  parentDepartmentId: string | null;
  parentDepartmentName: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
