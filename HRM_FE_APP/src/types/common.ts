// Shared API response wrapper (matching backend ApiResponse<T>)
export interface ApiResponse<T> {
  success: boolean;
  code: string | null;
  message: string | null;
  data: T;
  errors: string[] | null;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  pageable: any;
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  sort: any;
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}
