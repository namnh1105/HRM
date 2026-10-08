import Link from 'next/link';

export default function ForbiddenPage() {
    return (
        <div
            style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexDirection: 'column',
                gap: 12,
                background: '#f8fafc',
            }}
        >
            <h1 style={{ fontSize: 56, lineHeight: 1, color: '#dc2626' }}>403</h1>
            <p style={{ fontSize: 18, color: '#0f172a', fontWeight: 600 }}>Bạn không có quyền truy cập trang này.</p>
            <Link href="/login" style={{ color: '#2563eb', textDecoration: 'underline' }}>
                Quay lại đăng nhập
            </Link>
        </div>
    );
}
