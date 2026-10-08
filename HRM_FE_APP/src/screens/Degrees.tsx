import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useGetAllDegreesQuery } from '../store/api/degreeApi';
import LoadingIndicator from '../components/LoadingIndicator';
import { Degree } from '../types/degree';

const Degrees: React.FC<{ navigation: any }> = ({ navigation }) => {
  const [searchText, setSearchText] = useState('');
  const [filteredDegrees, setFilteredDegrees] = useState<Degree[]>([]);

  const {
    data: degreesData,
    isLoading,
    error,
    refetch,
  } = useGetAllDegreesQuery(undefined);

  const degrees = degreesData?.data ?? [];

  useEffect(() => {
    if (!degrees) return;
    
    const filtered = degrees.filter((degree) =>
      degree.degreeName.toLowerCase().includes(searchText.toLowerCase()) ||
      degree.institution.toLowerCase().includes(searchText.toLowerCase()) ||
      degree.major.toLowerCase().includes(searchText.toLowerCase())
    );
    setFilteredDegrees(filtered);
  }, [degrees, searchText]);

  const getDegreeIcon = (level: string) => {
    switch (level.toLowerCase()) {
      case 'bachelor':
      case 'cử nhân':
        return 'school-outline';
      case 'master':
      case 'thạc sĩ':
        return 'library-outline';
      case 'doctor':
      case 'tiến sĩ':
        return 'ribbon-outline';
      case 'diploma':
      case 'cao đẳng':
        return 'document-outline';
      default:
        return 'medal-outline';
    }
  };

  const getDegreeColor = (level: string) => {
    switch (level.toLowerCase()) {
      case 'bachelor':
      case 'cử nhân':
        return '#10B981';
      case 'master':
      case 'thạc sĩ':
        return '#3B82F6';
      case 'doctor':
      case 'tiến sĩ':
        return '#8B5CF6';
      case 'diploma':
      case 'cao đẳng':
        return '#F59E0B';
      default:
        return '#6B7280';
    }
  };

  const renderDegree = ({ item }: { item: Degree }) => {
    const degreeColor = getDegreeColor(item.degreeLevel);
    const degreeIcon = getDegreeIcon(item.degreeLevel);

    return (
      <View style={styles.degreeCard}>
        <View style={styles.degreeHeader}>
          <View style={styles.degreeInfo}>
            <View style={[styles.degreeBadge, { backgroundColor: degreeColor + '15' }]}>
              <Ionicons name={degreeIcon as any} size={16} color={degreeColor} />
              <Text style={[styles.degreeBadgeText, { color: degreeColor }]}>
                {item.degreeLevel}
              </Text>
            </View>
            <Text style={styles.degreeName}>{item.degreeName}</Text>
          </View>
        </View>

        <View style={styles.degreeDetails}>
          <View style={styles.detailRow}>
            <Ionicons name="business-outline" size={16} color="#64748B" />
            <Text style={styles.detailLabel}>Trường:</Text>
            <Text style={styles.detailValue}>{item.institution}</Text>
          </View>

          <View style={styles.detailRow}>
            <Ionicons name="book-outline" size={16} color="#64748B" />
            <Text style={styles.detailLabel}>Chuyên ngành:</Text>
            <Text style={styles.detailValue}>{item.major}</Text>
          </View>

          <View style={styles.detailRow}>
            <Ionicons name="calendar-outline" size={16} color="#64748B" />
            <Text style={styles.detailLabel}>Năm tốt nghiệp:</Text>
            <Text style={styles.detailValue}>{item.graduationDate ? new Date(item.graduationDate).getFullYear() : 'N/A'}</Text>
          </View>

          {item.gpa && (
            <View style={styles.detailRow}>
              <Ionicons name="trending-up-outline" size={16} color="#64748B" />
              <Text style={styles.detailLabel}>GPA:</Text>
              <Text style={styles.detailValue}>{item.gpa}</Text>
            </View>
          )}
        </View>
      </View>
    );
  };

  const renderEmptyState = () => (
    <View style={styles.emptyState}>
      <Ionicons name="school-outline" size={64} color="#94A3B8" />
      <Text style={styles.emptyTitle}>Không có bằng cấp nào</Text>
      <Text style={styles.emptySubtitle}>
        {searchText ? 'Không tìm thấy bằng cấp phù hợp' : 'Chưa có thông tin bằng cấp'}
      </Text>
    </View>
  );

  const renderError = () => (
    <View style={styles.errorState}>
      <Ionicons name="alert-circle-outline" size={64} color="#EF4444" />
      <Text style={styles.errorTitle}>Không thể tải dữ liệu</Text>
      <Text style={styles.errorSubtitle}>
        {(error as any)?.data?.message || 'Đã xảy ra lỗi khi tải thông tin bằng cấp'}
      </Text>
      <TouchableOpacity style={styles.retryButton} onPress={refetch}>
        <Ionicons name="refresh-outline" size={20} color="#3B82F6" />
        <Text style={styles.retryText}>Thử lại</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <View style={styles.headerContent}>
          <Text style={styles.title}>Bằng cấp</Text>
          <Text style={styles.subtitle}>Quản lý thông tin bằng cấp của bạn</Text>
        </View>
      </View>

      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <Ionicons name="search-outline" size={20} color="#64748B" />
          <TextInput
            style={styles.searchInput}
            placeholder="Tìm kiếm bằng cấp, trường, chuyên ngành..."
            placeholderTextColor="#94A3B8"
            value={searchText}
            onChangeText={setSearchText}
          />
          {searchText.length > 0 && (
            <TouchableOpacity onPress={() => setSearchText('')} style={styles.clearButton}>
              <Ionicons name="close-circle" size={20} color="#94A3B8" />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {isLoading ? (
        <View style={styles.loadingContainer}>
          <LoadingIndicator size="large" />
          <Text style={styles.loadingText}>Đang tải...</Text>
        </View>
      ) : error ? (
        renderError()
      ) : (
        <FlatList
          data={filteredDegrees}
          renderItem={renderDegree}
          keyExtractor={(item) => item.id.toString()}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          refreshControl={<RefreshControl refreshing={isLoading} onRefresh={refetch} />}
          ListEmptyComponent={renderEmptyState}
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
    padding: 20,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  backButton: {
    marginRight: 12,
  },
  headerContent: {
    flex: 1,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#1E293B',
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
    marginTop: 2,
  },
  searchContainer: {
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  searchInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    color: '#1E293B',
    marginLeft: 12,
  },
  clearButton: {
    padding: 4,
  },
  listContent: {
    padding: 20,
    paddingBottom: 40,
  },
  degreeCard: {
    backgroundColor: '#FFF',
    borderRadius: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  degreeHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    padding: 20,
    paddingBottom: 12,
  },
  degreeInfo: {
    flex: 1,
  },
  degreeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    alignSelf: 'flex-start',
    marginBottom: 8,
  },
  degreeBadgeText: {
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 4,
  },
  degreeName: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1E293B',
    lineHeight: 24,
  },
  degreeDetails: {
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailLabel: {
    fontSize: 14,
    color: '#64748B',
    marginLeft: 8,
    minWidth: 100,
  },
  detailValue: {
    fontSize: 14,
    color: '#1E293B',
    fontWeight: '500',
    flex: 1,
  },
  descriptionContainer: {
    marginTop: 8,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  descriptionText: {
    fontSize: 14,
    color: '#64748B',
    lineHeight: 20,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#1E293B',
    marginTop: 16,
  },
  emptySubtitle: {
    fontSize: 16,
    color: '#64748B',
    textAlign: 'center',
    marginTop: 8,
  },
  errorState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  errorTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#1E293B',
    marginTop: 16,
  },
  errorSubtitle: {
    fontSize: 16,
    color: '#64748B',
    textAlign: 'center',
    marginTop: 8,
    marginBottom: 24,
  },
  retryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#3B82F6',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
  },
  retryText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFF',
    marginLeft: 8,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  loadingText: {
    fontSize: 16,
    color: '#64748B',
    marginTop: 16,
  },
});

export default Degrees;