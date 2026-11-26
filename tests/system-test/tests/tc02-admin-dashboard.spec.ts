import { test, expect } from '@playwright/test';
import * as path from 'path';

const ADMIN_STATE_PATH = path.join(__dirname, '../.auth/admin.json');

test.describe('TC02 - Trang Admin Dashboard', () => {
  test.use({ storageState: ADMIN_STATE_PATH });

  // ─── TC02.1: Giao diện ───────────────────────────────────────────────────────
  test('TC02.1.1 - Kiểm tra giao diện hiển thị tổng quan của trang Admin Dashboard', async ({ page }) => {
    await page.goto('/admin/dashboard');

    await expect(page.locator('.page-title')).toBeVisible();
    await expect(page.locator('.stat-card')).toHaveCount(4);
    await expect(page.getByText('Hoạt động gần đây')).toBeVisible();
    await expect(page.getByText('Lối tắt quản trị')).toBeVisible();
    await expect(page.getByText('Danh sách chi nhánh')).toBeVisible();
  });

  test('TC02.1.2 - Kiểm tra hiển thị tiêu đề và lời chào cá nhân hóa', async ({ page }) => {
    await page.goto('/admin/dashboard');

    await expect(page.locator('.page-title')).toHaveText('Chào mừng trở lại, admin!');
    await expect(page.locator('.page-subtitle')).toHaveText(
      'Dưới đây là tóm tắt tình hình nhân sự của bạn ngày hôm nay.'
    );
  });

  // ─── TC02.2: Thẻ Tổng nhân viên ──────────────────────────────────────────────
  test('TC02.2.1 - Hiển thị số lượng Tổng nhân viên khi có dữ liệu', async ({ page }) => {
    await page.route('**/employees/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { total: 6 } }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Tổng nhân viên' });
    await expect(card).toBeVisible();
    await expect(card.locator('.stat-value')).toHaveText('6');
  });

  test('TC02.2.2 - Hiển thị giá trị mặc định khi chưa có dữ liệu Tổng nhân viên', async ({ page }) => {
    await page.route('**/employees/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Tổng nhân viên' });
    await expect(card.locator('.stat-value')).toHaveText('—');
  });

  // ─── TC02.3: Thẻ Có mặt hôm nay ──────────────────────────────────────────────
  test('TC02.3.1 - Hiển thị số lượng nhân viên có mặt hôm nay', async ({ page }) => {
    await page.route('**/attendances/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { active: 18, inactive: 2 } }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Có mặt hôm nay' });
    await expect(card).toBeVisible();
    await expect(card.locator('.stat-value')).toHaveText('18');
  });

  test('TC02.3.2 - Hiển thị giá trị mặc định khi không có dữ liệu điểm danh', async ({ page }) => {
    await page.route('**/attendances/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Có mặt hôm nay' });
    await expect(card.locator('.stat-value')).toHaveText('—');
  });

  // ─── TC02.4: Thẻ Đang nghỉ phép ──────────────────────────────────────────────
  test('TC02.4.1 - Hiển thị số lượng nhân viên đang nghỉ phép', async ({ page }) => {
    await page.route('**/leave-requests/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { active: 3 } }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Đang nghỉ phép' });
    await expect(card).toBeVisible();
    await expect(card.locator('.stat-value')).toHaveText('3');
  });

  test('TC02.4.2 - Hiển thị giá trị mặc định khi không có ai nghỉ', async ({ page }) => {
    await page.route('**/leave-requests/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Đang nghỉ phép' });
    await expect(card.locator('.stat-value')).toHaveText('—');
  });

  // ─── TC02.5: Thẻ Đi muộn/Về sớm ──────────────────────────────────────────────
  test('TC02.5.1 - Hiển thị số lượng nhân viên đi muộn hoặc về sớm', async ({ page }) => {
    await page.route('**/attendances/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { active: 18, inactive: 2 } }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Đi muộn/Về sớm' });
    await expect(card).toBeVisible();
    await expect(card.locator('.stat-value')).toHaveText('2');
  });

  test('TC02.5.2 - Hiển thị giá trị mặc định khi không có dữ liệu Đi muộn/Về sớm', async ({ page }) => {
    await page.route('**/attendances/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );
    await page.goto('/admin/dashboard');

    const card = page.locator('.stat-card', { hasText: 'Đi muộn/Về sớm' });
    await expect(card.locator('.stat-value')).toHaveText('—');
  });

  // ─── TC02.6: Danh sách hoạt động gần đây ─────────────────────────────────────
  test('TC02.6.1 - Hiển thị danh sách hoạt động khi có dữ liệu', async ({ page }) => {
    await page.route('**/leave-requests/pending*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'l1', employeeName: 'Nguyễn Văn An', totalDays: 2, createdAt: '2026-11-26T08:00:00Z' },
            { id: 'l2', employeeName: 'Trần Thị Bình', totalDays: 1, createdAt: '2026-11-25T09:00:00Z' },
          ],
        }),
      })
    );
    await page.route('**/employees?page=0&size=5', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'e1', fullName: 'Lê Minh Khôi', createdAt: '2026-11-26T07:30:00Z' },
            { id: 'e2', fullName: 'Phạm Thu Hà', createdAt: '2026-11-24T10:00:00Z' },
          ],
        }),
      })
    );

    await page.goto('/admin/dashboard');

    await expect(page.getByText('Nguyễn Văn An xin nghỉ phép 2 ngày')).toBeVisible();
    await expect(page.getByText('Trần Thị Bình xin nghỉ phép 1 ngày')).toBeVisible();
    await expect(page.getByText('Lê Minh Khôi đã gia nhập công ty')).toBeVisible();
    await expect(page.getByText('Phạm Thu Hà đã gia nhập công ty')).toBeVisible();
  });

  test('TC02.6.2 - Sắp xếp theo thứ tự thời gian và giới hạn hiển thị tối đa 4 hoạt động', async ({ page }) => {
    await page.route('**/leave-requests/pending*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'l1', employeeName: 'NV Cũ Nhất', totalDays: 1, createdAt: '2026-11-01T08:00:00Z' },
            { id: 'l2', employeeName: 'NV Mới Nhất', totalDays: 3, createdAt: '2026-11-26T10:00:00Z' },
            { id: 'l3', employeeName: 'NV Thứ Ba', totalDays: 2, createdAt: '2026-11-20T08:00:00Z' },
          ],
        }),
      })
    );
    await page.route('**/employees?page=0&size=5', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'e1', fullName: 'NV Mới Nhì', createdAt: '2026-11-25T08:00:00Z' },
            { id: 'e2', fullName: 'NV Thứ Tư', createdAt: '2026-11-18T08:00:00Z' },
            { id: 'e3', fullName: 'NV Bị Ẩn', createdAt: '2026-11-05T08:00:00Z' },
          ],
        }),
      })
    );

    await page.goto('/admin/dashboard');

    const activityCard = page.locator('.table-card', { hasText: 'Hoạt động gần đây' });
    const items = activityCard.locator('div[style*="gap: 12px"]');
    await expect(items).toHaveCount(4);
    await expect(items.first()).toContainText('NV Mới Nhất xin nghỉ phép 3 ngày');
    await expect(activityCard.getByText('NV Bị Ẩn')).toHaveCount(0);
  });

  test('TC02.6.3 - Hiển thị trạng thái trống khi không có hoạt động', async ({ page }) => {
    await page.route('**/leave-requests/pending*', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
    );
    await page.route('**/employees?page=0&size=5', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
    );

    await page.goto('/admin/dashboard');
    await expect(page.getByText('Chưa có hoạt động.')).toBeVisible();
  });

  test('TC02.6.4 - Nhấn nút "Xem tất cả" tại Hoạt động gần đây', async ({ page }) => {
    await page.goto('/admin/dashboard');
    const viewAllBtn = page.getByRole('button', { name: 'Xem tất cả' });
    await expect(viewAllBtn).toBeVisible();
    await viewAllBtn.click();
  });

  // ─── TC02.7: Lối tắt quản trị ────────────────────────────────────────────────
  test('TC02.7.1 - Điều hướng tới trang Quản lý Tài khoản', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.locator('.table-card', { hasText: 'Lối tắt quản trị' }).getByRole('link', { name: /Tài khoản/ }).click();
    await expect(page).toHaveURL(/.*\/admin\/accounts/);
  });

  test('TC02.7.2 - Điều hướng tới trang Quản lý Vai trò', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.locator('.table-card', { hasText: 'Lối tắt quản trị' }).getByRole('link', { name: /Vai trò/ }).click();
    await expect(page).toHaveURL(/.*\/admin\/roles/);
  });

  test('TC02.7.3 - Điều hướng tới trang Quản lý Quyền hạn', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.locator('.table-card', { hasText: 'Lối tắt quản trị' }).getByRole('link', { name: /Quyền hạn/ }).click();
    await expect(page).toHaveURL(/.*\/admin\/permissions/);
  });

  test('TC02.7.4 - Điều hướng tới trang Cấu hình Hệ thống', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.locator('.table-card', { hasText: 'Lối tắt quản trị' }).getByRole('link', { name: /Hệ thống/ }).click();
    await expect(page).toHaveURL(/.*\/admin\/settings/);
  });

  test('TC02.7.5 - Hiệu ứng rê chuột (Hover) trên các ô lối tắt', async ({ page }) => {
    await page.goto('/admin/dashboard');
    const shortcutLink = page.locator('.table-card', { hasText: 'Lối tắt quản trị' }).getByRole('link', { name: /Tài khoản/ });

    await shortcutLink.hover();
    const borderColor = await shortcutLink.evaluate((el) => el.style.borderColor);
    expect(borderColor).toBe('var(--border-accent)');
  });

  // ─── TC02.8: Danh sách chi nhánh ─────────────────────────────────────────────
  test('TC02.8.1 & TC02.8.2 & TC02.8.3 - Hiển thị bảng chi nhánh và badge Hoạt động / Tạm dừng', async ({ page }) => {
    await page.route('**/stores', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 's1', name: 'Chi nhánh Hà Nội', isActive: true, code: 'HN01', address: '123 Cầu Giấy', phone: '0241234567' },
            { id: 's2', name: 'Chi nhánh Đà Nẵng', isActive: false, code: 'DN01', address: '456 Hải Châu', phone: '0236765432' },
          ],
        }),
      })
    );

    await page.goto('/admin/dashboard');
    const storeCard = page.locator('.table-card', { hasText: 'Danh sách chi nhánh' });

    // Kiểm tra 5 cột
    for (const col of ['Chi nhánh', 'Trạng thái', 'Mã', 'Địa chỉ', 'Điện thoại']) {
      await expect(storeCard.locator('th', { hasText: col })).toBeVisible();
    }

    // Kiểm tra dòng Hoạt động (xanh lá) và Tạm dừng (xám)
    const activeRow = storeCard.locator('tbody tr', { hasText: 'Chi nhánh Hà Nội' });
    await expect(activeRow.locator('.badge-green')).toHaveText('Hoạt động');

    const inactiveRow = storeCard.locator('tbody tr', { hasText: 'Chi nhánh Đà Nẵng' });
    await expect(inactiveRow.locator('.badge-gray')).toHaveText('Tạm dừng');
  });

  test('TC02.8.4 - Hiển thị khi danh sách chi nhánh rỗng', async ({ page }) => {
    await page.route('**/stores', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
    );

    await page.goto('/admin/dashboard');
    const storeCard = page.locator('.table-card', { hasText: 'Danh sách chi nhánh' });
    await expect(storeCard.getByText('Chưa có dữ liệu.')).toBeVisible();
  });
});
