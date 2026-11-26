import { test, expect, Page } from '@playwright/test';
import * as path from 'path';

const ADMIN_STATE_PATH = path.join(__dirname, '../.auth/admin.json');

const MOCK_USERS = [
  {
    id: 'u1',
    email: 'admin@worksphere.com',
    familyName: 'Quản trị',
    givenName: 'Hệ thống',
    roles: ['ROLE_ADMIN'],
    isActive: true,
    isDeleted: false,
    createdAt: '2026-10-01T08:00:00Z',
  },
  {
    id: 'u2',
    email: 'hr1@worksphere.com',
    familyName: 'Nguyễn',
    givenName: 'Văn A',
    roles: ['ROLE_HR'],
    isActive: true,
    isDeleted: false,
    createdAt: '2026-10-05T08:00:00Z',
  },
  {
    id: 'u3',
    email: 'locked@worksphere.com',
    familyName: 'Trần',
    givenName: 'Bị Khóa',
    roles: [],
    isActive: false,
    isDeleted: false,
    createdAt: '2026-10-10T08:00:00Z',
  },
  {
    id: 'u4',
    email: 'deleted@worksphere.com',
    familyName: 'Lê',
    givenName: 'Đã Xóa',
    roles: [],
    isActive: false,
    isDeleted: true,
    createdAt: '2026-10-12T08:00:00Z',
  },
];

async function mockDefaultUsersApi(page: Page, users = MOCK_USERS, stats = { total: 15, active: 10, inactive: 3, deleted: 2 }) {
  await page.route('**/users/stats', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: stats }) })
  );
  await page.route('**/users?*', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: users,
        pagination: { page: 0, size: 10, totalItems: users.length, totalPages: 2 },
      }),
    })
  );
  await page.route('**/users/*/roles', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [] }) })
  );
}

