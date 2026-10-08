import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  Platform,
  Modal,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useCreateLeaveRequest } from '../hooks/useCreateLeaveRequest';
import type { LeaveType } from '../types/leave';
import { LEAVE_TYPE_LABELS } from '../types/leave';

const LEAVE_TYPES: { value: LeaveType; icon: string }[] = [
  { value: 'ANNUAL_LEAVE', icon: 'sunny' },
  { value: 'SICK_LEAVE', icon: 'medkit' },
  { value: 'UNPAID_LEAVE', icon: 'airplane' },
  { value: 'COMPENSATORY_LEAVE', icon: 'time' },
  { value: 'OTHER', icon: 'document-text' },
];

const CreateLeaveRequest: React.FC = () => {
  const {
    selectedType,
    setSelectedType,
    startDate,
    setStartDate,
    endDate,
    setEndDate,
    reason,
    setReason,
    isLoading,
    handleSubmit,
    goBack,
    formatDateDisplay,
  } = useCreateLeaveRequest();

  const [showPicker, setShowPicker] = React.useState(false);
  const [pickingFor, setPickingFor] = React.useState<'start' | 'end'>('start');

  const openPicker = (type: 'start' | 'end') => {
    setPickingFor(type);
    setShowPicker(true);
  };

  const onDateChange = (event: any, selectedDate?: Date) => {
    setShowPicker(Platform.OS === 'ios'); // On iOS inline mode, we might want to keep it visible or handle differently
    if (event.type === 'set' && selectedDate) {
      if (pickingFor === 'start') {
        setStartDate(selectedDate);
        // Automatically set end date to start date if it's currently before or same
        if (endDate < selectedDate) {
          setEndDate(selectedDate);
        }
      } else {
        setEndDate(selectedDate);
      }
      if (Platform.OS === 'android') setShowPicker(false);
    } else {
      setShowPicker(false);
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={goBack} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Tạo đơn mới</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ padding: 20 }}
      >
        {/* Leave Type Selection */}
        <Text style={styles.label}>Loại đơn</Text>
        <View style={styles.typeGrid}>
          {LEAVE_TYPES.map((lt) => (
            <TouchableOpacity
              key={lt.value}
              style={[
                styles.typeCard,
                selectedType === lt.value && styles.typeCardActive,
              ]}
              onPress={() => setSelectedType(lt.value)}
            >
              <Ionicons
                name={lt.icon as any}
                size={22}
                color={selectedType === lt.value ? '#3B82F6' : '#94A3B8'}
              />
              <Text
                style={[
                  styles.typeLabel,
                  selectedType === lt.value && styles.typeLabelActive,
                ]}
                numberOfLines={1}
              >
                {LEAVE_TYPE_LABELS[lt.value]}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Start Date */}
        <Text style={styles.label}>Ngày bắt đầu</Text>
        <TouchableOpacity 
          style={styles.inputRow} 
          onPress={() => openPicker('start')}
          activeOpacity={0.7}
        >
          <Ionicons name="calendar-outline" size={20} color="#3B82F6" style={{ marginRight: 10 }} />
          <Text style={styles.inputText}>
            {formatDateDisplay(startDate)}
          </Text>
        </TouchableOpacity>

        {/* End Date */}
        <Text style={styles.label}>Ngày kết thúc</Text>
        <TouchableOpacity 
          style={styles.inputRow} 
          onPress={() => openPicker('end')}
          activeOpacity={0.7}
        >
          <Ionicons name="calendar-outline" size={20} color="#3B82F6" style={{ marginRight: 10 }} />
          <Text style={styles.inputText}>
            {formatDateDisplay(endDate)}
          </Text>
        </TouchableOpacity>

        {showPicker && (
          <Modal
            transparent
            animationType="fade"
            visible={showPicker}
            onRequestClose={() => setShowPicker(false)}
          >
            <TouchableOpacity 
              style={styles.modalOverlay} 
              activeOpacity={1} 
              onPress={() => setShowPicker(false)}
            >
              <View style={styles.modalContent}>
                <View style={styles.modalHeader}>
                  <Text style={styles.modalTitle}>
                    Chọn ngày {pickingFor === 'start' ? 'bắt đầu' : 'kết thúc'}
                  </Text>
                  <TouchableOpacity onPress={() => setShowPicker(false)}>
                    <Ionicons name="close" size={24} color="#64748B" />
                  </TouchableOpacity>
                </View>
                
                <DateTimePicker
                  value={pickingFor === 'start' ? startDate : endDate}
                  mode="date"
                  display={Platform.OS === 'ios' ? 'inline' : 'calendar'}
                  onChange={onDateChange}
                  minimumDate={pickingFor === 'end' ? startDate : new Date()}
                  themeVariant="light"
                />

                {Platform.OS === 'ios' && (
                  <TouchableOpacity 
                    style={styles.modalDoneBtn}
                    onPress={() => setShowPicker(false)}
                  >
                    <Text style={styles.modalDoneText}>Xác nhận</Text>
                  </TouchableOpacity>
                )}
              </View>
            </TouchableOpacity>
          </Modal>
        )}

        {/* Reason */}
        <Text style={styles.label}>Lý do</Text>
        <TextInput
          style={styles.textArea}
          placeholder="Nhập lý do..."
          placeholderTextColor="#CBD5E1"
          value={reason}
          onChangeText={setReason}
          multiline
          numberOfLines={4}
          textAlignVertical="top"
        />

        {/* Submit */}
        <TouchableOpacity
          style={[styles.submitBtn, isLoading && { opacity: 0.6 }]}
          onPress={handleSubmit}
          activeOpacity={0.8}
          disabled={isLoading}
        >
          <Ionicons name={isLoading ? 'hourglass' : 'paper-plane'} size={20} color="#FFF" />
          <Text style={styles.submitText}>{isLoading ? 'Đang gửi...' : 'Gửi đơn'}</Text>
        </TouchableOpacity>
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
  // Form
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
    marginBottom: 8,
    marginTop: 16,
  },
  typeGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  typeCard: {
    width: '30%',
    marginRight: '3.33%',
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 2,
    borderColor: '#F1F5F9',
  },
  typeCardActive: {
    borderColor: '#3B82F6',
    backgroundColor: '#EFF6FF',
  },
  typeLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: '#94A3B8',
    marginTop: 6,
    textAlign: 'center',
  },
  typeLabelActive: {
    color: '#3B82F6',
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF',
    borderRadius: 12,
    paddingHorizontal: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  inputText: {
    flex: 1,
    height: 48,
    lineHeight: 48,
    fontSize: 15,
    color: '#1E293B',
  },
  textArea: {
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 14,
    fontSize: 15,
    color: '#1E293B',
    minHeight: 100,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  submitBtn: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#3B82F6',
    borderRadius: 14,
    paddingVertical: 16,
    marginTop: 32,
    shadowColor: '#3B82F6',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  submitText: {
    color: '#FFF',
    fontSize: 16,
    fontWeight: '700',
    marginLeft: 8,
  },
  // Modal
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalContent: {
    width: '100%',
    backgroundColor: '#FFF',
    borderRadius: 24,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.1,
    shadowRadius: 20,
    elevation: 10,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
    paddingHorizontal: 4,
  },
  modalTitle: {
    fontSize: 17,
    fontWeight: '700',
    color: '#1E293B',
  },
  modalDoneBtn: {
    backgroundColor: '#3B82F6',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: 16,
  },
  modalDoneText: {
    color: '#FFF',
    fontSize: 15,
    fontWeight: '700',
  },
});

export default CreateLeaveRequest;
