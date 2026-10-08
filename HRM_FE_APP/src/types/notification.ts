// Notification types

export type NotificationType =
  | 'attendance'
  | 'leave'
  | 'salary'
  | 'general'
  | 'hr';

export interface HrmNotification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}
