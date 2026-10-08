import { useState, useRef, useEffect } from 'react';
import { Alert, Animated } from 'react-native';

export const useForgotPassword = (navigation: any) => {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(30)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 500,
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

  const handleSubmit = () => {
    if (!email.trim()) {
      Alert.alert('Lỗi', 'Vui lòng nhập email hoặc số điện thoại.');
      return;
    }
    setIsSubmitted(true);
  };

  const goBack = () => navigation.goBack();

  return {
    email,
    setEmail,
    isSubmitted,
    fadeAnim,
    slideAnim,
    handleSubmit,
    goBack,
  };
};
