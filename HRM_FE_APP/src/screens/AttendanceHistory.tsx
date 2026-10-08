import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useAttendanceHistory } from '../hooks/useAttendanceHistory';
import { formatTime } from '../utils';
import type { AttendanceRecord } from '../types/attendance';

const AttendanceHistory: React.FC = () => {
  const {
    history,
    isLoading,
    isError,
    refetch,
    getStatusInfo,
    formatDate,
    presentCount,
    totalHours,
    absentCount,
    goBack,
    refreshing,
    onRefresh,
  } = useAttendanceHistory();

  const renderItem = ({ item }: { item: AttendanceRecord }) => {
    const statusInfo = getStatusInfo(item);
    const dateInfo = formatDate(item.workDate);

    return (
      <View style={styles.card}>
        <View style={styles.dateBox}>
          <Text style={styles.dayName}>{dateInfo.dayName}</Text>
          <Text style={styles.dayNum}>{dateInfo.day}</Text>
          <Text style={styles.monthNum}>{dateInfo.month}</Text>
        </View>
        <View style={styles.info}>
          <View style={styles.timeRow}>
            <Ionicons name="log-in-outline" size={16} color="#3B82F6" />
            <Text style={styles.timeText}>{formatTime(item.checkInTime)}</Text>
            <Ionicons name="arrow-forward" size={14} color="#CBD5E1" style={{ marginHorizontal: 6 }} />
            <Ionicons name="log-out-outline" size={16} color="#EF4444" />
            <Text style={styles.timeText}>{formatTime(item.checkOutTime)}</Text>
          </View>
          <Text style={styles.hoursText}>
            {item.workingHours != null ? `${item.workingHours} giờ làm việc` : 'Chưa có dữ liệu'}
          </Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: statusInfo.bg }]}>
          <Text style={[styles.statusText, { color: statusInfo.color }]}>
            {statusInfo.label}
          </Text>
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={goBack} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Lịch sử chấm công</Text>
        <View style={{ width: 40 }} />
      </View>

      {/* Summary */}
      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryValue}>
            {presentCount}
          </Text>
          <Text style={styles.summaryLabel}>Ngày đi làm</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={[styles.summaryValue, { color: '#10B981' }]}>
            {totalHours}h
          </Text>
          <Text style={styles.summaryLabel}>Tổng giờ</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={[styles.summaryValue, { color: '#F59E0B' }]}>
            {absentCount}
          </Text>
          <Text style={styles.summaryLabel}>Chưa chấm</Text>
        </View>
      </View>

      {/* List */}
      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang tải dữ liệu...</Text>
        </View>
      ) : isError ? (
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={48} color="#EF4444" />
          <Text style={styles.errorText}>Không thể tải dữ liệu</Text>
          <TouchableOpacity onPress={refetch} style={styles.retryBtn}>
            <Text style={styles.retryText}>Thử lại</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={history}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 24 }}
          showsVerticalScrollIndicator={false}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
          }
        />
      )}
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
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  backBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1E293B',
  },
  // Summary
  summaryRow: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    marginBottom: 16,
  },
  summaryCard: {
    flex: 1,
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 14,
    marginHorizontal: 4,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  summaryValue: {
    fontSize: 20,
    fontWeight: '700',
    color: '#3B82F6',
  },
  summaryLabel: {
    fontSize: 11,
    color: '#94A3B8',
    marginTop: 4,
  },
  // Card
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF',
    borderRadius: 14,
    padding: 14,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  dateBox: {
    width: 48,
    alignItems: 'center',
    marginRight: 12,
  },
  dayName: {
    fontSize: 11,
    color: '#94A3B8',
    fontWeight: '600',
  },
  dayNum: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1E293B',
  },
  monthNum: {
    fontSize: 11,
    color: '#94A3B8',
  },
  info: {
    flex: 1,
  },
  timeRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  timeText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1E293B',
    marginLeft: 4,
  },
  hoursText: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 4,
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
    marginLeft: 8,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '600',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 48,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: '#64748B',
  },
  errorText: {
    marginTop: 12,
    fontSize: 15,
    fontWeight: '600',
    color: '#EF4444',
  },
  retryBtn: {
    marginTop: 16,
    paddingHorizontal: 24,
    paddingVertical: 10,
    backgroundColor: '#3B82F6',
    borderRadius: 10,
  },
  retryText: {
    color: '#FFF',
    fontWeight: '600',
    fontSize: 14,
  },
});

export default AttendanceHistory;
