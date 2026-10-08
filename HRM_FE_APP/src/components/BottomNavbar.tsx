import React from 'react';
import {
  View,
  TouchableOpacity,
  Text,
  StyleSheet,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { useGetUnreadCountQuery } from '../store/api/notificationApi';

interface TabConfig {
  name: string;
  label: string;
  icon: string;
  isCenter?: boolean;
}

const EMPLOYEE_TABS: TabConfig[] = [
  { name: 'Dashboard', label: 'Trang chủ', icon: 'home' },
  { name: 'LeaveRequest', label: 'Đơn từ', icon: 'document-text' },
  { name: 'Attendance', label: 'Chấm công', icon: 'happy', isCenter: true },
  { name: 'Notifications', label: 'Thông báo', icon: 'notifications' },
  { name: 'Profile', label: 'Cá nhân', icon: 'person' },
];

const MANAGER_TABS: TabConfig[] = [
  { name: 'Dashboard', label: 'Dashboard', icon: 'stats-chart' },
  { name: 'Employees', label: 'Nhân viên', icon: 'people' },
  { name: 'Attendance', label: 'Chấm công', icon: 'happy', isCenter: true },
  { name: 'Notifications', label: 'Thông báo', icon: 'notifications' },
  { name: 'Profile', label: 'Cá nhân', icon: 'person' },
];

const ACTIVE_COLOR = '#3B82F6';
const INACTIVE_COLOR = '#94A3B8';

const BottomNavbar: React.FC<BottomTabBarProps> = ({ state, descriptors, navigation }) => {
  const { isAuthenticated, roles } = useSelector((s: RootState) => s.auth);
  const isManager = roles.includes('MANAGER');
  const tabs = isManager ? MANAGER_TABS : EMPLOYEE_TABS;

  const { data: unreadCountData } = useGetUnreadCountQuery(undefined, { skip: !isAuthenticated });
  const unreadCount = unreadCountData?.data ?? 0;

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.navbar}>
        {tabs.map((tab, index) => {
          const isFocused = state.index === index;

          const onPress = () => {
            const event = navigation.emit({
              type: 'tabPress',
              target: state.routes[index]?.key,
              canPreventDefault: true,
            });
            if (!isFocused && !event.defaultPrevented) {
              navigation.navigate(tab.name);
            }
          };

          // --- Center (Chấm công) ---
          if (tab.isCenter) {
            return (
              <TouchableOpacity
                key={tab.name}
                style={styles.centerWrapper}
                onPress={onPress}
                activeOpacity={0.8}
              >
                <View style={[styles.centerBtn, isFocused && styles.centerBtnActive]}>
                  <Ionicons name="happy" size={28} color="#FFF" />
                </View>
                <Text
                  style={[
                    styles.centerLabel,
                    { color: isFocused ? ACTIVE_COLOR : INACTIVE_COLOR },
                  ]}
                >
                  {tab.label}
                </Text>
              </TouchableOpacity>
            );
          }

          // --- Normal tabs ---
          const showBadge = tab.name === 'Notifications' && unreadCount > 0;

          return (
            <TouchableOpacity
              key={tab.name}
              style={styles.tabButton}
              onPress={onPress}
              activeOpacity={0.7}
            >
              <View>
                <Ionicons
                  name={
                    isFocused
                      ? (tab.icon as any)
                      : (`${tab.icon}-outline` as any)
                  }
                  size={22}
                  color={isFocused ? ACTIVE_COLOR : INACTIVE_COLOR}
                />
                {showBadge && (
                  <View style={styles.badge}>
                    <Text style={styles.badgeText}>
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </Text>
                  </View>
                )}
              </View>
              <Text
                style={[
                  styles.tabLabel,
                  { color: isFocused ? ACTIVE_COLOR : INACTIVE_COLOR },
                ]}
              >
                {tab.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFF',
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 8,
  },
  navbar: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-around',
    paddingBottom: 2,
  },
  // Normal tabs
  tabButton: {
    flex: 1,
    alignItems: 'center',
    paddingTop: 8,
    paddingBottom: 4,
  },
  tabLabel: {
    fontSize: 10,
    marginTop: 3,
    fontWeight: '600',
  },
  // Badge
  badge: {
    position: 'absolute',
    top: -4,
    right: -10,
    backgroundColor: '#EF4444',
    borderRadius: 9,
    minWidth: 16,
    height: 16,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 3,
    borderWidth: 1.5,
    borderColor: '#FFF',
  },
  badgeText: {
    color: '#FFF',
    fontSize: 9,
    fontWeight: '700',
  },
  // Center raised button
  centerWrapper: {
    flex: 1,
    alignItems: 'center',
    marginTop: -20,
  },
  centerBtn: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#3B82F6',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#3B82F6',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.35,
    shadowRadius: 8,
    elevation: 8,
    borderWidth: 3,
    borderColor: '#FFF',
  },
  centerBtnActive: {
    backgroundColor: '#1D4ED8',
  },
  centerLabel: {
    fontSize: 10,
    marginTop: 4,
    fontWeight: '600',
  },
});

export default BottomNavbar;