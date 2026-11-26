import { Page, expect } from '@playwright/test';

export const TEST_ACCOUNTS = {
  admin: {
    email: process.env.ADMIN_EMAIL || 'admin@worksphere.com',
    password: process.env.ADMIN_PASSWORD || 'Password123@',
  },
  hr: {
    email: process.env.HR_EMAIL || 'hr1@worksphere.com',
    password: process.env.HR_PASSWORD || '123456',
  },
  employee: {
    email: process.env.EMP_EMAIL || 'employee@worksphere.com',
    password: process.env.EMP_PASSWORD || '123456',
  },
};

/**
 * Đăng nhập nhanh với tài khoản Admin hoặc HR để dùng chung cho các test case khác.
 */
export async function loginAs(page: Page, role: 'admin' | 'hr') {
  const account = TEST_ACCOUNTS[role];
  await page.goto('/login');
  await page.fill('#login-email', account.email);
  await page.fill('#login-password', account.password);
  await page.click('#login-submit');

  if (role === 'admin') {
    await expect(page).toHaveURL(/.*\/admin\/dashboard/);
  } else {
    await expect(page).toHaveURL(/.*\/hr\/dashboard/);
  }
}

/**
 * Đăng xuất tài khoản từ thanh Sidebar.
 */
export async function logout(page: Page) {
  await page.click('button.logout-btn');
  await expect(page).toHaveURL(/.*\/login/);
}
