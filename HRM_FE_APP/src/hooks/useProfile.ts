import { Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { logout } from '../store/slices/authSlice';
import { useGetMyProfileQuery } from '../store/api/employeeApi';

interface MenuItem {
  icon: string;
  label: string;
  color: string;
  onPress: () => void;
  badge?: string;
}

export const useProfile = () => {
  const navigation = useNavigation<any>();
  const dispatch = useDispatch();
  const { user } = useSelector((state: RootState) => state.auth);
  const { data: profileData, isLoading, error, refetch } = useGetMyProfileQuery(undefined);
  const profile = profileData?.data;

  const handleLogout = () => {
    Alert.alert('Đăng xuất', 'Bạn có chắc chắn muốn đăng xuất?', [
      { text: 'Hủy', style: 'cancel' },
      {
        text: 'Đăng xuất',
        style: 'destructive',
        onPress: () => dispatch(logout()),
      },
    ]);
  };

  const menuItems: MenuItem[] = [
    {
      icon: 'calendar',
      label: 'Lịch làm việc',
      color: '#F59E0B',
      onPress: () => navigation.navigate('WorkSchedule'),
    },
    {
      icon: 'wallet',
      label: 'Lương & phúc lợi',
      color: '#10B981',
      onPress: () => navigation.navigate('Salary'),
    },
    {
      icon: 'time',
      label: 'Lịch sử chấm công',
      color: '#3B82F6',
      onPress: () => navigation.navigate('AttendanceHistory'),
    },
    {
      icon: 'document-text',
      label: 'Hợp đồng lao động',
      color: '#8B5CF6',
      onPress: () => navigation.navigate('Contracts'),
    },
    {
      icon: 'school',
      label: 'Bằng cấp & chứng chỉ',
      color: '#06B6D4',
      onPress: () => navigation.navigate('Degrees'),
    },
    {
      icon: 'lock-closed',
      label: 'Đổi mật khẩu',
      color: '#64748B',
      onPress: () => navigation.navigate('ChangePassword'),
    },
  ];

  const displayName = profile?.fullName
    || user?.employee?.fullName
    || user?.email
    || 'Nhân viên';

  return {
    user,
    profile,
    isLoading,
    error,
    refetch,
    displayName,
    menuItems,
    handleLogout,
  };
};
