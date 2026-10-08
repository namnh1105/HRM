import React, { useRef, useEffect, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  Animated,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useAuthLogin } from '../hooks';
import CustomAlert from '../components/CustomAlert';
import LoadingIndicator from '../components/LoadingIndicator';

const PRIMARY_BUTTON_COLOR = '#6B4CE6';

const Login = ({ navigation }: any) => {
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(50)).current;
  const [alertConfig, setAlertConfig] = useState({
    visible: false,
    title: '',
    message: '',
    type: 'info' as 'success' | 'error' | 'info' | 'warning',
  });

  const {
    email,
    setEmail,
    password,
    setPassword,
    isLoading,
    handleLogin,
  } = useAuthLogin(navigation);

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 600,
        useNativeDriver: true,
      }),
      Animated.spring(slideAnim, {
        toValue: 0,
        tension: 50,
        friction: 7,
        useNativeDriver: true,
      }),
    ]).start();
  }, []);

  const onLoginPress = async () => {
    try {
      await handleLogin();
    } catch (error: any) {
      setAlertConfig({
        visible: true,
        title: 'Đăng nhập thất bại',
        message: error?.message || 'Vui lòng kiểm tra lại thông tin đăng nhập',
        type: 'error',
      });
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        {/* Login content */}

        <Animated.View 
          style={[
            styles.contentContainer,
            {
              opacity: fadeAnim,
              transform: [{ translateY: slideAnim }],
            },
          ]}
        >
          <Text style={styles.title}>Đăng nhập HRM</Text>

          <View style={styles.formContainer}>
            <View style={styles.inputContainer}>
              <Ionicons name="mail-outline" size={20} color="#666" style={styles.inputIcon} />
              <TextInput
                style={styles.input}
                value={email}
                onChangeText={setEmail}
                placeholder="Email"
                placeholderTextColor="#999"
                autoCapitalize="none"
                keyboardType="email-address"
              />
            </View>
            <View style={styles.inputContainer}>
              <Ionicons name="lock-closed-outline" size={20} color="#666" style={styles.inputIcon} />
              <TextInput
                style={styles.input}
                secureTextEntry
                value={password}
                onChangeText={setPassword}
                placeholder="Mật khẩu"
                placeholderTextColor="#999"
              />
            </View>

            <TouchableOpacity
              onPress={() => navigation.navigate('ForgotPassword')}
              activeOpacity={0.7}
              style={styles.forgotPasswordBtn}
            >
              <Text style={styles.forgotPasswordText}>Quên mật khẩu?</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            style={[styles.button, styles.primaryButton, { opacity: isLoading ? 0.6 : 1 }]}
            onPress={onLoginPress}
            disabled={isLoading}
            activeOpacity={0.8}
          >
            {isLoading ? (
              <LoadingIndicator size="small" color="#fff" />
            ) : (
              <Text style={styles.buttonText}>Đăng nhập</Text>
            )}
          </TouchableOpacity>
        </Animated.View>
      </KeyboardAvoidingView>

      <CustomAlert
        visible={alertConfig.visible}
        title={alertConfig.title}
        message={alertConfig.message}
        type={alertConfig.type}
        onClose={() => setAlertConfig({ ...alertConfig, visible: false })}
      />
    </SafeAreaView>
  );
};

export default Login;

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: "#fff" 
  },
  keyboardView: {
    flex: 1,
    paddingHorizontal: 35,
    justifyContent: 'center',
  },
  contentContainer: {
    width: '100%',
  },

  title: { 
    fontSize: 24, 
    marginBottom: 30, 
    textAlign: "center", 
    fontWeight: "700",
    color: '#333',
  },
  formContainer: { 
    marginBottom: 25,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: '#E0E0E0',
    borderRadius: 12,
    marginBottom: 15,
    backgroundColor: '#F8F9FA',
    paddingHorizontal: 15,
  },
  inputIcon: {
    marginRight: 10,
  },
  input: { 
    flex: 1,
    padding: 15,
    fontSize: 16,
    color: '#333',
  },
  button: { 
    padding: 16, 
    borderRadius: 12, 
    alignItems: "center", 
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  primaryButton: {
    backgroundColor: PRIMARY_BUTTON_COLOR,
  },
  forgotPasswordBtn: {
    alignSelf: 'flex-end',
    marginTop: 8,
  },
  forgotPasswordText: {
    fontSize: 14,
    color: PRIMARY_BUTTON_COLOR,
    fontWeight: '600',
  },
  buttonText: { 
    color: "#fff", 
    fontSize: 17, 
    fontWeight: "700"
  },
  dividerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 25,
  },
  divider: {
    flex: 1,
    height: 1,
    backgroundColor: '#E0E0E0',
  },
  dividerText: {
    marginHorizontal: 15,
    fontSize: 13,
    color: '#999',
    fontWeight: '600',
  },
  socialButton: { 
    padding: 15, 
    borderRadius: 12, 
    flexDirection: "row", 
    alignItems: "center", 
    justifyContent: "center", 
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 3,
    elevation: 2,
  },
  bottom: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 20,
  },
  bottomText: {
    color: '#666',
    fontSize: 15,
  },
  register: {
    color: '#FF6B6B',
    fontWeight: '700',
    fontSize: 15,
  },
});
