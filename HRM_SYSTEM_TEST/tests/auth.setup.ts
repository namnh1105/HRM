import { test as setup } from '@playwright/test';
import * as path from 'path';
import { loginAs } from '../helpers/auth.helper';

const adminFile = path.join(__dirname, '../.auth/admin.json');

setup('Đăng nhập Admin 1 lần để lưu session', async ({ page }) => {
  await loginAs(page, 'admin');
  await page.context().storageState({ path: adminFile });
});

