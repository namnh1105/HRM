import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  Animated,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useForgotPassword } from '../hooks/useForgotPassword';

const ForgotPassword: React.FC<{ navigation: any }> = ({ navigation }) => {
  const {
    email,
    setEmail,
    isSubmitted,
    fadeAnim,
    slideAnim,
    handleSubmit,
    goBack,
  } = useForgotPassword(navigation);

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        {/* Back */}
        <TouchableOpacity
          style={styles.backBtn}
          onPress={goBack}
          activeOpacity={0.7}
        >
          <Ionicons name="arrow-back" size={28} color="#1E293B" />
        </TouchableOpacity>

        <Animated.View
          style={[
            styles.content,
            { opacity: fadeAnim, transform: [{ translateY: slideAnim }] },
          ]}
        >
          {!isSubmitted ? (
            <>
              <View style={styles.iconBox}>
                <Ionicons name="lock-open-outline" size={48} color="#3B82F6" />
              </View>
              <Text style={styles.title}>Quên mật khẩu?</Text>
              <Text style={styles.subtitle}>
                Nhập email hoặc số điện thoại để nhận hướng dẫn đặt lại mật khẩu.
              </Text>

              <View style={styles.inputContainer}>
                <Ionicons name="mail-outline" size={20} color="#94A3B8" />
                <TextInput
                  style={styles.input}
                  placeholder="Email hoặc số điện thoại"
                  placeholderTextColor="#CBD5E1"
                  value={email}
                  onChangeText={setEmail}
                  keyboardType="email-address"
                  autoCapitalize="none"
                />
              </View>

              <TouchableOpacity
                style={styles.submitBtn}
                onPress={handleSubmit}
                activeOpacity={0.8}
              >
                <Text style={styles.submitText}>Gửi yêu cầu</Text>
              </TouchableOpacity>
            </>
          ) : (
            <>
              <View style={styles.successIcon}>
                <Ionicons name="checkmark-circle" size={64} color="#10B981" />
              </View>
              <Text style={styles.title}>Đã gửi!</Text>
              <Text style={styles.subtitle}>
                Vui lòng kiểm tra hộp thư email của bạn. Link đặt lại mật khẩu
                sẽ có hiệu lực trong 15 phút.
              </Text>
              <TouchableOpacity
                style={[styles.submitBtn, { backgroundColor: '#10B981' }]}
                onPress={goBack}
                activeOpacity={0.8}
              >
                <Text style={styles.submitText}>Quay lại đăng nhập</Text>
              </TouchableOpacity>
            </>
          )}
        </Animated.View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  keyboardView: {
    flex: 1,
  },
  backBtn: {
    marginLeft: 16,
    marginTop: 8,
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#F1F5F9',
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 32,
    marginTop: -60,
  },
  iconBox: {
    width: 80,
    height: 80,
    borderRadius: 24,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
    alignSelf: 'center',
    marginBottom: 24,
  },
  successIcon: {
    alignSelf: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    color: '#1E293B',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 32,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF',
    borderRadius: 14,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 20,
  },
  input: {
    flex: 1,
    height: 52,
    fontSize: 16,
    color: '#1E293B',
    marginLeft: 10,
  },
  submitBtn: {
    backgroundColor: '#3B82F6',
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: 'center',
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
  },
});

export default ForgotPassword;
