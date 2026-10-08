// ─── Pagination ────────────────────────────────────────────────
export interface PageMeta {
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface PaginatedApiResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  pagination: PageMeta;
}

// ─── User / Account ────────────────────────────────────────────
export interface EmployeeResponse {
  id: string;
  fullName: string;
  phoneNumber?: string;
  position?: string;
  department?: string;
}

export interface UserResponse {
  id: string;
  email: string;
  avatarUrl: string | null;
  employee: EmployeeResponse | null;
  roles: string[];
  permissions: string[];
  isActive: boolean;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  deletedAt: string | null;
  deletedBy: string | null;
}

// ─── Role ──────────────────────────────────────────────────────
export interface PermissionResponse {
  id: string;
  code: string;
  displayName: string;
  description: string | null;
  resource: string;
  action: string;
  isSystem: boolean;
  isActive: boolean;
  isDeleted?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RoleResponse {
  id: string;
  code: string;
  displayName: string;
  description: string | null;
  isSystem: boolean;
  isActive: boolean;
  isDeleted?: boolean;
  permissions: PermissionResponse[];
  createdAt: string;
  updatedAt: string;
}

// ─── Request DTOs ─────────────────────────────────────────────
export interface CreateRoleRequest {
  code: string;
  displayName: string;
  description?: string;
  isActive?: boolean;
}

export interface UpdateRoleRequest {
  code: string;
  displayName: string;
  description?: string;
  isActive?: boolean;
}

export interface AssignPermissionsToRoleRequest {
  permissionIds: string[];
}

export interface CreatePermissionRequest {
  code: string;
  displayName: string;
  description?: string;
  resource: string;
  action: string;
  isActive?: boolean;
}

export interface UpdatePermissionRequest {
  code: string;
  displayName: string;
  description?: string;
  resource: string;
  action: string;
  isActive?: boolean;
}

export interface AssignRolesRequest {
  userId: string;
  roleIds: string[];
}

// ─── UserRole assignment ───────────────────────────────────────
export interface UserRoleAssignmentResponse {
  id: string;
  userId: string;
  role: RoleResponse;
  isActive: boolean;
}

// ─── Stats ───────────────────────────────────────────────────
export interface ResourceStatsResponse {
  total: number;
  active: number;
  inactive: number;
  deleted?: number;
}
