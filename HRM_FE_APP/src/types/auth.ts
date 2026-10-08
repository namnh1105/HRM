import { EmployeeProfile } from './employee';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserInfo {
  id: string;
  email: string;
  roles: string[];
  permissions: string[];
  employee: EmployeeProfile | null;
  avatarUrl: string | null;
  isActive: boolean;
  createdAt: string | null;
  updatedAt: string;
  createdBy: string;
  updatedBy: string | null;
  isDeleted: boolean;
  deletedAt: string | null;
  deletedBy: string | null;
  storeId?: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: UserInfo;
  };
  timestamp: string;
}

export interface RegisterRequest {
  email: string;
  givenName: string;
  familyName: string;
  password: string;
  avatarUrl?: string;
}

export interface RegisterResponse {
  success: boolean;
  message: string;
  data?: {
    user: {
      id: string;
      email: string;
      givenName: string;
      familyName: string;
    };
  };
}

export interface UserProfileResponse {
  success: boolean;
  message: string;
  data: {
    id: string;
    email: string;
    roles: string[];
    permissions: string[];
    avatarUrl: string;
    employee: EmployeeProfile | null;
  };
}
