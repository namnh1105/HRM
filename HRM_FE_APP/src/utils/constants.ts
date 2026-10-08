import Constants from 'expo-constants';

// App constants
export const APP_NAME = 'Scrolla FE';
export const VERSION = '1.0.0';

// API Configuration
export const API_BASE_URL = Constants.expoConfig?.extra?.apiBaseUrl || 'http://10.0.2.2:8080/api/v1';
// 10.0.2.2 = host machine from Android emulator. Use localhost for iOS or real device IP for physical device.

// Colors
export const COLORS = {
  PRIMARY: '#6B4CE6',
  PRIMARY_GRADIENT_START: '#2E7FD9',
  PRIMARY_GRADIENT_END: '#E74C3C',
  SECONDARY: '#FF6B35',
  SUCCESS: '#34C759',
  WARNING: '#FF9500',
  ERROR: '#FF3B30',
  BACKGROUND: '#FFFFFF',
  TEXT: '#000000',
  TEXT_SECONDARY: '#666666',
} as const;

// Layout
export const SPACING = {
  XS: 4,
  SM: 8,
  MD: 16,
  LG: 24,
  XL: 32,
} as const;