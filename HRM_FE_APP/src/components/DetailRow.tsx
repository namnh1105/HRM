import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ViewStyle,
  TextStyle,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

type Variant = 'default' | 'success' | 'danger' | 'warning' | 'info' | 'muted';

const VARIANT_COLORS: Record<Variant, string> = {
  default: '#1E293B',
  success: '#10B981',
  danger: '#EF4444',
  warning: '#F59E0B',
  info: '#3B82F6',
  muted: '#94A3B8',
};

export interface DetailRowProps {
  /** Label text (left side) */
  label: string;

  /** Value to display – string is shown as-is, number can be auto-formatted */
  value: string | number;

  /** Quick color variant for the value text */
  variant?: Variant;

  /** Custom color for value – overrides variant */
  valueColor?: string;

  /** Custom color for label */
  labelColor?: string;

  /** Show +/- sign prefix for numeric values (default: false) */
  showSign?: boolean;

  /** Format number as VND currency (requires a formatter function) */
  formatNumber?: (n: number) => string;

  /** Optional icon name (Ionicons) on the left */
  icon?: string;

  /** Icon color (default: #94A3B8) */
  iconColor?: string;

  /** Icon size (default: 18) */
  iconSize?: number;

  /** Show bottom border (default: true) */
  showBorder?: boolean;

  /** Custom border color */
  borderColor?: string;

  /** Size preset */
  size?: 'sm' | 'md' | 'lg';

  /** Extra style for the row container */
  style?: ViewStyle;

  /** Extra style for the label */
  labelStyle?: TextStyle;

  /** Extra style for the value */
  valueStyle?: TextStyle;

  /** Optional suffix text after the value (e.g. "đ", "%") */
  suffix?: string;

  /** Optional prefix text before the value */
  prefix?: string;

  /** Right-side accessory (custom ReactNode) – replaces value text if provided */
  rightAccessory?: React.ReactNode;
}

const SIZE_MAP = {
  sm: { fontSize: 12, paddingVertical: 6, iconSize: 14 },
  md: { fontSize: 14, paddingVertical: 10, iconSize: 18 },
  lg: { fontSize: 16, paddingVertical: 14, iconSize: 22 },
};

const DetailRow: React.FC<DetailRowProps> = ({
  label,
  value,
  variant = 'default',
  valueColor,
  labelColor,
  showSign = false,
  formatNumber,
  icon,
  iconColor = '#94A3B8',
  iconSize,
  showBorder = true,
  borderColor = '#F1F5F9',
  size = 'md',
  style,
  labelStyle,
  valueStyle,
  suffix,
  prefix,
  rightAccessory,
}) => {
  const sizeConfig = SIZE_MAP[size];
  const resolvedIconSize = iconSize ?? sizeConfig.iconSize;
  const resolvedValueColor = valueColor ?? VARIANT_COLORS[variant];

  // Build display string
  const buildDisplayValue = (): string => {
    if (typeof value === 'string') {
      return `${prefix ?? ''}${value}${suffix ?? ''}`;
    }

    // Numeric value
    const num = value;
    let sign = '';
    if (showSign) {
      sign = num >= 0 ? '+' : '-';
    } else if (num < 0) {
      sign = '-';
    }

    const absValue = Math.abs(num);
    const formatted = formatNumber ? formatNumber(absValue) : absValue.toLocaleString('vi-VN');

    return `${prefix ?? ''}${sign}${formatted}${suffix ?? ''}`;
  };

  return (
    <View
      style={[
        styles.row,
        { paddingVertical: sizeConfig.paddingVertical },
        showBorder && { borderBottomWidth: 1, borderBottomColor: borderColor },
        style,
      ]}
    >
      {/* Left side: icon + label */}
      <View style={styles.leftSection}>
        {icon && (
          <Ionicons
            name={icon as any}
            size={resolvedIconSize}
            color={iconColor}
            style={styles.icon}
          />
        )}
        <Text
          style={[
            styles.label,
            { fontSize: sizeConfig.fontSize },
            labelColor ? { color: labelColor } : undefined,
            labelStyle,
          ]}
          numberOfLines={2}
        >
          {label}
        </Text>
      </View>

      {/* Right side: value or custom accessory */}
      {rightAccessory ?? (
        <Text
          style={[
            styles.value,
            { fontSize: sizeConfig.fontSize, color: resolvedValueColor },
            valueStyle,
          ]}
          numberOfLines={1}
        >
          {buildDisplayValue()}
        </Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  leftSection: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 12,
  },
  icon: {
    marginRight: 10,
  },
  label: {
    color: '#64748B',
    fontWeight: '500',
    flexShrink: 1,
  },
  value: {
    fontWeight: '600',
  },
});

export default DetailRow;
