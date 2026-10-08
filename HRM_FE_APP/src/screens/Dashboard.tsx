import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useDashboard } from '../hooks/useDashboard';
import { formatTime } from '../utils';

const Dashboard: React.FC = () => {
  const {
    user,
    todayRecord,
    isAttendanceLoading,
    unreadCount,
    recentNotifications,
    quickActions,
    storeName,
    getStatusLabel,
    getStatusColor,
    getNotificationIcon,
    navigateToNotifications,
    refreshing,
    onRefresh,
  } = useDashboard();

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <ScrollView 
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.headerLeft}>
            <Text style={styles.greeting}>Xin chào,</Text>
            <Text style={styles.userName}>{user?.employee?.fullName || 'Người dùng'}</Text>
            <View style={styles.storeBadge}>
              <Ionicons name="location-outline" size={12} color="#64748B" />
              <Text style={styles.storeName}>{storeName}</Text>
            </View>
          </View>
          <TouchableOpacity
            style={styles.notifBtn}
            onPress={navigateToNotifications}
          >
            <Ionicons name="notifications-outline" size={24} color="#1E293B" />
            {unreadCount > 0 && (
              <View style={styles.badge}>
                <Text style={styles.badgeText}>
                  {unreadCount > 9 ? '9+' : unreadCount}
                </Text>
              </View>
            )}
          </TouchableOpacity>
        </View>

        {/* Today Status Card */}
        <View style={styles.statusCard}>
          <View style={styles.statusHeader}>
            <Text style={styles.statusTitle}>Trạng thái hôm nay</Text>
            <View style={[styles.statusBadge, { backgroundColor: getStatusColor() }]}>
              <Text style={styles.statusBadgeText}>{getStatusLabel()}</Text>
            </View>
          </View>
          {isAttendanceLoading ? (
            <ActivityIndicator size="small" color="#3B82F6" style={{ paddingVertical: 16 }} />
          ) : (
          <View style={styles.statusRow}>
            <View style={styles.statusItem}>
              <Ionicons name="log-in-outline" size={20} color="#3B82F6" />
              <Text style={styles.statusLabel}>Vào</Text>
              <Text style={styles.statusValue}>
                {formatTime(todayRecord?.checkInTime)}
              </Text>
            </View>
            <View style={styles.statusDivider} />
            <View style={styles.statusItem}>
              <Ionicons name="log-out-outline" size={20} color="#EF4444" />
              <Text style={styles.statusLabel}>Ra</Text>
              <Text style={styles.statusValue}>
                {formatTime(todayRecord?.checkOutTime)}
              </Text>
            </View>
            <View style={styles.statusDivider} />
            <View style={styles.statusItem}>
              <Ionicons name="time-outline" size={20} color="#10B981" />
              <Text style={styles.statusLabel}>Giờ làm</Text>
              <Text style={styles.statusValue}>
                {todayRecord?.workingHours != null
                  ? `${todayRecord.workingHours}h`
                  : '--'}
              </Text>
            </View>
          </View>
          )}
        </View>

        {/* Quick Actions */}
        <Text style={styles.sectionTitle}>Thao tác nhanh</Text>
        <View style={styles.quickActions}>
          {quickActions.map((action) => (
            <TouchableOpacity
              key={action.label}
              style={[styles.actionCard, { backgroundColor: action.bg }]}
              onPress={action.onPress}
              activeOpacity={0.7}
            >
              <View style={[styles.actionIcon, { backgroundColor: action.color }]}>
                <Ionicons name={action.icon as any} size={22} color="#FFF" />
              </View>
              <Text style={styles.actionLabel}>{action.label}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Recent Notifications */}
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Thông báo mới</Text>
          <TouchableOpacity onPress={navigateToNotifications}>
            <Text style={styles.seeAll}>Xem tất cả</Text>
          </TouchableOpacity>
        </View>

        {recentNotifications.length === 0 ? (
          <View style={styles.emptyNotif}>
            <Ionicons name="checkmark-circle-outline" size={32} color="#94A3B8" />
            <Text style={styles.emptyText}>Không có thông báo mới</Text>
          </View>
        ) : (
          recentNotifications.map((notif) => (
            <TouchableOpacity
              key={notif.id}
              style={styles.notifCard}
              onPress={navigateToNotifications}
              activeOpacity={0.7}
            >
              <View style={styles.notifIcon}>
                <Ionicons
                  name={getNotificationIcon(notif.type) as any}
                  size={20}
                  color="#3B82F6"
                />
              </View>
              <View style={styles.notifContent}>
                <Text style={styles.notifTitle} numberOfLines={1}>
                  {notif.title}
                </Text>
                <Text style={styles.notifMessage} numberOfLines={2}>
                  {notif.message}
                </Text>
              </View>
            </TouchableOpacity>
          ))
        )}

        <View style={{ height: 20 }} />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  // Header
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 8,
  },
  headerLeft: {
    flex: 1,
  },
  greeting: {
    fontSize: 14,
    color: '#64748B',
  },
  userName: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1E293B',
    marginTop: 2,
  },
  storeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  storeName: {
    fontSize: 12,
    color: '#64748B',
    marginLeft: 4,
    fontWeight: '500',
  },
  notifBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center',
  },
  badge: {
    position: 'absolute',
    top: 6,
    right: 6,
    backgroundColor: '#EF4444',
    borderRadius: 10,
    minWidth: 18,
    height: 18,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 4,
  },
  badgeText: {
    color: '#FFF',
    fontSize: 10,
    fontWeight: '700',
  },
  // Status Card
  statusCard: {
    marginHorizontal: 20,
    marginTop: 16,
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 3,
  },
  statusHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  statusTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 20,
  },
  statusBadgeText: {
    color: '#FFF',
    fontSize: 12,
    fontWeight: '600',
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusItem: {
    flex: 1,
    alignItems: 'center',
  },
  statusDivider: {
    width: 1,
    height: 40,
    backgroundColor: '#E2E8F0',
  },
  statusLabel: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 4,
  },
  statusValue: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1E293B',
    marginTop: 2,
  },
  // Quick Actions
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    paddingHorizontal: 20,
    marginTop: 24,
    marginBottom: 12,
  },
  quickActions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
  },
  actionCard: {
    width: '48%',
    borderRadius: 14,
    padding: 16,
    marginBottom: 12,
    alignItems: 'center',
  },
  actionIcon: {
    width: 44,
    height: 44,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  actionLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#334155',
  },
  // Notifications section
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingRight: 20,
  },
  seeAll: {
    fontSize: 13,
    color: '#3B82F6',
    fontWeight: '600',
  },
  notifCard: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 20,
    marginBottom: 8,
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  notifIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  notifContent: {
    flex: 1,
  },
  notifTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1E293B',
  },
  notifMessage: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
  },
  emptyNotif: {
    alignItems: 'center',
    paddingVertical: 24,
  },
  emptyText: {
    fontSize: 13,
    color: '#94A3B8',
    marginTop: 8,
  },
});

export default Dashboard;
