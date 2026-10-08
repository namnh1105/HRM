// Degree types (matching backend DTOs)
export type DegreeLevel = 'HIGH_SCHOOL' | 'BACHELOR' | 'MASTER' | 'PHD' | 'DIPLOMA' | 'CERTIFICATE' | 'OTHER';

export interface Degree {
  id: string;
  employeeId: string;
  employeeName: string;
  degreeName: string;
  degreeLevel: DegreeLevel | null;
  institution: string;
  major: string;
  graduationDate: string | null;
  grade: string;
  attachmentUrl: string | null;
  note: string | null;
  createdAt: string;
  updatedAt: string;
}

export const DEGREE_LEVEL_LABELS: Record<DegreeLevel, string> = {
  HIGH_SCHOOL: 'Trung học phổ thông',
  BACHELOR: 'Cử nhân',
  MASTER: 'Thạc sĩ',
  PHD: 'Tiến sĩ',
  DIPLOMA: 'Cao đẳng',
  CERTIFICATE: 'Chứng chỉ',
  OTHER: 'Khác',
};