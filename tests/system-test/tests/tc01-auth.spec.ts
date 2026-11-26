import { test, expect } from '@playwright/test';
import { loginAs, logout, TEST_ACCOUNTS } from '../helpers/auth.helper';

test.describe('TC01 - Đăng nhập, đăng xuất tài khoản', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  // ─── TC01.1: Giao diện ───────────────────────────────────────────────────────
  test('TC01.1.1 - Kiểm tra giao diện chức năng Đăng nhập', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'GiaKhanh HRM' })).toBeVisible();
    await expect(page.getByText('Đăng nhập vào hệ thống quản trị')).toBeVisible();
    await expect(page.locator('#login-email')).toBeVisible();
    await expect(page.locator('#login-password')).toBeVisible();
    await expect(page.locator('#toggle-password')).toBeVisible();
    await expect(page.locator('#login-submit')).toBeVisible();
    await expect(page.locator('#login-submit')).toHaveText(/Đăng nhập/);
  });

  // ─── TC01.2: Đăng nhập tài khoản ─────────────────────────────────────────────
  test('TC01.2.1 - Đăng nhập thành công với tài khoản Admin', async ({ page }) => {
    await page.fill('#login-email', TEST_ACCOUNTS.admin.email);
    await page.fill('#login-password', TEST_ACCOUNTS.admin.password);
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/admin\/dashboard/);
    const token = await page.evaluate(() => localStorage.getItem('ws_access_token'));
    expect(token).toBeTruthy();
  });

  test('TC01.2.2 - Đăng nhập thành công với tài khoản HR', async ({ page }) => {
    await page.fill('#login-email', TEST_ACCOUNTS.hr.email);
    await page.fill('#login-password', TEST_ACCOUNTS.hr.password);
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/hr\/dashboard/);
    const token = await page.evaluate(() => localStorage.getItem('ws_access_token'));
    expect(token).toBeTruthy();
  });

  test('TC01.2.3 - Đăng nhập thất bại khi Email đúng nhưng sai Mật khẩu', async ({ page }) => {
    await page.fill('#login-email', TEST_ACCOUNTS.admin.email);
    await page.fill('#login-password', 'SaiMatKhau123');
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/login/);
    await expect(page.locator('.bg-red-50')).toBeVisible();
  });

  test('TC01.2.4 - Đăng nhập thất bại với Email và Mật khẩu đều không tồn tại', async ({ page }) => {
    await page.fill('#login-email', 'khongtontai@gmail.com');
    await page.fill('#login-password', 'adminabc');
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/login/);
    await expect(page.locator('.bg-red-50')).toBeVisible();
  });

  test('TC01.2.5 - Đăng nhập thất bại khi Email sai định dạng', async ({ page }) => {
    await page.fill('#login-email', 'admin');
    await page.fill('#login-password', 'adminabc');
    await page.click('#login-submit');

    const isValid = await page.locator('#login-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(isValid).toBe(false);
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('TC01.2.6 - Đăng nhập với Email có khoảng trắng ở đầu hoặc cuối', async ({ page }) => {
    await page.fill('#login-email', ` ${TEST_ACCOUNTS.admin.email} `);
    await page.fill('#login-password', TEST_ACCOUNTS.admin.password);
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/admin\/dashboard/);
  });

  test('TC01.2.7 - Đăng nhập thất bại khi để trống cả Email và Mật khẩu', async ({ page }) => {
    await page.click('#login-submit');

    const emailValid = await page.locator('#login-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(emailValid).toBe(false);
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('TC01.2.8 - Đăng nhập thất bại khi có Email nhưng để trống Mật khẩu', async ({ page }) => {
    await page.fill('#login-email', TEST_ACCOUNTS.hr.email);
    await page.click('#login-submit');

    const pwValid = await page.locator('#login-password').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(pwValid).toBe(false);
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('TC01.2.9 - Đăng nhập thất bại khi để trống Email và có Mật khẩu', async ({ page }) => {
    await page.fill('#login-password', TEST_ACCOUNTS.hr.password);
    await page.click('#login-submit');

    const emailValid = await page.locator('#login-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(emailValid).toBe(false);
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('TC01.2.10 - Đăng nhập với tài khoản nhân viên không có quyền quản trị', async ({ page }) => {
    await page.fill('#login-email', TEST_ACCOUNTS.employee.email);
    await page.fill('#login-password', TEST_ACCOUNTS.employee.password);
    await page.click('#login-submit');

    await expect(page).toHaveURL(/.*\/403/);
  });

  test('TC01.2.11 - Kiểm tra trạng thái nút bấm khi đang xử lý đăng nhập', async ({ page }) => {
    // Làm chậm API login để kịp kiểm tra trạng thái loading của nút bấm
    await page.route('**/auth/login', async (route) => {
      await new Promise((r) => setTimeout(r, 800));
      await route.continue();
    });

    await page.fill('#login-email', TEST_ACCOUNTS.admin.email);
    await page.fill('#login-password', TEST_ACCOUNTS.admin.password);

    const submitBtn = page.locator('#login-submit');
    await submitBtn.click();

    await expect(submitBtn).toBeDisabled();
    await expect(submitBtn).toHaveText('Đang đăng nhập…');
    await expect(page).toHaveURL(/.*\/admin\/dashboard/);
  });

  // ─── TC01.3: Ẩn mật khẩu ─────────────────────────────────────────────────────
  test('TC01.3.1 - Kiểm tra trạng thái mặc định của ô Mật khẩu', async ({ page }) => {
    const pwInput = page.locator('#login-password');
    await pwInput.fill('admin123');
    await expect(pwInput).toHaveAttribute('type', 'password');
  });

  test('TC01.3.2 - Hiện mật khẩu khi nhấn icon con mắt', async ({ page }) => {
    const pwInput = page.locator('#login-password');
    const toggleBtn = page.locator('#toggle-password');

    await pwInput.fill('admin123');
    await toggleBtn.click();
    await expect(pwInput).toHaveAttribute('type', 'text');
  });

  test('TC01.3.3 - Ẩn lại mật khẩu khi nhấn icon con mắt lần thứ hai', async ({ page }) => {
    const pwInput = page.locator('#login-password');
    const toggleBtn = page.locator('#toggle-password');

    await pwInput.fill('admin123');
    await toggleBtn.click();
    await expect(pwInput).toHaveAttribute('type', 'text');

    await toggleBtn.click();
    await expect(pwInput).toHaveAttribute('type', 'password');
  });

  // ─── TC01.4: Đăng xuất tài khoản ─────────────────────────────────────────────
  test('TC01.4.1 - Đăng xuất thành công', async ({ page }) => {
    await loginAs(page, 'admin');
    await logout(page);

    await expect(page).toHaveURL(/.*\/login/);
    const token = await page.evaluate(() => localStorage.getItem('ws_access_token'));
    expect(token).toBeNull();
  });

  test('TC01.4.2 - Ngăn chặn quay lại phiên làm việc sau khi đã đăng xuất', async ({ page }) => {
    await loginAs(page, 'admin');
    await page.goto('/admin/accounts');
    await expect(page).toHaveURL(/.*\/admin\/accounts/);
    await logout(page);

    await page.goBack();
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('TC01.4.3 - Ngăn chặn truy cập trực tiếp URL trang quản trị khi chưa đăng nhập', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await expect(page).toHaveURL(/.*\/login/);

    await page.goto('/hr/dashboard');
    await expect(page).toHaveURL(/.*\/login/);
  });
});
