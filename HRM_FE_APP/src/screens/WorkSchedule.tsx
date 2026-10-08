import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Modal,
  Alert,
  TextInput,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import {
  useWorkSchedule,
  HOLIDAYS,
  getShiftColor,
  normaliseDate,
} from '../hooks/useWorkSchedule';
import { useGetAllEmployeesQuery } from '../store/api/employeeApi';
import type { EmployeeWorkShift } from '../types/workshift';

const WorkSchedule: React.FC = () => {
  const {
    shifts,
    todayShifts,
    isLoading,
    isAssigning,
    error,
    refetch,
    weekSchedule,
    assignWorkShift,
    deleteWorkShift,
    isManager,
    storeId,
    goBack,
    refreshing,
    onRefresh,
    allAssignments,
    currentDate,
    setCurrentDate,
  } = useWorkSchedule();

  const { data: employeeRes } = useGetAllEmployeesQuery();
  const allEmployees = employeeRes?.data ?? [];

  const [assignModalVisible, setAssignModalVisible] = useState(false);
  const [selectedDay, setSelectedDay] = useState<string | null>(null);
  const [selectedEmployeeIds, setSelectedEmployeeIds] = useState<string[]>([]);
  const [initialEmployeeIds, setInitialEmployeeIds] = useState<string[]>([]);
  const [selectedShiftIds, setSelectedShiftIds] = useState<string[]>([]);
  const [employeeQuery, setEmployeeQuery] = useState('');
  const [showEmployeeDropdown, setShowEmployeeDropdown] = useState(false);
  const [shiftQuery, setShiftQuery] = useState('');
  const [showShiftDropdown, setShowShiftDropdown] = useState(false);

  const toLocalDateStr = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  const todayStr = toLocalDateStr(new Date());
  const isPastDate = (dateStr: string) => dateStr < todayStr;

  const filteredEmployees = allEmployees.filter((emp) => {
    // Only show employees that belong to the manager's store, if storeId is present
    if (isManager && storeId && emp.storeId !== storeId) {
      return false;
    }
    const query = employeeQuery.trim().toLowerCase();
    if (!query) return true;
    const name = (emp.fullName || '').toLowerCase();
    const code = (emp.employeeCode || '').toLowerCase();
    return name.includes(query) || code.includes(query);
  });

  const filteredShifts = shifts.filter((s) => {
    const query = shiftQuery.trim().toLowerCase();
    if (!query) return true;
    return s.name.toLowerCase().includes(query) || s.code?.toLowerCase()?.includes(query);
  });

  const prevWeek = () => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() - 7);
    setCurrentDate(d);
  };
  const nextWeek = () => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() + 7);
    setCurrentDate(d);
  };

  const handleAssign = async () => {
    if (!selectedDay || selectedShiftIds.length === 0) {
      Alert.alert('Thông báo', 'Vui lòng chọn đầy đủ thông tin');
      return;
    }

    if (isPastDate(selectedDay)) {
      Alert.alert('Thông báo', 'Không thể gán ca cho ngày đã qua');
      return;
    }

    try {
      // Find removed employees: were in initial, but no longer in selected
      const removedIds = initialEmployeeIds.filter(id => !selectedEmployeeIds.includes(id));
      
      // Find added employees: are in selected, but were not in initial
      const addedIds = selectedEmployeeIds.filter(id => !initialEmployeeIds.includes(id));

      // 1. Delete removed assignments
      for (const employeeId of removedIds) {
        const assignment = allAssignments.find(
          (a) =>
            normaliseDate(a.date) === selectedDay &&
            a.workShiftId === selectedShiftIds[0] &&
            a.employeeId === employeeId
        );
        if (assignment) {
          await deleteWorkShift(assignment.id);
        }
      }

      // 2. Add new assignments
      for (const employeeId of addedIds) {
        await assignWorkShift(employeeId, selectedShiftIds[0], selectedDay);
      }

      await refetch(); // Force refetch to ensure data is updated immediately
      Alert.alert('Thành công', 'Đã cập nhật ca làm việc thành công');
      setAssignModalVisible(false);
      setSelectedEmployeeIds([]);
      setInitialEmployeeIds([]);
      setSelectedShiftIds([]);
      setEmployeeQuery('');
      setShowEmployeeDropdown(false);
      setShiftQuery('');
      setShowShiftDropdown(false);
    } catch (err: any) {
      Alert.alert('Lỗi', err.data?.message || 'Không thể cập nhật ca làm việc');
    }
  };

  const openAssignModal = (date: string, shiftId: string) => {
    if (isPastDate(date)) {
      Alert.alert('Thông báo', 'Không thể gán ca cho ngày đã qua');
      return;
    }
    setSelectedDay(date);
    
    // Find already assigned employees for this specific day and shift
    const existing = allAssignments.filter(
      (a) => normaliseDate(a.date) === date && a.workShiftId === shiftId
    );
    const existingIds = existing.map((a) => a.employeeId);
    
    setSelectedEmployeeIds(existingIds);
    setInitialEmployeeIds(existingIds);
    
    setSelectedShiftIds([shiftId]);
    setEmployeeQuery('');
    setShowEmployeeDropdown(false);
    setShiftQuery('');
    setShowShiftDropdown(false);
    setAssignModalVisible(true);
  };

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={goBack} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Lịch làm việc</Text>
        <TouchableOpacity onPress={refetch} disabled={isLoading}>
          <Ionicons name="refresh" size={22} color="#1E293B" />
        </TouchableOpacity>
      </View>

      {isLoading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang tải lịch làm việc...</Text>
        </View>
      ) : error ? (
        <View style={styles.centered}>
          <Ionicons name="alert-circle-outline" size={48} color="#EF4444" />
          <Text style={styles.errorText}>Không thể tải dữ liệu</Text>
          <TouchableOpacity style={styles.retryBtn} onPress={refetch}>
            <Text style={styles.retryText}>Thử lại</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <ScrollView
          showsVerticalScrollIndicator={false}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#3B82F6']} />
          }
        >
          {/* Today's Shifts */}
          {todayShifts.length > 0 && (
            <>
              <Text style={styles.sectionTitle}>Ca hôm nay</Text>
              <View style={styles.todayCard}>
                {todayShifts.map((s, idx) => (
                  <View key={idx} style={styles.todayShiftRow}>
                    <View style={[styles.shiftDot, { backgroundColor: s.color }]} />
                    <View style={{ flex: 1 }}>
                      <Text style={styles.todayShiftName}>{s.name}</Text>
                      <Text style={styles.employeeNameText}>{s.employees.join(', ')}</Text>
                      {s.isUnderstaffed && (
                        <View style={styles.warningRow}>
                          <Ionicons name="alert-circle" size={12} color="#EF4444" />
                          <Text style={styles.warningText}>Thiếu nhân sự</Text>
                        </View>
                      )}
                    </View>
                    <Text style={styles.todayShiftTime}>{s.time}</Text>
                  </View>
                ))}
              </View>
            </>
          )}

          {/* Week Matrix Schedule */}
          <View style={styles.monthHeaderRow}>
            <Text style={styles.sectionTitle}>
              Tuần {weekSchedule[0]?.date} - {weekSchedule[6]?.date}
            </Text>
            <View style={styles.monthControls}>
              <TouchableOpacity onPress={prevWeek} style={styles.monthBtn}>
                <Ionicons name="chevron-back" size={20} color="#3B82F6" />
              </TouchableOpacity>
              <TouchableOpacity onPress={nextWeek} style={styles.monthBtn}>
                <Ionicons name="chevron-forward" size={20} color="#3B82F6" />
              </TouchableOpacity>
            </View>
          </View>

          <View style={styles.matrixContainer}>
            <View style={{ flexDirection: 'row' }}>
              {/* Sticky First Column (Days of Week) */}
              <View style={styles.stickyCol}>
                <View style={[styles.matrixHeaderCell, styles.matrixFirstCell, { width: 80 }]}>
                  <Text style={styles.matrixHeaderText}>Ngày \ Ca</Text>
                </View>
                {weekSchedule.map((day) => {
                  const maxEmps = Math.max(...shifts.map(s => {
                    const info = day.shifts.find(sh => sh.id === s.id);
                    return info ? info.employees.length : 0;
                  }), 1);
                  const rowHeight = Math.max(80, maxEmps * 18 + 24);

                  return (
                    <View key={day.fullDate} style={[styles.matrixRowCell, { height: rowHeight, width: 80 }]}>
                      <Text style={styles.matrixEmpText}>{day.dayName}</Text>
                      <Text style={{ fontSize: 10, color: '#64748B' }}>{day.date}</Text>
                    </View>
                  );
                })}
              </View>

              {/* Scrollable Right Columns (Shifts) */}
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ flex: 1 }}>
                <View>
                  {/* Header Row (Shifts) */}
                  <View style={{ flexDirection: 'row' }}>
                    {shifts.map((s) => (
                      <View key={s.id} style={[styles.matrixHeaderCell, { width: 140 }]}>
                        <Text style={[styles.matrixHeaderText, { textAlign: 'center' }]}>{s.name}</Text>
                        <Text style={{ fontSize: 10, color: '#64748B', textAlign: 'center' }}>
                          {s.startTime.slice(0, 5)} - {s.endTime.slice(0, 5)}
                        </Text>
                      </View>
                    ))}
                  </View>
                  {/* Day Rows */}
                  {weekSchedule.map((day) => {
                    const maxEmps = Math.max(...shifts.map(s => {
                      const info = day.shifts.find(sh => sh.id === s.id);
                      return info ? info.employees.length : 0;
                    }), 1);
                    const rowHeight = Math.max(80, maxEmps * 18 + 24);

                    return (
                      <View key={day.fullDate} style={{ flexDirection: 'row' }}>
                        {shifts.map((s) => {
                          const shiftInfo = day.shifts.find((sh) => sh.id === s.id);
                          const assignedEmployees = shiftInfo ? shiftInfo.employees : [];
                          return (
                            <TouchableOpacity
                              key={s.id}
                              style={[styles.matrixDataCell, { width: 140, height: rowHeight, padding: 4, justifyContent: 'flex-start' }]}
                              onPress={() => isManager ? openAssignModal(day.fullDate, s.id) : null}
                              disabled={isPastDate(day.fullDate) || !isManager}
                            >
                              {assignedEmployees.length === 0 ? (
                                <View style={{ flex: 1, justifyContent: 'center' }}><Text style={styles.matrixEmptyCell}>-</Text></View>
                              ) : (
                                <View style={{ gap: 2, padding: 4 }}>
                                  {assignedEmployees.map((empName, i) => (
                                    <Text key={i} style={{ fontSize: 11, color: '#1E293B' }} numberOfLines={1}>
                                      • {empName}
                                    </Text>
                                  ))}
                                </View>
                              )}
                            </TouchableOpacity>
                          );
                        })}
                      </View>
                    );
                  })}
                </View>
              </ScrollView>
            </View>
          </View>

          {/* Modal for Assigning Shift */}
          <Modal
            visible={assignModalVisible}
            transparent
            animationType="fade"
            onRequestClose={() => {
              setAssignModalVisible(false);
            }}
          >
            <View style={styles.modalOverlay}>
              <View style={[styles.modalContent, { maxHeight: '85%' }]}>
                <Text style={styles.modalTitle}>Gán nhân viên vào ca</Text>

                <View style={{ marginBottom: 16, marginTop: 8 }}>
                  <Text style={styles.modalSub}>
                    <Text style={{ fontWeight: '600' }}>Ngày:</Text> {selectedDay}
                  </Text>
                  {selectedShiftIds.length > 0 && (
                    <Text style={styles.modalSub}>
                      <Text style={{ fontWeight: '600' }}>Ca:</Text> {shifts.find((s) => s.id === selectedShiftIds[0])?.name}
                    </Text>
                  )}
                </View>

                <Text style={styles.inputLabel}>Chọn Nhân viên</Text>

                <TextInput
                  placeholder="Tìm nhân viên..."
                  value={employeeQuery}
                  onChangeText={setEmployeeQuery}
                  style={[styles.searchInput, { marginTop: 8, marginBottom: 8 }]}
                />

                {selectedEmployeeIds.length > 0 && (
                  <View style={styles.selectedChips}>
                    {selectedEmployeeIds.map((id) => {
                      const emp = allEmployees.find((e) => e.id === id);
                      if (!emp) return null;
                      const isInitiallyAssigned = initialEmployeeIds.includes(id);
                      return (
                        <TouchableOpacity
                          key={id}
                          style={[
                            styles.chip,
                            isInitiallyAssigned && { backgroundColor: '#D1FAE5' } // light green for initial
                          ]}
                          onPress={() =>
                            setSelectedEmployeeIds((prev) => prev.filter((x) => x !== id))
                          }
                        >
                          <Text style={[
                            styles.chipText,
                            isInitiallyAssigned && { color: '#065F46', fontWeight: '600' } // dark green text
                          ]}>
                            {emp.fullName}
                          </Text>
                          <Ionicons
                            name="close"
                            size={14}
                            color={isInitiallyAssigned ? '#065F46' : '#64748B'}
                          />
                        </TouchableOpacity>
                      );
                    })}
                  </View>
                )}

                <ScrollView style={{ flexShrink: 1, minHeight: 150 }} keyboardShouldPersistTaps="handled">
                  {filteredEmployees.length === 0 ? (
                    <Text style={styles.emptyText}>Không tìm thấy nhân viên</Text>
                  ) : (
                    filteredEmployees.map((emp) => {
                      const isInitiallyAssigned = initialEmployeeIds.includes(emp.id);
                      const isCurrentlySelected = selectedEmployeeIds.includes(emp.id);
                      
                      let itemStyle = styles.dropdownItem;
                      let textStyle = styles.dropdownItemText;
                      
                      if (isCurrentlySelected) {
                        if (isInitiallyAssigned) {
                          itemStyle = [styles.dropdownItem, styles.dropdownItemAssigned];
                          textStyle = [styles.dropdownItemText, styles.dropdownItemTextAssigned];
                        } else {
                          itemStyle = [styles.dropdownItem, styles.dropdownItemActive];
                          textStyle = [styles.dropdownItemText, styles.dropdownItemTextActive];
                        }
                      }

                      return (
                        <TouchableOpacity
                          key={emp.id}
                          style={itemStyle}
                          onPress={() => {
                            setSelectedEmployeeIds((prev) =>
                              prev.includes(emp.id)
                                ? prev.filter((x) => x !== emp.id)
                                : [...prev, emp.id]
                            );
                          }}
                        >
                          <Text style={textStyle}>
                            {emp.fullName} {isInitiallyAssigned && isCurrentlySelected && '(Đã gán)'}
                          </Text>
                        </TouchableOpacity>
                      );
                    })
                  )}
                </ScrollView>

                <View style={[styles.modalButtons, { marginTop: 16 }]}>
                  <TouchableOpacity
                    style={styles.cancelBtn}
                    onPress={() => {
                      setAssignModalVisible(false);
                    }}
                  >
                    <Text style={styles.cancelText}>Hủy</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[styles.confirmBtn, isAssigning && { opacity: 0.7 }]}
                    onPress={handleAssign}
                    disabled={isAssigning}
                  >
                    {isAssigning ? (
                      <ActivityIndicator size="small" color="#FFF" />
                    ) : (
                      <Text style={styles.confirmText}>Xác nhận</Text>
                    )}
                  </TouchableOpacity>
                </View>
              </View>
            </View>
          </Modal>

          {HOLIDAYS.map((h, idx) => (
            <View key={idx} style={styles.holidayCard}>
              <View style={styles.holidayIcon}>
                <Ionicons name="flag" size={18} color="#EF4444" />
              </View>
              <View style={styles.holidayInfo}>
                <Text style={styles.holidayName}>{h.name}</Text>
                <Text style={styles.holidayDate}>{h.date}</Text>
              </View>
            </View>
          ))}

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
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1E293B',
    paddingHorizontal: 20,
    marginTop: 20,
    marginBottom: 12,
  },
  // Loading / Error
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
    fontSize: 15,
    fontWeight: '600',
    color: '#EF4444',
  },
  retryBtn: {
    marginTop: 16,
    paddingHorizontal: 24,
    paddingVertical: 10,
    backgroundColor: '#3B82F6',
    borderRadius: 8,
  },
  retryText: {
    color: '#FFF',
    fontWeight: '600',
    fontSize: 14,
  },
  // Today's shifts
  todayCard: {
    marginHorizontal: 20,
    backgroundColor: '#EFF6FF',
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  todayShiftRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 6,
  },
  shiftDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 10,
  },
  todayShiftName: {
    fontSize: 14,
    fontWeight: '700',
    color: '#1E40AF',
  },
  employeeNameText: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 1,
  },
  warningRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 2,
  },
  warningText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#EF4444',
  },
  todayShiftTime: {
    fontSize: 13,
    color: '#3B82F6',
    fontWeight: '600',
  },
  // Month Matrix
  monthHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingRight: 20,
  },
  monthControls: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 20,
  },
  monthBtn: {
    padding: 6,
    backgroundColor: '#EFF6FF',
    borderRadius: 8,
  },
  matrixContainer: {
    marginHorizontal: 20,
    backgroundColor: '#FFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    overflow: 'hidden',
    marginTop: 12,
    marginBottom: 20,
  },
  stickyCol: {
    backgroundColor: '#F8FAFC',
    borderRightWidth: 1,
    borderRightColor: '#E2E8F0',
    width: 100,
    zIndex: 10,
  },
  matrixHeaderCell: {
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
    width: 40,
  },
  matrixFirstCell: {
    width: 100,
    backgroundColor: '#F1F5F9',
  },
  matrixHeaderText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
  },
  matrixRowCell: {
    height: 48,
    justifyContent: 'center',
    paddingHorizontal: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  matrixEmpText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#1E293B',
  },
  matrixDataCell: {
    width: 40,
    height: 48,
    justifyContent: 'center',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    borderRightWidth: 1,
    borderRightColor: '#F8FAFC',
  },
  matrixEmptyCell: {
    color: '#CBD5E1',
    fontSize: 12,
  },
  matrixShiftBadge: {
    width: 16,
    height: 16,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    margin: 1,
  },
  matrixShiftBadgeText: {
    color: '#FFF',
    fontSize: 9,
    fontWeight: 'bold',
  },
  // Holidays
  holidayCard: {
    flexDirection: 'row',
    alignItems: 'center',
    marginHorizontal: 20,
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  holidayIcon: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: '#FEE2E2',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  holidayInfo: {
    flex: 1,
  },
  holidayName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1E293B',
  },
  holidayDate: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 2,
  },
  // Add & Modal
  addBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
    backgroundColor: '#EFF6FF',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    alignSelf: 'flex-start',
  },
  addText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#3B82F6',
    marginLeft: 4,
  },
  addTextDisabled: {
    color: '#94A3B8',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: '#FFF',
    borderRadius: 20,
    padding: 24,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 4,
  },
  modalSub: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 20,
  },
  inputLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
    marginBottom: 8,
  },
  dropdownContainer: {
    marginBottom: 16,
    position: 'relative',
  },
  dropdownTrigger: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#3B82F6',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 12,
    backgroundColor: '#FFF',
  },
  dropdownTriggerText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1E293B',
  },
  dropdownPanel: {
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0,
    borderWidth: 1,
    borderColor: '#3B82F6',
    borderRadius: 10,
    backgroundColor: '#FFF',
    padding: 10,
    zIndex: 50,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.12,
    shadowRadius: 8,
  },
  searchInput: {
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    color: '#334155',
    backgroundColor: '#FFF',
  },
  dropdownList: {
    marginTop: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    overflow: 'hidden',
    backgroundColor: '#FFF',
  },
  selectedChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginTop: 8,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
    backgroundColor: '#F1F5F9',
    borderRadius: 999,
  },
  chipText: {
    fontSize: 12,
    color: '#334155',
  },
  dropdownItem: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  dropdownItemActive: {
    backgroundColor: '#3B82F6',
  },
  dropdownItemAssigned: {
    backgroundColor: '#10B981',
  },
  dropdownItemText: {
    fontSize: 14,
    color: '#334155',
  },
  dropdownItemTextActive: {
    color: '#FFF',
    fontWeight: '600',
  },
  dropdownItemTextAssigned: {
    color: '#FFF',
    fontWeight: '700',
  },
  pickerContainer: {
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    marginBottom: 16,
    overflow: 'hidden',
  },
  pickerItem: {
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  pickerItemActive: {
    backgroundColor: '#3B82F6',
  },
  pickerItemText: {
    fontSize: 14,
    color: '#334155',
  },
  pickerItemTextActive: {
    color: '#FFF',
    fontWeight: '600',
  },
  emptyText: {
    padding: 12,
    fontSize: 13,
    color: '#94A3B8',
  },
  modalButtons: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 8,
  },
  cancelBtn: {
    flex: 1,
    paddingVertical: 14,
    alignItems: 'center',
    borderRadius: 12,
    backgroundColor: '#F1F5F9',
  },
  cancelText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#64748B',
  },
  confirmBtn: {
    flex: 2,
    paddingVertical: 14,
    alignItems: 'center',
    borderRadius: 12,
    backgroundColor: '#3B82F6',
  },
  confirmText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFF',
  },
});

export default WorkSchedule;
