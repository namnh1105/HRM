import React, { useState } from 'react';
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
import { useNavigation } from '@react-navigation/native';
import { useGetMyProfileQuery } from '../store/api/employeeApi';
import { useGetContractsByEmployeeQuery } from '../store/api/contractApi';
import { useAppSelector } from '../hooks';
import { formatTime } from '../utils';
import type { Contract } from '../types/contract';
import { CONTRACT_TYPE_LABELS, CONTRACT_STATUS_LABELS, CONTRACT_STATUS_COLORS } from '../types/contract';

const Contracts: React.FC = () => {
  const navigation = useNavigation<any>();
  const { user } = useAppSelector((state) => state.auth);
  const { data: profileData } = useGetMyProfileQuery(undefined);
  
  const employeeId = profileData?.data?.id;
  const { 
    data: contractsData, 
    isLoading, 
    isError, 
    refetch 
  } = useGetContractsByEmployeeQuery(employeeId || '', { 
    skip: !employeeId 
  });

  const contracts = contractsData?.data || [];

  const [refreshing, setRefreshing] = useState(false);
  const onRefresh = async () => {
    setRefreshing(true);
    try {
      await refetch();
    } finally {
      setRefreshing(false);
    }
  };

  const renderContractCard = (contract: Contract) => {
    const statusColor = CONTRACT_STATUS_COLORS[contract.status];
    
    return (
      <View key={contract.id} style={styles.card}>
        <View style={styles.cardHeader}>
          <View>
            <Text style={styles.contractCode}>{contract.contractCode}</Text>
            <Text style={styles.contractType}>
              {CONTRACT_TYPE_LABELS[contract.contractType]}
            </Text>
          </View>
          <View style={[styles.statusBadge, { backgroundColor: statusColor }]}>
            <Text style={styles.statusText}>
              {CONTRACT_STATUS_LABELS[contract.status]}
            </Text>
          </View>
        </View>

        <View style={styles.contractInfo}>
          <View style={styles.infoRow}>
            <Ionicons name="calendar-outline" size={16} color="#666" />
            <Text style={styles.infoLabel}>Ngày bắt đầu:</Text>
            <Text style={styles.infoValue}>
              {new Date(contract.startDate).toLocaleDateString('vi-VN')}
            </Text>
          </View>

          {contract.endDate && (
            <View style={styles.infoRow}>
              <Ionicons name="calendar-outline" size={16} color="#666" />
              <Text style={styles.infoLabel}>Ngày kết thúc:</Text>
              <Text style={styles.infoValue}>
                {new Date(contract.endDate).toLocaleDateString('vi-VN')}
              </Text>
            </View>
          )}

          <View style={styles.infoRow}>
            <Ionicons name="cash-outline" size={16} color="#666" />
            <Text style={styles.infoLabel}>Lương cơ bản:</Text>
            <Text style={styles.infoValue}>
              {contract.baseSalary.toLocaleString('vi-VN')} VNĐ
            </Text>
          </View>

          <View style={styles.infoRow}>
            <Ionicons name="calculator-outline" size={16} color="#666" />
            <Text style={styles.infoLabel}>Hệ số lương:</Text>
            <Text style={styles.infoValue}>{contract.salaryCoefficient}</Text>
          </View>

          {contract.signingDate && (
            <View style={styles.infoRow}>
              <Ionicons name="document-text-outline" size={16} color="#666" />
              <Text style={styles.infoLabel}>Ngày ký:</Text>
              <Text style={styles.infoValue}>
                {new Date(contract.signingDate).toLocaleDateString('vi-VN')}
              </Text>
            </View>
          )}

          {contract.note && (
            <View style={styles.noteContainer}>
              <Text style={styles.noteLabel}>Ghi chú:</Text>
              <Text style={styles.noteText}>{contract.note}</Text>
            </View>
          )}
        </View>
      </View>
    );
  };

  if (isLoading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={22} color="#1E293B" />
          </TouchableOpacity>
          <View style={styles.headerCenter}>
            <Text style={styles.title}>Hợp đồng lao động</Text>
          </View>
          <View style={styles.backBtnPlaceholder} />
        </View>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang tải hợp đồng...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (isError) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
            <Ionicons name="arrow-back" size={22} color="#1E293B" />
          </TouchableOpacity>
          <View style={styles.headerCenter}>
            <Text style={styles.title}>Hợp đồng lao động</Text>
          </View>
          <View style={styles.backBtnPlaceholder} />
        </View>
        <View style={styles.errorContainer}>
          <Ionicons name="alert-circle-outline" size={64} color="#EF4444" />
          <Text style={styles.errorTitle}>Lỗi tải dữ liệu</Text>
          <Text style={styles.errorSub}>Không thể tải hợp đồng lao động</Text>
          <TouchableOpacity style={styles.retryButton} onPress={refetch}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back" size={22} color="#1E293B" />
        </TouchableOpacity>
        <View style={styles.headerCenter}>
          <Text style={styles.title}>Hợp đồng lao động</Text>
          <Text style={styles.subtitle}>
            {contracts.length} hợp đồng
          </Text>
        </View>
        <View style={styles.backBtnPlaceholder} />
      </View>

      <ScrollView 
        style={styles.content} 
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
        }
      >
        {contracts.length === 0 ? (
          <View style={styles.emptyContainer}>
            <Ionicons name="document-outline" size={64} color="#CBD5E1" />
            <Text style={styles.emptyTitle}>Chưa có hợp đồng</Text>
            <Text style={styles.emptySub}>
              Bạn chưa có hợp đồng lao động nào trong hệ thống
            </Text>
          </View>
        ) : (
          contracts.map(renderContractCard)
        )}
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
    paddingBottom: 12,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
    flexDirection: 'row',
    alignItems: 'center',
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center',
  },
  backBtnPlaceholder: {
    width: 36,
    height: 36,
  },
  headerCenter: {
    flex: 1,
    alignItems: 'center',
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
  },
  content: {
    flex: 1,
    padding: 16,
  },
  card: {
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  contractCode: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 2,
  },
  contractType: {
    fontSize: 14,
    color: '#64748B',
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#FFF',
  },
  contractInfo: {
    gap: 8,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  infoLabel: {
    fontSize: 14,
    color: '#64748B',
    flex: 1,
  },
  infoValue: {
    fontSize: 14,
    fontWeight: '500',
    color: '#1E293B',
  },
  noteContainer: {
    marginTop: 8,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  noteLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: '#64748B',
    marginBottom: 4,
  },
  noteText: {
    fontSize: 14,
    color: '#1E293B',
    lineHeight: 20,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#64748B',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#EF4444',
    marginTop: 16,
    marginBottom: 8,
  },
  errorSub: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    marginBottom: 20,
  },
  retryButton: {
    backgroundColor: '#3B82F6',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
  },
  retryButtonText: {
    color: '#FFF',
    fontWeight: '600',
  },
  emptyContainer: {
    alignItems: 'center',
    paddingTop: 60,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#64748B',
    marginTop: 16,
    marginBottom: 8,
  },
  emptySub: {
    fontSize: 14,
    color: '#94A3B8',
    textAlign: 'center',
  },
});

export default Contracts;