test.describe('TC03 - Trang Tài khoản', () => {
  test.use({ storageState: ADMIN_STATE_PATH });

  // ─── TC03.1: Giao diện ───────────────────────────────────────────────────────
  test('TC03.1.1 - Kiểm tra giao diện hiển thị tổng quan của trang Quản lý tài khoản', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');

    await expect(page.locator('.page-title')).toHaveText('Quản lý tài khoản');
    await expect(page.locator('.page-subtitle')).toHaveText('Quản lý người dùng và vai trò hệ thống');
    await expect(page.locator('#add-user-btn')).toBeVisible();
    await expect(page.locator('.stat-card')).toHaveCount(4);
    await expect(page.getByPlaceholder('Tìm email, tên, role…')).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
  });

  // ─── TC03.2 - TC03.5: 4 Thẻ thống kê ─────────────────────────────────────────
  test('TC03.2.1 & TC03.3.1 & TC03.4.1 & TC03.5.1 - Hiển thị đúng 4 thẻ thống kê khi có dữ liệu', async ({ page }) => {
    await mockDefaultUsersApi(page, MOCK_USERS, { total: 15, active: 10, inactive: 3, deleted: 2 });
    await page.goto('/admin/accounts');

    await expect(page.locator('.stat-card', { hasText: 'Số lượng tài khoản' }).locator('.stat-value')).toHaveText('15');
    await expect(page.locator('.stat-card', { hasText: 'Đang hoạt động' }).locator('.stat-value')).toHaveText('10');
    await expect(page.locator('.stat-card', { hasText: 'Không hoạt động' }).locator('.stat-value')).toHaveText('3');
    await expect(page.locator('.stat-card', { hasText: 'Đã xóa' }).locator('.stat-value')).toHaveText('2');
  });

  test('TC03.2.2 & TC03.3.2 & TC03.4.2 & TC03.5.2 - Hiển thị giá trị mặc định khi API stats trả về null', async ({ page }) => {
    await page.route('**/users/stats', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );
    await page.route('**/users?*', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: [], pagination: { page: 0, size: 10, totalItems: 0, totalPages: 1 } }) })
    );
    await page.goto('/admin/accounts');

    await expect(page.locator('.stat-card', { hasText: 'Số lượng tài khoản' }).locator('.stat-value')).toHaveText(/^(0|—)$/);
    await expect(page.locator('.stat-card', { hasText: 'Đang hoạt động' }).locator('.stat-value')).toHaveText(/^(0|—)$/);
    await expect(page.locator('.stat-card', { hasText: 'Không hoạt động' }).locator('.stat-value')).toHaveText(/^(0|—)$/);
    await expect(page.locator('.stat-card', { hasText: 'Đã xóa' }).locator('.stat-value')).toHaveText(/^(0|—)$/);
  });

  // ─── TC03.6: Thêm tài khoản ──────────────────────────────────────────────────
  test('TC03.6.1 - Thêm tài khoản thành công với thông tin hợp lệ', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/auth/register', (route) =>
      route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify({ success: true, data: { id: 'u-new' } }) })
    );

    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');

    await page.fill('#create-user-email', 'tester10@worksphere.com');
    await page.fill('#create-user-family', 'Nguyễn');
    await page.fill('#create-user-given', 'Văn B');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    await expect(page.getByText('Đã tạo người dùng')).toBeVisible();
    await expect(page.locator('#create-user-email')).toHaveCount(0);
  });

  test('TC03.6.2 - Thêm tài khoản thất bại khi để trống Email', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');

    await page.fill('#create-user-family', 'Nguyễn');
    await page.fill('#create-user-given', 'Văn C');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    const isValid = await page.locator('#create-user-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(isValid).toBe(false);
  });

  test('TC03.6.3 - Thêm tài khoản thất bại khi Email sai định dạng', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');

    await page.fill('#create-user-email', 'testabc');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    const isValid = await page.locator('#create-user-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(isValid).toBe(false);
  });

  test('TC03.6.4 - Thêm tài khoản thất bại khi để trống Mật khẩu', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');

    await page.fill('#create-user-email', 'tester11@worksphere.com');
    await page.click('#create-user-submit');

    const isValid = await page.locator('#create-user-password').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(isValid).toBe(false);
  });

  test('TC03.6.5 - Đóng modal khi nhấn nút "Hủy" hoặc biểu tượng "X"', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');
    await expect(page.locator('#create-user-email')).toBeVisible();

    await page.getByRole('button', { name: 'Hủy' }).click();
    await expect(page.locator('#create-user-email')).toHaveCount(0);
  });

  test('TC03.6.6 - Thêm tài khoản thất bại khi Email đã tồn tại trong hệ thống', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/auth/register', (route) =>
      route.fulfill({ status: 409, contentType: 'application/json', body: JSON.stringify({ success: false, message: 'Email đã tồn tại' }) })
    );

    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');
    await page.fill('#create-user-email', 'admin@worksphere.com');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    await expect(page.getByText('Tạo thất bại')).toBeVisible();
  });

  test('TC03.6.7 - Thêm tài khoản thành công khi bỏ trống cả Họ và Tên', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/auth/register', (route) =>
      route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify({ success: true, data: { id: 'u-noname' } }) })
    );

    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');
    await page.fill('#create-user-email', 'user_notname@company.com');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    await expect(page.getByText('Đã tạo người dùng')).toBeVisible();
  });

  test('TC03.6.8 - Thêm tài khoản thất bại khi Email chỉ chứa khoảng trắng', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');
    await page.fill('#create-user-email', '   ');
    await page.fill('#create-user-password', 'Password123@');
    await page.click('#create-user-submit');

    const isValid = await page.locator('#create-user-email').evaluate((el: HTMLInputElement) => el.checkValidity());
    expect(isValid).toBe(false);
  });

  test('TC03.6.9 - Kiểm tra trạng thái nút bấm khi đang gửi yêu cầu tạo tài khoản', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/auth/register', async (route) => {
      await new Promise((r) => setTimeout(r, 800));
      await route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify({ success: true, data: {} }) });
    });

    await page.goto('/admin/accounts');
    await page.click('#add-user-btn');
    await page.fill('#create-user-email', 'loading_test@company.com');
    await page.fill('#create-user-password', 'Password123@');

    const submitBtn = page.locator('#create-user-submit');
    await submitBtn.click();

    await expect(submitBtn).toBeDisabled();
    await expect(submitBtn).toHaveText('Đang tạo…');
    await expect(page.getByText('Đã tạo người dùng')).toBeVisible();
  });

  // ─── TC03.7: Xem danh sách & Phân trang ──────────────────────────────────────
  test('TC03.7.1 - Hiển thị đầy đủ 6 cột trong danh sách', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');

    for (const col of ['Email', 'Nhân viên', 'Vai trò', 'Trạng thái', 'Ngày tạo', 'Hành động']) {
      await expect(page.locator('th', { hasText: col })).toBeVisible();
    }
  });

  test('TC03.7.2 & TC03.7.3 - Chuyển trang Next và thay đổi kích thước trang 20/trang', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');

    await page.locator('button[aria-label="Trang sau"]').click();
    await expect(page.locator('button[aria-label="Trang 2"]')).toHaveAttribute('aria-current', 'page');

    await page.locator('select[aria-label="Số bản ghi mỗi trang"]').selectOption('20');
    await expect(page.locator('select[aria-label="Số bản ghi mỗi trang"]')).toHaveValue('20');
  });

  // ─── TC03.8: Tìm kiếm & Lọc tài khoản ────────────────────────────────────────
  test('TC03.8.1 & TC03.8.6 - Tìm kiếm tài khoản theo từ khóa và xóa từ khóa quay lại danh sách', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/search*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [MOCK_USERS[0]],
          pagination: { page: 0, size: 10, totalItems: 1, totalPages: 1 },
        }),
      })
    );

    await page.goto('/admin/accounts');
    const searchInput = page.getByPlaceholder('Tìm email, tên, role…');
    await searchInput.fill('admin');

    await expect(page.locator('tbody tr')).toHaveCount(1);
    await expect(page.locator('tbody')).toContainText('admin@worksphere.com');

    // Xóa từ khóa tìm kiếm (TC03.8.6)
    await searchInput.fill('');
    await expect(page.locator('tbody tr')).toHaveCount(MOCK_USERS.length);
  });

  test('TC03.8.2 - Tìm kiếm với từ khóa không tồn tại', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/search*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, data: [], pagination: { page: 0, size: 10, totalItems: 0, totalPages: 1 } }),
      })
    );

    await page.goto('/admin/accounts');
    await page.getByPlaceholder('Tìm email, tên, role…').fill('xyz999@test');
    await expect(page.getByText('Không tìm thấy tài khoản nào')).toBeVisible();
  });

  test('TC03.8.3 & TC03.8.4 & TC03.8.5 - Lọc danh sách theo tab Hoạt động, Không hoạt động, Đã xóa', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.goto('/admin/accounts');

    await page.getByRole('button', { name: 'Hoạt động', exact: true }).click();
    await expect(page.locator('tbody tr')).toHaveCount(2);

    await page.getByRole('button', { name: 'Không hoạt động', exact: true }).click();
    await expect(page.locator('tbody tr')).toHaveCount(1);
    await expect(page.locator('tbody')).toContainText('locked@worksphere.com');

    await page.getByRole('button', { name: 'Đã xóa', exact: true }).click();
    await expect(page.locator('tbody tr')).toHaveCount(1);
    await expect(page.locator('tbody')).toContainText('deleted@worksphere.com');
  });

  // ─── TC03.9: Gán và Gỡ quyền ─────────────────────────────────────────────────
  test('TC03.9.1 & TC03.9.2 & TC03.9.3 & TC03.9.5 - Gán role, gỡ role, hiển thị "Chưa có" và đóng modal', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/roles/active', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            { id: 'r-hr', code: 'ROLE_HR', displayName: 'Nhân sự' },
            { id: 'r-mgr', code: 'ROLE_MANAGER', displayName: 'Quản lý' },
          ],
        }),
      })
    );
    await page.route('**/users/u3/roles/r-hr', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );

    await page.goto('/admin/accounts');

    // Tài khoản u3 chưa có role hiển thị "Chưa có" (TC03.9.3)
    const u3Row = page.locator('tbody tr', { hasText: 'locked@worksphere.com' });
    await expect(u3Row).toContainText('Chưa có');

    // Mở modal gán quyền
    await page.click('#manage-role-u3');
    await expect(page.getByText('Gán quyền — locked@worksphere.com')).toBeVisible();

    // Gán ROLE_HR (TC03.9.1)
    await page.click('#toggle-role-r-hr');
    await expect(page.getByText('Đã gán role')).toBeVisible();

    // Đóng modal (TC03.9.5)
    await page.getByRole('button', { name: 'Đóng' }).click();
    await expect(page.getByText('Gán quyền — locked@worksphere.com')).toHaveCount(0);
  });

  // ─── TC03.10 & TC03.11: Khóa & Mở khóa ──────────────────────────────────────
  test('TC03.10.1 & TC03.11.1 - Khóa tài khoản đang hoạt động & Mở khóa tài khoản bị khóa', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/u2/deactivate', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { ...MOCK_USERS[1], isActive: false } }) })
    );
    await page.route('**/users/u3/activate', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: { ...MOCK_USERS[2], isActive: true } }) })
    );

    await page.goto('/admin/accounts');

    await page.click('#toggle-user-u2');
    await expect(page.getByText('Đã khóa tài khoản')).toBeVisible();

    await page.click('#toggle-user-u3');
    await expect(page.getByText('Đã mở khóa tài khoản')).toBeVisible();
  });

  // ─── TC03.12: Xóa mềm ────────────────────────────────────────────────────────
  test('TC03.12.1 & TC03.12.2 & TC03.12.3 - Modal xác nhận xóa, hủy xóa và xác nhận xóa mềm', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/u2', (route) => {
      if (route.request().method() === 'DELETE') {
        return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) });
      }
      return route.continue();
    });

    await page.goto('/admin/accounts');

    // Mở modal và hủy xóa (TC03.12.1 & TC03.12.2)
    await page.click('#delete-u2');
    await expect(page.getByText('Bạn có chắc muốn xóa tài khoản "hr1@worksphere.com"? Thao tác này có thể khôi phục.')).toBeVisible();
    await page.getByRole('button', { name: 'Hủy' }).click();
    await expect(page.locator('#confirm-action-btn')).toHaveCount(0);

    // Xác nhận xóa mềm (TC03.12.3)
    await page.click('#delete-u2');
    await page.click('#confirm-action-btn');
    await expect(page.getByText('Đã xóa tài khoản')).toBeVisible();
  });

  // ─── TC03.13: Khôi phục tài khoản ────────────────────────────────────────────
  test('TC03.13.1 - Khôi phục tài khoản đã bị xóa thành công', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/u4/restore', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );

    await page.goto('/admin/accounts');
    await page.getByRole('button', { name: 'Đã xóa', exact: true }).click();
    await page.click('#restore-u4');

    await expect(page.getByText('Đã khôi phục tài khoản')).toBeVisible();
  });

  // ─── TC03.14: Xóa vĩnh viễn ──────────────────────────────────────────────────
  test('TC03.14.1 & TC03.14.2 & TC03.14.3 - Cảnh báo xóa vĩnh viễn, hủy và xác nhận xóa vĩnh viễn', async ({ page }) => {
    await mockDefaultUsersApi(page);
    await page.route('**/users/u4/permanent', (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ success: true, data: null }) })
    );

    await page.goto('/admin/accounts');
    await page.getByRole('button', { name: 'Đã xóa', exact: true }).click();

    // Mở modal và hủy (TC03.14.1 & TC03.14.3)
    await page.click('#perm-delete-u4');
    await expect(page.getByText('Tài khoản "deleted@worksphere.com" sẽ bị xóa VĨNH VIỄN và không thể khôi phục!')).toBeVisible();
    await page.getByRole('button', { name: 'Hủy' }).click();
    await expect(page.locator('#confirm-action-btn')).toHaveCount(0);

    // Xác nhận xóa vĩnh viễn (TC03.14.2)
    await page.click('#perm-delete-u4');
    await page.click('#confirm-action-btn');
    await expect(page.getByText('Đã xóa vĩnh viễn')).toBeVisible();
  });
});

