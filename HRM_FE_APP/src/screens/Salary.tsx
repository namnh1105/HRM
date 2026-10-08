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
import { useSalary, formatVND } from '../hooks/useSalary';
import { DetailRow } from '../components';

const Salary: React.FC = () => {
  const {
    salaryHistory,
    isLoading,
    isError,
    selectedIndex,
    setSelectedIndex,
    selected,
    monthLabel,
    goBack,
    isManager,
    totalPayroll,
    refreshing,
    onRefresh,
  } = useSalary();

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={goBack} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Lương & phúc lợi</Text>
        <View style={{ width: 40 }} />
      </View>

      {isLoading && (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang tải dữ liệu lương...</Text>
        </View>
      )}

      {isError && (
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={48} color="#EF4444" />
          <Text style={styles.errorText}>Không thể tải dữ liệu lương. Vui lòng thử lại.</Text>
        </View>
      )}

      {!isLoading && !isError && salaryHistory.length === 0 && (
        <View style={styles.centered}>
          <Ionicons name="document-text-outline" size={48} color="#94A3B8" />
          <Text style={styles.emptyText}>Chưa có dữ liệu lương.</Text>
        </View>
      )}

      {!isLoading && !isError && salaryHistory.length > 0 && (
      <ScrollView 
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
        }
      >
        {/* Month Selector */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.monthRow}
        >
          {salaryHistory.map((s, idx) => (
            <TouchableOpacity
              key={s.id}
              style={[styles.monthChip, selectedIndex === idx && styles.monthChipActive]}
              onPress={() => setSelectedIndex(idx)}
            >
              <Text
                style={[
                  styles.monthText,
                  selectedIndex === idx && styles.monthTextActive,
                ]}
              >
                {monthLabel(s)}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>



        {selected && (
          <>
            {/* Net Salary Card */}
            <View style={styles.netCard}>
              <Text style={styles.netLabel}>Lương thực nhận</Text>
              <Text style={styles.netValue}>{formatVND(selected.netSalary)}</Text>
              {selected.paymentDate && (
                <Text style={styles.paidDate}>
                  Ngày trả: {new Date(selected.paymentDate).toLocaleDateString('vi-VN')}
                </Text>
              )}
            </View>

            {/* Breakdown */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Thu nhập</Text>
              <DetailRow label="Lương cơ bản" value={selected.baseSalary} showSign formatNumber={formatVND} />
              <DetailRow label="Phụ cấp" value={selected.allowance} variant="success" showSign formatNumber={formatVND} />
              <DetailRow label="Làm thêm giờ" value={selected.overtimePay} variant="info" showSign formatNumber={formatVND} />
              <DetailRow label="Thưởng" value={selected.bonus} valueColor="#8B5CF6" showSign formatNumber={formatVND} />
            </View>

            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Khấu trừ</Text>
              <DetailRow label="BHXH" value={-selected.socialInsurance} variant="danger" showSign formatNumber={formatVND} />
              <DetailRow label="BHYT" value={-selected.healthInsurance} variant="danger" showSign formatNumber={formatVND} />
              <DetailRow label="BHTN" value={-selected.unemploymentInsurance} variant="danger" showSign formatNumber={formatVND} />
              <DetailRow label="Thuế TNCN" value={-selected.personalIncomeTax} variant="danger" showSign formatNumber={formatVND} />
              <DetailRow label="Khấu trừ khác" value={-(selected.totalDeductions - selected.socialInsurance - selected.healthInsurance - selected.unemploymentInsurance - selected.personalIncomeTax)} variant="danger" showSign formatNumber={formatVND} />
            </View>

            {/* Total */}
            <View style={styles.totalRow}>
              <Text style={styles.totalLabel}>Tổng thực nhận</Text>
              <Text style={styles.totalValue}>{formatVND(selected.netSalary)}</Text>
            </View>

            {/* Download */}
            <TouchableOpacity style={styles.downloadBtn} activeOpacity={0.7}>
              <Ionicons name="download-outline" size={20} color="#3B82F6" />
              <Text style={styles.downloadText}>Tải phiếu lương (PDF)</Text>
            </TouchableOpacity>
          </>
        )}

        <View style={{ height: 32 }} />
      </ScrollView>
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
  // Month selector
  monthRow: {
    paddingHorizontal: 20,
    paddingBottom: 12,
  },
  monthChip: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 12,
    backgroundColor: '#F1F5F9',
    marginRight: 8,
  },
  monthChipActive: {
    backgroundColor: '#3B82F6',
  },
  monthText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  monthTextActive: {
    color: '#FFF',
  },
  // Total Payroll
  totalPayrollCard: {
    marginHorizontal: 20,
    backgroundColor: '#EFF6FF',
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#BFDBFE',
    alignItems: 'center',
  },
  totalPayrollLabel: {
    fontSize: 13,
    color: '#3B82F6',
    fontWeight: '600',
  },
  totalPayrollValue: {
    fontSize: 24,
    fontWeight: '800',
    color: '#1E40AF',
    marginTop: 4,
  },
  // Net Card
  netCard: {
    marginHorizontal: 20,
    backgroundColor: '#1E40AF',
    borderRadius: 20,
    padding: 24,
    alignItems: 'center',
    marginBottom: 20,
  },
  employeeNameLabel: {
    fontSize: 18,
    fontWeight: '700',
    color: '#FFF',
    marginBottom: 4,
  },
  netLabel: {
    fontSize: 14,
    color: '#93C5FD',
  },
  netValue: {
    fontSize: 32,
    fontWeight: '800',
    color: '#FFF',
    marginTop: 4,
  },
  paidDate: {
    fontSize: 12,
    color: '#93C5FD',
    marginTop: 8,
  },
  // Sections
  section: {
    marginHorizontal: 20,
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 4,
  },
  // Total
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginHorizontal: 20,
    backgroundColor: '#ECFDF5',
    borderRadius: 14,
    padding: 16,
    marginBottom: 16,
  },
  totalLabel: {
    fontSize: 15,
    fontWeight: '700',
    color: '#065F46',
  },
  totalValue: {
    fontSize: 18,
    fontWeight: '800',
    color: '#059669',
  },
  // Download
  downloadBtn: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginHorizontal: 20,
    backgroundColor: '#EFF6FF',
    borderRadius: 14,
    paddingVertical: 14,
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  downloadText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#3B82F6',
    marginLeft: 8,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 80,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: '#64748B',
  },
  errorText: {
    marginTop: 12,
    fontSize: 14,
    color: '#EF4444',
    textAlign: 'center',
    paddingHorizontal: 32,
  },
  emptyText: {
    marginTop: 12,
    fontSize: 14,
    color: '#94A3B8',
  },
});

export default Salary;
