import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useAttendance } from '../hooks/useAttendance';
import { formatTime } from '../utils';

const Attendance: React.FC = () => {
  const {
    timeStr,
    dateStr,
    todayRecord,
    todayLoading,
    historyLoading,
    isProcessing,
    canCheckIn,
    canCheckOut,
    isCheckedOut,
    isAllDone,
    shouldShowFaceRegistration,
    historyRecords,
    handleCheckIn,
    handleCheckOut,
    navigateToHistory,
    navigateToFaceRegistration,
    upcomingShift,
    isManager,
    refreshing,
    onRefresh,
  } = useAttendance();

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
          <Text style={styles.headerTitle}>Chấm công</Text>
        </View>

        {/* Clock */}
        <View style={styles.clockSection}>
          <Text style={styles.time}>{timeStr}</Text>
          <Text style={styles.date}>{dateStr}</Text>
        </View>

        {/* Upcoming / Current Shift Info */}
        {upcomingShift && (
          <View style={styles.shiftCard}>
            <View style={styles.shiftCardHeader}>
              <View style={[styles.shiftStatusDot, { backgroundColor: upcomingShift.isActive ? '#10B981' : '#F59E0B' }]} />
              <Text style={styles.shiftCardTitle}>
                {upcomingShift.isActive ? 'Ca đang diễn ra' : 'Ca sắp tới'}
              </Text>
            </View>
            <Text style={styles.shiftName}>{upcomingShift.name}</Text>
            <View style={styles.shiftTimeRow}>
              <Ionicons name="time-outline" size={16} color="#3B82F6" />
              <Text style={styles.shiftTimeText}>
                {upcomingShift.startTime} - {upcomingShift.endTime}
              </Text>
            </View>
            {!upcomingShift.isActive && upcomingShift.minutesUntilStart > 0 && (
              <View style={styles.shiftCountdown}>
                <Ionicons name="hourglass-outline" size={14} color="#F59E0B" />
                <Text style={styles.shiftCountdownText}>
                  Bắt đầu sau {upcomingShift.minutesUntilStart >= 60
                    ? `${Math.floor(upcomingShift.minutesUntilStart / 60)}h${upcomingShift.minutesUntilStart % 60 > 0 ? ` ${upcomingShift.minutesUntilStart % 60} phút` : ''}`
                    : `${upcomingShift.minutesUntilStart} phút`}
                </Text>
              </View>
            )}
          </View>
        )}

        {!upcomingShift && !todayLoading && (
          <View style={styles.noShiftCard}>
            <Ionicons name="calendar-outline" size={20} color="#94A3B8" />
            <Text style={styles.noShiftText}>Không có ca làm việc sắp tới</Text>
          </View>
        )}

        {/* Loading state */}
        {(todayLoading || isProcessing) && (
          <ActivityIndicator size="large" color="#3B82F6" style={{ marginVertical: 20 }} />
        )}

        {/* Check-in / Check-out Button */}
        {!todayLoading && (
          <View style={styles.buttonSection}>
            {shouldShowFaceRegistration ? (
              <>
                <TouchableOpacity
                  style={[styles.mainBtn, styles.faceRegisterCircleBtn]}
                  onPress={navigateToFaceRegistration}
                  activeOpacity={0.8}
                >
                  <Ionicons name="scan" size={40} color="#FFF" />
                  <Text style={styles.mainBtnText}>ĐĂNG KÝ</Text>
                  <Text style={styles.mainBtnSubText}>Khuôn mặt</Text>
                </TouchableOpacity>
                <Text style={styles.faceRegisterHint}>Bạn cần đăng ký khuôn mặt trước khi chấm công</Text>
              </>
            ) : (
              <>
                {canCheckIn && (
                  <TouchableOpacity
                    style={[styles.mainBtn, styles.checkInBtn]}
                    onPress={handleCheckIn}
                    activeOpacity={0.8}
                    disabled={isProcessing}
                  >
                    <Ionicons name="scan" size={40} color="#FFF" />
                    <Text style={styles.mainBtnText}>CHẤM CÔNG VÀO</Text>
                    <Text style={styles.mainBtnSubText}>Nhận diện khuôn mặt</Text>
                  </TouchableOpacity>
                )}

                {canCheckOut && (
                  <TouchableOpacity
                    style={[styles.mainBtn, styles.checkOutBtn]}
                    onPress={handleCheckOut}
                    activeOpacity={0.8}
                    disabled={isProcessing}
                  >
                    <Ionicons name="scan" size={40} color="#FFF" />
                    <Text style={styles.mainBtnText}>CHẤM CÔNG RA</Text>
                    <Text style={styles.mainBtnSubText}>Nhận diện khuôn mặt</Text>
                  </TouchableOpacity>
                )}

                {isAllDone && (
                  <View style={[styles.mainBtn, styles.doneBtn]}>
                    <Ionicons name="checkmark-circle" size={40} color="#FFF" />
                    <Text style={styles.mainBtnText}>ĐÃ HOÀN THÀNH</Text>
                  </View>
                )}
              </>
            )}
          </View>
        )}

        {/* Today Info */}
        <View style={styles.todayCard}>
          <Text style={styles.cardTitle}>Hôm nay</Text>
          <View style={styles.todayRow}>
            <View style={styles.todayItem}>
              <Ionicons name="log-in-outline" size={20} color="#3B82F6" />
              <Text style={styles.todayLabel}>Giờ vào</Text>
              <Text style={styles.todayValue}>
                {formatTime(todayRecord?.checkInTime ?? null)}
              </Text>
            </View>
            <View style={styles.todayItem}>
              <Ionicons name="log-out-outline" size={20} color="#EF4444" />
              <Text style={styles.todayLabel}>Giờ ra</Text>
              <Text style={styles.todayValue}>
                {formatTime(todayRecord?.checkOutTime ?? null)}
              </Text>
            </View>
            <View style={styles.todayItem}>
              <Ionicons name="time-outline" size={20} color="#10B981" />
              <Text style={styles.todayLabel}>Tổng giờ</Text>
              <Text style={styles.todayValue}>
                {todayRecord?.workingHours != null ? `${todayRecord.workingHours}h` : '--'}
              </Text>
            </View>
          </View>
        </View>

        {/* History */}
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Lịch sử gần đây</Text>
          <TouchableOpacity onPress={navigateToHistory}>
            <Text style={styles.seeAll}>Xem tất cả</Text>
          </TouchableOpacity>
        </View>

        {historyLoading && (
          <ActivityIndicator size="small" color="#3B82F6" style={{ marginVertical: 12 }} />
        )}

        {historyRecords.map((record) => (
          <View key={record.id} style={styles.historyCard}>
            <View style={styles.historyDate}>
              <Text style={styles.historyDateText}>{record.workDate}</Text>
            </View>
            <View style={styles.historyInfo}>
              <View style={styles.historyTopRow}>
                <Text style={styles.historyTime}>
                  {formatTime(record.checkInTime)} → {formatTime(record.checkOutTime)}
                </Text>
                {isManager && (
                  <Text style={styles.historyEmployeeName}>{record.employeeName}</Text>
                )}
              </View>
              <View style={styles.historyBottomRow}>
                <Text style={styles.historyHours}>
                  {record.workingHours != null ? `${record.workingHours} giờ` : '--'}
                </Text>
                {record.lateMinutes > 0 && (
                  <View style={[styles.miniBadge, styles.lateBadge]}>
                    <Text style={styles.miniBadgeText}>Muộn {record.lateMinutes}p</Text>
                  </View>
                )}
                {record.overtimeHours > 0 && (
                  <View style={[styles.miniBadge, styles.otBadge]}>
                    <Text style={styles.miniBadgeText}>OT {record.overtimeHours}h</Text>
                  </View>
                )}
              </View>
            </View>
            <View
              style={[
                styles.historyStatus,
                {
                  backgroundColor:
                    record.checkOutTime ? '#ECFDF5' : '#FEF3C7',
                },
              ]}
            >
              <Text
                style={[
                  styles.historyStatusText,
                  {
                    color:
                      record.checkOutTime ? '#10B981' : '#F59E0B',
                  },
                ]}
              >
                {record.checkOutTime ? '✓' : '•'}
              </Text>
            </View>
          </View>
        ))}

        <View style={{ height: 24 }} />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 8,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1E293B',
  },
  // Clock
  clockSection: {
    alignItems: 'center',
    paddingVertical: 20,
  },
  time: {
    fontSize: 48,
    fontWeight: '700',
    color: '#1E293B',
    fontVariant: ['tabular-nums'],
  },
  date: {
    fontSize: 15,
    color: '#64748B',
    marginTop: 4,
    textTransform: 'capitalize',
  },
  // Main Button
  buttonSection: {
    alignItems: 'center',
    paddingVertical: 12,
  },
  mainBtn: {
    width: 160,
    height: 160,
    borderRadius: 80,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 8,
  },
  checkInBtn: {
    backgroundColor: '#3B82F6',
  },
  checkOutBtn: {
    backgroundColor: '#EF4444',
  },
  doneBtn: {
    backgroundColor: '#10B981',
  },
  mainBtnText: {
    color: '#FFF',
    fontSize: 13,
    fontWeight: '700',
    marginTop: 8,
    letterSpacing: 0.5,
  },
  mainBtnSubText: {
    color: 'rgba(255,255,255,0.8)',
    fontSize: 10,
    marginTop: 4,
  },
  faceRegisterCircleBtn: {
    backgroundColor: '#EC4899',
  },
  faceRegisterHint: {
    marginTop: 12,
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
    paddingHorizontal: 24,
  },
  // Upcoming Shift Card
  shiftCard: {
    marginHorizontal: 20,
    marginBottom: 8,
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 16,
    borderLeftWidth: 4,
    borderLeftColor: '#3B82F6',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 3,
  },
  shiftCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  shiftStatusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  shiftCardTitle: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  shiftName: {
    fontSize: 17,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 6,
  },
  shiftTimeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  shiftTimeText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#3B82F6',
  },
  shiftCountdown: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
    backgroundColor: '#FFFBEB',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
    alignSelf: 'flex-start',
    gap: 6,
  },
  shiftCountdownText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#D97706',
  },
  noShiftCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 20,
    marginBottom: 8,
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    padding: 14,
    gap: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderStyle: 'dashed',
  },
  noShiftText: {
    fontSize: 13,
    color: '#94A3B8',
  },
  // Today Card
  todayCard: {
    marginHorizontal: 20,
    marginTop: 24,
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 3,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 16,
  },
  todayRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  todayItem: {
    alignItems: 'center',
  },
  todayLabel: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 4,
  },
  todayValue: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1E293B',
    marginTop: 2,
  },
  // History
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginTop: 24,
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
  },
  seeAll: {
    fontSize: 13,
    color: '#3B82F6',
    fontWeight: '600',
  },
  historyCard: {
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
  historyDate: {
    marginRight: 12,
  },
  historyDateText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  historyInfo: {
    flex: 1,
  },
  historyTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  historyBottomRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 2,
  },
  historyEmployeeName: {
    fontSize: 12,
    fontWeight: '700',
    color: '#3B82F6',
  },
  historyTime: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1E293B',
  },
  historyHours: {
    fontSize: 12,
    color: '#94A3B8',
  },
  miniBadge: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  lateBadge: {
    backgroundColor: '#FEE2E2',
  },
  otBadge: {
    backgroundColor: '#ECFDF5',
  },
  miniBadgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#1E293B',
  },
  historyStatus: {
    width: 28,
    height: 28,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  historyStatusText: {
    fontSize: 14,
    fontWeight: '700',
  },
});

export default Attendance;
