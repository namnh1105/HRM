import { useState } from 'react';
import { Alert } from 'react-native';
import { useDispatch } from 'react-redux';
import { useLoginMutation, LoginResponse } from '../store/api/authApi';
import { setCredentials, mapToUserInfo } from '../store/slices/authSlice';
import { saveTokens } from '../services/tokenStorage';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const useAuthLogin = (navigation: any) => {
  const dispatch = useDispatch();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const [login, { isLoading }] = useLoginMutation();

  const validateInputs = (): boolean => {
    if (!email) {
      Alert.alert('Lỗi', 'Email không được bỏ trống');
      return false;
    }
    if (!password) {
      Alert.alert('Lỗi', 'Mật khẩu không được bỏ trống');
      return false;
    }
    return true;
  };

  const saveAuthData = async (accessToken: string, refreshToken?: string, userInfo?: any) => {
    if (!accessToken) {
      console.error('[useAuthLogin] Cannot save auth data: accessToken is undefined');
      throw new Error('Access token is undefined');
    }
    
    // Save tokens to SecureStore (Keychain/Keystore)
    if (refreshToken) {
      await saveTokens(accessToken, refreshToken);
    }
    
    // Keep userInfo in AsyncStorage (non-sensitive data)
    if (userInfo) {
      await AsyncStorage.setItem('userInfo', JSON.stringify(userInfo));
    }
    console.log('[useAuthLogin] Auth data saved securely');
  };

  const handleLogin = async (): Promise<void> => {
    if (!validateInputs()) return;

    try {
      console.log('[useAuthLogin] Attempting login with email:', email);
      const response = await login({ email, password });
      
      console.log('[useAuthLogin] Login response:', JSON.stringify(response, null, 2));

      if ('data' in response && response.data) {
        const result = response.data as LoginResponse;
        console.log('[useAuthLogin] Login result:', JSON.stringify(result, null, 2));

        if (result.success && result.data && result.data.accessToken) {
          console.log('[useAuthLogin] Access token found:', !!result.data.accessToken);
          
          // Map backend user → UserInfo (consistent camelCase format)
          const mappedUser = mapToUserInfo(result.data.user);
          
          // Save mapped user to AsyncStorage (so restore reads the same format)
          await saveAuthData(result.data.accessToken, result.data.refreshToken, mappedUser);
          
          // Update Redux state - this triggers RootNavigator
          dispatch(setCredentials({
            accessToken: result.data.accessToken,
            user: mappedUser,
          }));
          
          console.log('[useAuthLogin] Login successful, credentials saved');
          
          // Auth state change will automatically trigger navigation to MainTabs
          Alert.alert('Đăng nhập thành công', 'Chào mừng bạn đã quay trở lại!');
        } else {
          console.error('[useAuthLogin] Login failed - invalid response structure:', {
            success: result.success,
            hasData: !!result.data,
            hasAccessToken: !!(result.data && result.data.accessToken)
          });
          Alert.alert('Đăng nhập thất bại', result.message || 'Phản hồi từ server không hợp lệ');
        }
      } else if ('error' in response) {
        console.error('[useAuthLogin] Login error response:', response.error);
        const errorMessage = response.error?.data?.message || 'Đăng nhập thất bại';
        Alert.alert('Đăng nhập thất bại', errorMessage);
      } else {
        console.error('[useAuthLogin] Unexpected response structure:', response);
        throw new Error('Unexpected response structure');
      }
    } catch (error: any) {
      console.error('[useAuthLogin] Login exception:', error);
      const errorMessage = error?.data?.message || error?.message || 'Không thể kết nối đến server';
      Alert.alert('Đăng nhập thất bại', errorMessage);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const navigateBack = () => {
    // Navigation is now handled automatically by conditional rendering in RootNavigator
    // No manual navigation needed
  };

  return {
    email,
    setEmail,
    password,
    setPassword,
    showPassword,
    isLoading,
    handleLogin,
    togglePasswordVisibility,
    navigateBack,
  };
};
