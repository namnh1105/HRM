import { COLORS, SPACING, APP_NAME, VERSION } from '../../src/utils/constants';

describe('Constants', () => {
  describe('App Constants', () => {
    it('should have correct app name', () => {
      expect(APP_NAME).toBe('Scrolla FE');
    });

    it('should have version', () => {
      expect(VERSION).toBe('1.0.0');
      expect(typeof VERSION).toBe('string');
    });
  });

  describe('Colors', () => {
    it('should have all color constants defined', () => {
      expect(COLORS.PRIMARY).toBeDefined();
      expect(COLORS.SECONDARY).toBeDefined();
      expect(COLORS.SUCCESS).toBeDefined();
      expect(COLORS.WARNING).toBeDefined();
      expect(COLORS.ERROR).toBeDefined();
      expect(COLORS.BACKGROUND).toBeDefined();
      expect(COLORS.TEXT).toBeDefined();
      expect(COLORS.TEXT_SECONDARY).toBeDefined();
    });

    it('should have valid hex color format', () => {
      const hexColorRegex = /^#[0-9A-F]{6}$/i;
      
      expect(COLORS.PRIMARY).toMatch(hexColorRegex);
      expect(COLORS.SECONDARY).toMatch(hexColorRegex);
      expect(COLORS.SUCCESS).toMatch(hexColorRegex);
      expect(COLORS.WARNING).toMatch(hexColorRegex);
      expect(COLORS.ERROR).toMatch(hexColorRegex);
      expect(COLORS.BACKGROUND).toMatch(hexColorRegex);
      expect(COLORS.TEXT).toMatch(hexColorRegex);
      expect(COLORS.TEXT_SECONDARY).toMatch(hexColorRegex);
    });
  });

  describe('Spacing', () => {
    it('should have all spacing values defined', () => {
      expect(SPACING.XS).toBeDefined();
      expect(SPACING.SM).toBeDefined();
      expect(SPACING.MD).toBeDefined();
      expect(SPACING.LG).toBeDefined();
      expect(SPACING.XL).toBeDefined();
    });

    it('should have correct spacing values', () => {
      expect(SPACING.XS).toBe(4);
      expect(SPACING.SM).toBe(8);
      expect(SPACING.MD).toBe(16);
      expect(SPACING.LG).toBe(24);
      expect(SPACING.XL).toBe(32);
    });

    it('should have ascending spacing values', () => {
      expect(SPACING.XS).toBeLessThan(SPACING.SM);
      expect(SPACING.SM).toBeLessThan(SPACING.MD);
      expect(SPACING.MD).toBeLessThan(SPACING.LG);
      expect(SPACING.LG).toBeLessThan(SPACING.XL);
    });

    it('should be numbers', () => {
      expect(typeof SPACING.XS).toBe('number');
      expect(typeof SPACING.SM).toBe('number');
      expect(typeof SPACING.MD).toBe('number');
      expect(typeof SPACING.LG).toBe('number');
      expect(typeof SPACING.XL).toBe('number');
    });
  });
});
