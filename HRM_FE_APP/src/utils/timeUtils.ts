/**
 * Time and date formatting utilities
 */

/**
 * Format ISO time string to HH:MM:SS format
 * @param isoTime - ISO time string (can be full datetime or just time part)
 * @returns Formatted time as HH:MM:SS or '--:--:--' if null/invalid
 */
export const formatTime = (isoTime: string | null): string => {
  if (!isoTime) return '--:--:--';
  const timePart = isoTime.includes('T') ? isoTime.split('T')[1] : isoTime;
  return timePart?.substring(0, 8) || '--:--:--';
};

/**
 * Format time string to HH:MM:SS format (alias for shift times)
 * @param timeString - Time string 
 * @returns Formatted time as HH:MM:SS
 */
export const formatShiftTime = (timeString: string): string => {
  return timeString.slice(0, 8);
};

/**
 * Format time to HH:MM format (without seconds)
 * @param isoTime - ISO time string
 * @returns Formatted time as HH:MM or '--:--' if null/invalid
 */
export const formatTimeShort = (isoTime: string | null): string => {
  if (!isoTime) return '--:--';
  const timePart = isoTime.includes('T') ? isoTime.split('T')[1] : isoTime;
  return timePart?.substring(0, 5) || '--:--';
};

/**
 * Format date string to relative time (e.g., "2 hours ago", "3 days ago")
 * @param dateStr - ISO date string
 * @returns Relative time string in Vietnamese
 */
export const formatRelativeTime = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffHours < 1) return 'Vừa xong';
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays < 7) return `${diffDays} ngày trước`;
  return date.toLocaleDateString('vi-VN');
};

/**
 * Format date to Vietnamese day info
 * @param dateStr - Date string (YYYY-MM-DD format)
 * @returns Object with dayName, day, month info
 */
export const formatDateInfo = (dateStr: string) => {
  const date = new Date(dateStr);
  const dayNames = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
  const monthNames = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
  
  return {
    dayName: dayNames[date.getDay()],
    day: date.getDate().toString().padStart(2, '0'),
    month: monthNames[date.getMonth()],
  };
};