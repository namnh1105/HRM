import { test as setup } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import { loginAs } from '../helpers/auth.helper';

const adminFile = path.join(__dirname, '../.auth/admin.json');

setup('Đăng nhập Admin 1 lần để lưu session', async ({ page }) => {
  // Nếu file session đã tồn tại và còn mới (< 30 phút), dùng lại để tránh bị backend rate-limit
  if (fs.existsSync(adminFile)) {
    const ageMs = Date.now() - fs.statSync(adminFile).mtimeMs;
    if (ageMs < 30 * 60 * 1000) {
      return;
    }
  }

  await loginAs(page, 'admin');
  await page.context().storageState({ path: adminFile });
});
