import { useNavigation } from '@react-navigation/native';
import { useState, useEffect } from 'react';
import {
  useGetMyNotificationsQuery,
  useGetUnreadCountQuery,
  useMarkAsReadMutation,
  useMarkAllAsReadMutation,
} from '../store/api/notificationApi';
import { formatRelativeTime } from '../utils';
import type { NotificationType, HrmNotification } from '../types/notification';

export const useNotifications = () => {
  const navigation = useNavigation<any>();
  const [page, setPage] = useState(0);
  const [allNotifications, setAllNotifications] = useState<HrmNotification[]>([]);

  const { data, isLoading, isError, isFetching, refetch: refetchNotifications } = useGetMyNotificationsQuery({ page, size: 20 });
  const { data: unreadCountData, refetch: refetchUnreadCount } = useGetUnreadCountQuery();
  
  const [doMarkAsRead] = useMarkAsReadMutation();
  const [doMarkAllAsRead] = useMarkAllAsReadMutation();

  useEffect(() => {
    if (data?.data?.content) {
      if (page === 0) {
        setAllNotifications(data.data.content);
      } else {
        setAllNotifications((prev) => {
          const newItems = data.data.content.filter((item: HrmNotification) => !prev.some(p => p.id === item.id));
          return [...prev, ...newItems];
        });
      }
    }
  }, [data, page]);

  const unreadCount = unreadCountData?.data ?? 0;

  const getIcon = (type: NotificationType) => {
    switch (type) {
      case 'attendance':
      case 'REMINDER':
        return { name: 'time', color: '#3B82F6', bg: '#EFF6FF' };
      case 'leave':
      case 'LEAVE_APPROVED':
      case 'LEAVE_REJECTED':
        return { name: 'document-text', color: '#8B5CF6', bg: '#F5F3FF' };
      case 'salary':
      case 'SALARY':
        return { name: 'wallet', color: '#10B981', bg: '#ECFDF5' };
      case 'hr':
        return { name: 'people', color: '#F59E0B', bg: '#FFFBEB' };
      default:
        return { name: 'notifications', color: '#64748B', bg: '#F1F5F9' };
    }
  };

  const loadMore = () => {
    if (data?.data && !data.data.last && !isFetching) {
      setPage(p => p + 1);
    }
  };

  const refetch = () => {
    setPage(0);
    refetchNotifications();
    refetchUnreadCount();
  };

  const markAsRead = async (id: string) => {
    await doMarkAsRead(id);
    setAllNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    refetchUnreadCount();
  };

  const markAllAsRead = async () => {
    await doMarkAllAsRead();
    setAllNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    refetchUnreadCount();
  };

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      setPage(0);
      await Promise.all([refetchNotifications(), refetchUnreadCount()]);
    } finally {
      setRefreshing(false);
    }
  };

  return {
    notifications: allNotifications,
    unreadCount,
    isLoading: isLoading && page === 0,
    isFetchingMore: isFetching && page > 0,
    isError,
    refetch,
    loadMore,
    getIcon,
    formatTime: formatRelativeTime,
    markAsRead,
    markAllAsRead,
    refreshing,
    onRefresh,
  };
};
