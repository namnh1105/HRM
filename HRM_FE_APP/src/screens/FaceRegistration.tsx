import React, { useState, useRef, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  ActivityIndicator,
  Alert,
  Animated,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { CameraView, useCameraPermissions, useMicrophonePermissions } from 'expo-camera';
import { useGetMyProfileQuery } from '../store/api/employeeApi';
import {
  useGetFaceStatusQuery,
} from '../store/api/attendanceApi';
import { useRegisterFacePolling } from '../hooks/useRegisterFacePolling';

type Step = 'intro' | 'recording' | 'processing' | 'success' | 'already_registered';

const ANGLES = [
  { key: 'front', label: 'Nhìn thẳng', icon: 'person-outline' as const, instruction: 'Nhìn thẳng vào camera' },
  { key: 'left', label: 'Quay trái', icon: 'arrow-back-outline' as const, instruction: 'Quay mặt sang bên trái' },
  { key: 'right', label: 'Quay phải', icon: 'arrow-forward-outline' as const, instruction: 'Quay mặt sang bên phải' },
  { key: 'up', label: 'Ngẩng lên', icon: 'arrow-up-outline' as const, instruction: 'Ngẩng mặt lên trên' },
  { key: 'down', label: 'Cúi xuống', icon: 'arrow-down-outline' as const, instruction: 'Cúi mặt xuống dưới' },
];

const FaceRegistration: React.FC = () => {
  const navigation = useNavigation();
  const cameraRef = useRef<CameraView>(null);
  const pulseAnim = useRef(new Animated.Value(1)).current;

  const [step, setStep] = useState<Step>('intro');
  const [isRecording, setIsRecording] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [facing, setFacing] = useState<'front' | 'back'>('front');

  const toggleCameraFacing = () => {
    setFacing(current => (current === 'back' ? 'front' : 'back'));
  };

  const [cameraPermission, requestCameraPermission] = useCameraPermissions();
  const [micPermission, requestMicPermission] = useMicrophonePermissions();

  // Fetch employee profile
  const { data: profileData } = useGetMyProfileQuery();
  const employeeId = profileData?.data?.id;

  // Check face registration status
  const { data: faceStatusData, isLoading: statusLoading, refetch: refetchFaceStatus, isFetching: statusFetching } = useGetFaceStatusQuery();
  const isRegistered = faceStatusData?.data?.registered ?? false;

  // Register face with Redis queue polling
  const {
    phase: registerPhase,
    jobStatus,
    error: registerError,
    progress: registerProgress,
    submitRegistration,
    checkJobStatus,
    reset: resetRegistration,
    isProcessing,
  } = useRegisterFacePolling();

  // Pulse animation for recording indicator
  const startPulse = useCallback(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, { toValue: 1.3, duration: 800, useNativeDriver: true }),
        Animated.timing(pulseAnim, { toValue: 1, duration: 800, useNativeDriver: true }),
      ]),
    ).start();
  }, [pulseAnim]);

  const handleStartRecording = async () => {
    if (!employeeId) {
      Alert.alert('Lỗi', 'Không thể xác định thông tin nhân viên.');
      return;
    }

    if (isRegistered) {
      setStep('already_registered');
      return;
    }

    // Request camera permission
    if (!cameraPermission?.granted) {
      const result = await requestCameraPermission();
      if (!result.granted) {
        Alert.alert('Quyền truy cập', 'Cần quyền truy cập camera để đăng ký khuôn mặt.');
        return;
      }
    }

    // Request microphone permission for video
    if (!micPermission?.granted) {
      const result = await requestMicPermission();
      if (!result.granted) {
        Alert.alert('Quyền truy cập', 'Cần quyền truy cập micro để quay video.');
        return;
      }
    }

    setStep('recording');
    setErrorMessage('');
  };

  const handleRecordVideo = async () => {
    if (!cameraRef.current || isRecording) return;

    try {
      setIsRecording(true);
      startPulse();

      const video = await cameraRef.current.recordAsync({
        maxDuration: 12,
      });

      setIsRecording(false);
      pulseAnim.stopAnimation();
      pulseAnim.setValue(1);

      if (!video?.uri) {
        Alert.alert('Lỗi', 'Không thể quay video. Vui lòng thử lại.');
        return;
      }

      // Upload video → enqueue → start polling
      setStep('processing');
      submitRegistration(video.uri);
    } catch (error: any) {
      setIsRecording(false);
      pulseAnim.stopAnimation();
      pulseAnim.setValue(1);

      const msg = 'Không thể quay video. Vui lòng thử lại.';
      setErrorMessage(msg);
      setStep('intro');
      Alert.alert('Lỗi', msg);
    }
  };

  const handleStopRecording = () => {
    if (cameraRef.current && isRecording) {
      cameraRef.current.stopRecording();
    }
  };

  // ── Loading state ──────────────────────────────────────────────────
  if (statusLoading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <Text style={styles.loadingText}>Đang kiểm tra trạng thái...</Text>
        </View>
      </SafeAreaView>
    );
  }

  // ── Already registered ─────────────────────────────────────────────
  if (isRegistered || step === 'already_registered') {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
            <Ionicons name="arrow-back" size={24} color="#1E293B" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>Đăng ký khuôn mặt</Text>
          <View style={{ width: 40 }} />
        </View>
        <View style={styles.centered}>
          <View style={styles.successCircle}>
            <Ionicons name="checkmark-circle" size={80} color="#10B981" />
          </View>
          <Text style={styles.successTitle}>Đã đăng ký khuôn mặt</Text>
          <Text style={styles.successSubtitle}>
            Khuôn mặt của bạn đã được đăng ký thành công.{'\n'}
            Mỗi nhân viên chỉ được đăng ký một lần.
          </Text>
          <TouchableOpacity
            style={styles.primaryBtn}
            onPress={() => navigation.goBack()}
          >
            <Text style={styles.primaryBtnText}>Quay lại</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  // ── Success state ──────────────────────────────────────────────────
  if (step === 'success') {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
            <Ionicons name="arrow-back" size={24} color="#1E293B" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>Đăng ký khuôn mặt</Text>
          <View style={{ width: 40 }} />
        </View>
        <View style={styles.centered}>
          <View style={styles.successCircle}>
            <Ionicons name="checkmark-circle" size={80} color="#10B981" />
          </View>
          <Text style={styles.successTitle}>Đăng ký thành công!</Text>
          <Text style={styles.successSubtitle}>
            Khuôn mặt của bạn đã được ghi nhận.{'\n'}
            Bạn có thể sử dụng khuôn mặt để chấm công từ bây giờ.
          </Text>
          <TouchableOpacity
            style={styles.primaryBtn}
            onPress={() => navigation.goBack()}
          >
            <Text style={styles.primaryBtnText}>Hoàn tất</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  // ── Processing state (polling) ──────────────────────────────────────
  // Khi polling phát hiện completed/failed → chuyển step tương ứng
  React.useEffect(() => {
    if (step !== 'processing') return;
    if (registerPhase === 'completed') {
      setStep('success');
    } else if (registerPhase === 'failed') {
      setErrorMessage(registerError || 'Đăng ký khuôn mặt thất bại.');
      setStep('intro');
      Alert.alert('Lỗi', registerError || 'Đăng ký khuôn mặt thất bại.');
    }
  }, [registerPhase, registerError, step]);

  if (step === 'processing') {
    const statusLabel =
      registerPhase === 'uploading'
        ? 'Đang tải video lên...'
        : registerProgress || 'Đang phân tích khuôn mặt...';

    const statusIcon =
      jobStatus === 'processing' ? 'hourglass-outline' : 'cloud-upload-outline';

    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.header}>
          <View style={{ width: 40 }} />
          <Text style={styles.headerTitle}>Đang xử lý</Text>
          <View style={{ width: 40 }} />
        </View>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#3B82F6" />
          <View style={{ marginTop: 16, alignItems: 'center' }}>
            <Ionicons name={statusIcon as any} size={28} color="#64748B" style={{ marginBottom: 8 }} />
            <Text style={styles.processingTitle}>{statusLabel}</Text>
          </View>
          <Text style={styles.processingSubtitle}>
            Hệ thống đang xử lý video và trích xuất{'\n'}
            dữ liệu khuôn mặt của bạn. Vui lòng đợi.
          </Text>
          {jobStatus && (
            <View style={{
              marginTop: 20,
              paddingHorizontal: 16,
              paddingVertical: 8,
              backgroundColor: '#F1F5F9',
              borderRadius: 20,
            }}>
              <Text style={{ fontSize: 13, color: '#64748B' }}>
                Trạng thái: {jobStatus === 'pending' ? 'Chờ xử lý' : jobStatus === 'processing' ? 'Đang xử lý' : jobStatus}
              </Text>
            </View>
          )}

          <View style={{ flexDirection: 'row', marginTop: 24, gap: 12 }}>
            <TouchableOpacity
              style={[styles.primaryBtn, { backgroundColor: '#F1F5F9' }]}
              onPress={() => navigation.goBack()}
            >
              <Text style={[styles.primaryBtnText, { color: '#1E293B' }]}>Thoát ra</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={styles.primaryBtn}
              onPress={checkJobStatus}
            >
              <Ionicons name="reload" size={18} color="#FFF" style={{ marginRight: 8 }} />
              <Text style={styles.primaryBtnText}>Làm mới</Text>
            </TouchableOpacity>
          </View>
        </View>
      </SafeAreaView>
    );
  }

  // ── Recording state ────────────────────────────────────────────────
  if (step === 'recording') {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.cameraContainer}>
          <CameraView
            ref={cameraRef}
            style={StyleSheet.absoluteFillObject}
            facing={facing}
            mode="video"
          />
          {/* Overlay */}
          <View style={styles.cameraOverlay}>
            {/* Top bar */}
            <View style={styles.cameraTopBar}>
              <TouchableOpacity
                onPress={() => {
                  handleStopRecording();
                  setStep('intro');
                }}
                style={styles.cameraCloseBtn}
              >
                <Ionicons name="close" size={28} color="#FFF" />
              </TouchableOpacity>

              {isRecording && (
                <View style={styles.recordingBadge}>
                  <Animated.View
                    style={[styles.recordingDot, { transform: [{ scale: pulseAnim }] }]}
                  />
                  <Text style={styles.recordingText}>Đang quay</Text>
                </View>
              )}
            </View>

            {/* Face guide oval */}
            <View style={styles.faceGuideContainer}>
              <View style={styles.faceGuideOval} />
            </View>

            {/* Instruction */}
            <View style={styles.cameraBottomSection}>
              <Text style={styles.cameraInstruction}>
                {isRecording
                  ? 'Xoay đầu từ từ: trái → phải → lên → xuống'
                  : 'Đặt khuôn mặt vào khung tròn rồi nhấn nút quay'}
              </Text>

              {/* Angle indicators */}
              {isRecording && (
                <View style={styles.angleRow}>
                  {ANGLES.map((a) => (
                    <View key={a.key} style={styles.angleItem}>
                      <Ionicons name={a.icon} size={18} color="#FFF" />
                      <Text style={styles.angleLabel}>{a.label}</Text>
                    </View>
                  ))}
                </View>
              )}

              {/* Record / Stop button */}
              <View style={{ alignItems: 'center', width: '100%' }}>
                <View style={styles.recordBtnRow}>
                  {/* Left: Flip camera button */}
                  <View style={styles.recordBtnSide}>
                    {!isRecording && (
                      <TouchableOpacity
                        onPress={toggleCameraFacing}
                        style={styles.flipCameraBtn}
                      >
                        <Ionicons name="camera-reverse" size={28} color="#FFF" />
                      </TouchableOpacity>
                    )}
                  </View>

                  {/* Center: Record button */}
                  <View style={styles.recordBtnCenter}>
                    {!isRecording ? (
                      <TouchableOpacity style={styles.recordBtn} onPress={handleRecordVideo}>
                        <View style={styles.recordBtnInner} />
                      </TouchableOpacity>
                    ) : (
                      <TouchableOpacity style={styles.stopBtn} onPress={handleStopRecording}>
                        <View style={styles.stopBtnInner} />
                      </TouchableOpacity>
                    )}
                  </View>

                  {/* Right: Empty spacer to keep record button centered */}
                  <View style={styles.recordBtnSide} />
                </View>

                <Text style={styles.recordHint}>
                  {isRecording ? 'Nhấn để dừng quay' : 'Nhấn để bắt đầu quay'}
                </Text>
              </View>
            </View>
          </View>
        </View>
      </SafeAreaView>
    );
  }

  // ── Intro / instruction screen ─────────────────────────────────────
  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={24} color="#1E293B" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Đăng ký khuôn mặt</Text>
        <View style={{ width: 40 }} />
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Hero icon */}
        <View style={styles.heroSection}>
          <View style={styles.heroCircle}>
            <Ionicons name="scan-outline" size={56} color="#3B82F6" />
          </View>
          <Text style={styles.heroTitle}>Đăng ký nhận diện khuôn mặt</Text>
          <Text style={styles.heroSubtitle}>
            Đăng ký khuôn mặt để sử dụng tính năng chấm công bằng nhận diện khuôn mặt.
            Bạn chỉ cần thực hiện một lần duy nhất.
          </Text>
        </View>

        {/* Error message */}
        {errorMessage !== '' && (
          <View style={styles.errorCard}>
            <Ionicons name="alert-circle" size={20} color="#EF4444" />
            <Text style={styles.errorText}>{errorMessage}</Text>
          </View>
        )}

        {/* Instructions card */}
        <View style={styles.instructionCard}>
          <Text style={styles.instructionCardTitle}>
            <Ionicons name="information-circle" size={18} color="#3B82F6" /> Hướng dẫn
          </Text>

          <View style={styles.stepItem}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>1</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Chuẩn bị</Text>
              <Text style={styles.stepDesc}>
                Đảm bảo khuôn mặt rõ ràng, đủ ánh sáng, không đeo kính râm hay khẩu trang.
              </Text>
            </View>
          </View>

          <View style={styles.stepItem}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>2</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Quay video</Text>
              <Text style={styles.stepDesc}>
                Nhấn nút quay, sau đó xoay đầu từ từ theo 5 hướng: thẳng, trái, phải, lên, xuống.
              </Text>
            </View>
          </View>

          <View style={styles.stepItem}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>3</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Chờ xử lý</Text>
              <Text style={styles.stepDesc}>
                Hệ thống sẽ tự động phân tích video và trích xuất dữ liệu khuôn mặt của bạn.
              </Text>
            </View>
          </View>
        </View>

        {/* Required angles */}
        <View style={styles.anglesCard}>
          <Text style={styles.anglesCardTitle}>5 góc khuôn mặt cần thiết</Text>
          <View style={styles.anglesGrid}>
            {ANGLES.map((angle) => (
              <View key={angle.key} style={styles.angleGridItem}>
                <View style={styles.angleIconCircle}>
                  <Ionicons name={angle.icon} size={22} color="#3B82F6" />
                </View>
                <Text style={styles.angleGridLabel}>{angle.label}</Text>
                <Text style={styles.angleGridDesc}>{angle.instruction}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Tips */}
        <View style={styles.tipsCard}>
          <Text style={styles.tipsTitle}>
            <Ionicons name="bulb-outline" size={16} color="#F59E0B" /> Mẹo
          </Text>
          <Text style={styles.tipsText}>• Xoay đầu từ từ, mỗi hướng giữ khoảng 2 giây</Text>
          <Text style={styles.tipsText}>• Giữ khoảng cách 30-50cm với camera</Text>
          <Text style={styles.tipsText}>• Tránh ánh sáng quá chói hoặc quá tối</Text>
          <Text style={styles.tipsText}>• Giữ biểu cảm tự nhiên</Text>
        </View>

        {/* Warning */}
        <View style={styles.warningCard}>
          <Ionicons name="warning-outline" size={20} color="#F59E0B" />
          <Text style={styles.warningText}>
            Mỗi nhân viên chỉ được đăng ký khuôn mặt một lần duy nhất. 
            Hãy đảm bảo quay video rõ ràng và đầy đủ các góc.
          </Text>
        </View>

        {/* Start button */}
        <TouchableOpacity
          style={styles.startBtn}
          onPress={handleStartRecording}
          activeOpacity={0.8}
        >
          <Ionicons name="videocam" size={22} color="#FFF" />
          <Text style={styles.startBtnText}>Bắt đầu đăng ký</Text>
        </TouchableOpacity>

        {/* Check status button */}
        <TouchableOpacity
          style={[styles.startBtn, { backgroundColor: '#F1F5F9', marginTop: 12, shadowOpacity: 0 }]}
          onPress={() => refetchFaceStatus()}
          activeOpacity={0.8}
          disabled={statusFetching}
        >
          <Ionicons name="refresh" size={22} color="#3B82F6" />
          <Text style={[styles.startBtnText, { color: '#3B82F6' }]}>
            {statusFetching ? 'Đang kiểm tra...' : 'Kiểm tra lại trạng thái'}
          </Text>
        </TouchableOpacity>

        <View style={{ height: 32 }} />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  loadingText: {
    fontSize: 16,
    color: '#64748B',
    marginTop: 12,
  },

  // ── Header ──────────────────────────────────────────────────────────
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFF',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  backBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1E293B',
  },

  // ── Scroll ──────────────────────────────────────────────────────────
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 20,
  },

  // ── Hero ────────────────────────────────────────────────────────────
  heroSection: {
    alignItems: 'center',
    marginBottom: 20,
  },
  heroCircle: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  heroTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#1E293B',
    textAlign: 'center',
  },
  heroSubtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    marginTop: 8,
    lineHeight: 20,
  },

  // ── Error ───────────────────────────────────────────────────────────
  errorCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FEF2F2',
    borderRadius: 12,
    padding: 14,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#FECACA',
  },
  errorText: {
    flex: 1,
    color: '#DC2626',
    fontSize: 13,
    marginLeft: 10,
  },

  // ── Instruction card ────────────────────────────────────────────────
  instructionCard: {
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  instructionCardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 16,
  },
  stepItem: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  stepNumber: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#3B82F6',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  stepNumberText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFF',
  },
  stepContent: {
    flex: 1,
  },
  stepTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 4,
  },
  stepDesc: {
    fontSize: 13,
    color: '#64748B',
    lineHeight: 19,
  },

  // ── Angles card ─────────────────────────────────────────────────────
  anglesCard: {
    backgroundColor: '#FFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  anglesCardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 14,
  },
  anglesGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  angleGridItem: {
    width: '48%',
    alignItems: 'center',
    marginBottom: 14,
    padding: 10,
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
  },
  angleIconCircle: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#EFF6FF',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  angleGridLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 2,
  },
  angleGridDesc: {
    fontSize: 11,
    color: '#94A3B8',
    textAlign: 'center',
  },

  // ── Tips card ───────────────────────────────────────────────────────
  tipsCard: {
    backgroundColor: '#FFFBEB',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#FEF3C7',
  },
  tipsTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#92400E',
    marginBottom: 8,
  },
  tipsText: {
    fontSize: 13,
    color: '#78350F',
    lineHeight: 20,
  },

  // ── Warning card ────────────────────────────────────────────────────
  warningCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#FFF7ED',
    borderRadius: 12,
    padding: 14,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#FFEDD5',
  },
  warningText: {
    flex: 1,
    fontSize: 13,
    color: '#9A3412',
    marginLeft: 10,
    lineHeight: 19,
  },

  // ── Start button ────────────────────────────────────────────────────
  startBtn: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#3B82F6',
    borderRadius: 14,
    paddingVertical: 16,
    shadowColor: '#3B82F6',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  startBtnText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFF',
    marginLeft: 10,
  },

  // ── Camera ──────────────────────────────────────────────────────────
  cameraContainer: {
    flex: 1,
    backgroundColor: '#000',
  },
  cameraOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.15)',
  },
  cameraTopBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 16,
  },
  cameraCloseBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  recordingBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(239,68,68,0.85)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  recordingDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#FFF',
    marginRight: 6,
  },
  recordingText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#FFF',
  },
  faceGuideContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  faceGuideOval: {
    width: 220,
    height: 300,
    borderRadius: 110,
    borderWidth: 3,
    borderColor: 'rgba(255,255,255,0.7)',
    borderStyle: 'dashed',
  },
  cameraBottomSection: {
    paddingHorizontal: 20,
    paddingBottom: 40,
    alignItems: 'center',
  },
  cameraInstruction: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFF',
    textAlign: 'center',
    marginBottom: 16,
    textShadowColor: 'rgba(0,0,0,0.5)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 4,
  },
  angleRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: 20,
    flexWrap: 'wrap',
    gap: 12,
  },
  angleItem: {
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.35)',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 10,
  },
  angleLabel: {
    fontSize: 10,
    color: '#FFF',
    marginTop: 2,
  },
  recordBtnContainer: {
    alignItems: 'center',
  },
  recordBtnRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: '100%',
    paddingHorizontal: 20,
  },
  recordBtnSide: {
    width: 50,
    alignItems: 'center',
  },
  recordBtnCenter: {
    alignItems: 'center',
  },
  flipCameraBtn: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  recordBtn: {
    width: 72,
    height: 72,
    borderRadius: 36,
    borderWidth: 4,
    borderColor: '#FFF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  recordBtnInner: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: '#EF4444',
  },
  stopBtn: {
    width: 72,
    height: 72,
    borderRadius: 36,
    borderWidth: 4,
    borderColor: '#FFF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  stopBtnInner: {
    width: 30,
    height: 30,
    borderRadius: 4,
    backgroundColor: '#EF4444',
  },
  recordHint: {
    fontSize: 12,
    color: '#FFF',
    marginTop: 8,
    textShadowColor: 'rgba(0,0,0,0.5)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 3,
  },

  // ── Success ─────────────────────────────────────────────────────────
  successCircle: {
    marginBottom: 20,
  },
  successTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 12,
  },
  successSubtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 28,
  },
  primaryBtn: {
    backgroundColor: '#3B82F6',
    paddingVertical: 14,
    paddingHorizontal: 24,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryBtnText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFF',
  },

  // ── Processing ──────────────────────────────────────────────────────
  processingTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1E293B',
    marginTop: 20,
  },
  processingSubtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    marginTop: 8,
    lineHeight: 22,
  },
});

export default FaceRegistration;
