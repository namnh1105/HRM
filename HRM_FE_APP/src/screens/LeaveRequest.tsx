import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useLeaveRequests } from '../hooks/useLeaveRequests';
import type { LeaveRequest, LeaveStatus } from '../types/leave';
import { LEAVE_TYPE_LABELS, LEAVE_STATUS_LABELS } from '../types/leave';

const FILTERS: { label: string; value: LeaveStatus | 'all' }[] = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Chờ duyệt', value: 'PENDING' },
  { label: 'Đã duyệt', value: 'APPROVED' },
  { label: 'Từ chối', value: 'REJECTED' },
];

const LeaveRequestScreen: React.FC = () => {
  const {
    filterStatus,
    setFilterStatus,
    filteredRequests,
    isLoading,
    isError,
    refetch,
    getStatusColor,
    getTypeIcon,
    formatDate,
    handleCancel,
    navigateToCreate,
    refreshing,
    onRefresh,
  } = useLeaveRequests();

  const renderItem = ({ item }: { item: LeaveRequest }) => {
    const statusColor = getStatusColor(item.status);

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.typeRow}>
            <View style={styles.typeIcon}>
              <Ionicons name={getTypeIcon(item.leaveType) as any} size={18} color="#3B82F6" />
            </View>
            <Text style={styles.typeName}>{LEAVE_TYPE_LABELS[item.leaveType]}</Text>
          </View>
          <View style={[styles.statusBadge, { backgroundColor: statusColor.bg }]}>
            <Text style={[styles.statusText, { color: statusColor.text }]}>
              {LEAVE_STATUS_LABELS[item.status]}
            </Text>
          </View>
        </View>

        <View style={styles.dateRow}>
          <Ionicons name="calendar-outline" size={14} color="#94A3B8" />
          <Text style={styles.dateText}>
            {formatDate(item.startDate)}
            {item.startDate !== item.endDate && ` → ${formatDate(item.endDate)}`}
          </Text>
        </View>

        <Text style={styles.reason} numberOfLines={2}>
          {item.reason}
        </Text>

        {item.approverName && (
          <Text style={styles.approver}>Người duyệt: {item.approverName}</Text>
        )}
        {item.approverComment && (
          <Text style={styles.rejectedReason}>Nhận xét: {item.approverComment}</Text>
        )}

        {item.status === 'PENDING' && (
          <TouchableOpacity
            style={styles.cancelBtn}
            onPress={() => handleCancel(item.id)}
          >
            <Ionicons name="close-circle-outline" size={16} color="#EF4444" />
            <Text style={styles.cancelBtnText}>Hủy đơn</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  if (isLoading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Đơn từ</Text>
        </View>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang tải...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (isError) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Đơn từ</Text>
        </View>
        <View style={styles.centered}>
          <Ionicons name="cloud-offline-outline" size={48} color="#EF4444" />
          <Text style={styles.errorText}>Không thể tải dữ liệu</Text>
          <TouchableOpacity style={styles.retryBtn} onPress={refetch}>
            <Text style={styles.retryBtnText}>Thử lại</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Đơn từ</Text>
        <TouchableOpacity
          style={styles.addBtn}
          onPress={navigateToCreate}
        >
          <Ionicons name="add" size={24} color="#FFF" />
        </TouchableOpacity>
      </View>

      {/* Filters */}
      <View style={styles.filterRow}>
        {FILTERS.map((f) => (
          <TouchableOpacity
            key={f.value}
            style={[
              styles.filterChip,
              filterStatus === f.value && styles.filterChipActive,
            ]}
            onPress={() => setFilterStatus(f.value)}
          >
            <Text
              style={[
                styles.filterText,
                filterStatus === f.value && styles.filterTextActive,
              ]}
            >
              {f.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* List */}
      <FlatList
        data={filteredRequests}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 24 }}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="document-text-outline" size={48} color="#CBD5E1" />
            <Text style={styles.emptyText}>Không có đơn nào</Text>
          </View>
        }
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
        }
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 8,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1E293B',
  },
  addBtn: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: '#3B82F6',
    justifyContent: 'center',
    alignItems: 'center',
  },
  // Loading / Error
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 14,
    color: '#64748B',
    marginTop: 12,
  },
  errorText: {
    fontSize: 15,
    color: '#EF4444',
    marginTop: 12,
    marginBottom: 16,
  },
  retryBtn: {
    backgroundColor: '#3B82F6',
    paddingVertical: 10,
    paddingHorizontal: 32,
    borderRadius: 10,
  },
  retryBtnText: {
    color: '#FFF',
    fontSize: 14,
    fontWeight: '600',
  },
  // Filters
  filterRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    marginBottom: 12,
  },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#F1F5F9',
    marginRight: 8,
  },
  filterChipActive: {
    backgroundColor: '#3B82F6',
  },
  filterText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  filterTextActive: {
    color: '#FFF',
  },
  // Card
  card: {
    backgroundColor: '#FFF',
    borderRadius: 14,
    padding: 16,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  typeRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  typeIcon: {
    width: 32,
    height: 32,
    borderRadius: 8,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10,
  },
  typeName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1E293B',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
  },
  dateRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  dateText: {
    fontSize: 13,
    color: '#64748B',
    marginLeft: 6,
  },
  reason: {
    fontSize: 13,
    color: '#475569',
    lineHeight: 18,
  },
  approver: {
    fontSize: 12,
    color: '#10B981',
    marginTop: 6,
    fontStyle: 'italic',
  },
  rejectedReason: {
    fontSize: 12,
    color: '#EF4444',
    marginTop: 6,
    fontStyle: 'italic',
  },
  cancelBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-end',
    marginTop: 10,
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 8,
    backgroundColor: '#FEE2E2',
  },
  cancelBtnText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#EF4444',
    marginLeft: 4,
  },
  // Empty
  empty: {
    alignItems: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 15,
    color: '#94A3B8',
    marginTop: 12,
  },
});

export default LeaveRequestScreen;
