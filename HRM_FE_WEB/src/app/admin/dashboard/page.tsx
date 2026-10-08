'use client';
import React from 'react';
import {
    Users,
    Calendar,
    Clock,
    CheckCircle2,
    ArrowUpRight,
    Activity,
    Shield,
    Key,
    Settings,
} from 'lucide-react';
import { useAppSelector } from '@/store/hooks';
import {
    useGetAttendanceStatsQuery,
    useGetEmployeeStatsQuery,
    useGetLeaveRequestStatsQuery,
    useGetPendingLeaveRequestsQuery,
    useGetEmployeesQuery,
    useGetStoresQuery,
} from '@/store/api/hrApi';

export default function DashboardPage() {
    const user = useAppSelector((s) => s.auth?.user);

    const { data: empStatsData } = useGetEmployeeStatsQuery();
    const { data: attStatsData } = useGetAttendanceStatsQuery();
    const { data: leaveStatsData } = useGetLeaveRequestStatsQuery();
    const { data: pendingLeaveData } = useGetPendingLeaveRequestsQuery({ page: 0, size: 5 });
    const { data: recentEmployeesData } = useGetEmployeesQuery({ page: 0, size: 5 });
    const { data: storesData } = useGetStoresQuery();

    const empStats: any = (empStatsData as any)?.data;
    const attStats: any = (attStatsData as any)?.data;
    const leaveStats: any = (leaveStatsData as any)?.data;

    const pendingLeaves: any[] = (pendingLeaveData as any)?.data?.content ?? (pendingLeaveData as any)?.data ?? [];
    const recentEmployees: any[] = (recentEmployeesData as any)?.data?.content ?? (recentEmployeesData as any)?.data ?? [];
    const stores: any[] = (storesData as any)?.data ?? [];

    const formatDateTime = (value?: string) => {
        if (!value) return '—';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '—';
        return date.toLocaleString('vi-VN');
    };

    const toTimestamp = (value?: string) => {
        if (!value) return 0;
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? 0 : date.getTime();
    };

    const stats = [
        { label: 'Tổng nhân viên', value: empStats?.total ?? '—', change: null, icon: Users, color: 'var(--accent-light)' },
        { label: 'Có mặt hôm nay', value: attStats?.active ?? '—', change: null, icon: CheckCircle2, color: 'var(--green)' },
        { label: 'Đang nghỉ phép', value: leaveStats?.active ?? '—', change: null, icon: Calendar, color: 'var(--amber)' },
        { label: 'Đi muộn/Về sớm', value: attStats?.inactive ?? '—', change: null, icon: Clock, color: 'var(--red)' },
    ];

    const activities = [
        ...pendingLeaves.map((item, index) => ({
            id: `leave-${item?.id ?? index}`,
            type: 'leave',
            text: `${item?.employeeName ?? 'Nhân viên'} xin nghỉ phép ${item?.totalDays ?? ''} ngày`
                .replace('  ', ' ').trim(),
            time: formatDateTime(item?.createdAt),
            createdAt: item?.createdAt,
            icon: Calendar,
            color: 'var(--amber)',
        })),
        ...recentEmployees.map((item, index) => ({
            id: `employee-${item?.id ?? index}`,
            type: 'new_user',
            text: `${item?.fullName ?? 'Nhân viên'} đã gia nhập công ty`,
            time: formatDateTime(item?.createdAt),
            createdAt: item?.createdAt,
            icon: Users,
            color: 'var(--accent)',
        })),
    ]
        .sort((a, b) => toTimestamp(b.createdAt) - toTimestamp(a.createdAt))
        .slice(0, 4);

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-title">Chào mừng trở lại, {user?.email?.split('@')[0] || 'Admin'}!</h1>
                    <p className="page-subtitle">Dưới đây là tóm tắt tình hình nhân sự của bạn ngày hôm nay.</p>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stat-grid">
                {stats.map((stat, i) => {
                    const Icon = stat.icon;
                    const isPositive = stat.change?.startsWith('+');
                    return (
                        <div key={i} className="stat-card">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                <div style={{ 
                                    width: 42, height: 42, borderRadius: 10, 
                                    background: 'var(--bg-elevated)', border: '1px solid var(--border)',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    color: stat.color, flexShrink: 0
                                }}>
                                    <Icon size={20} style={{ margin: 'auto' }} />
                                </div>
                                {stat.change ? (
                                    <span style={{ 
                                        fontSize: 11, fontWeight: 600, 
                                        color: isPositive ? 'var(--green)' : 'var(--red)',
                                        display: 'flex', alignItems: 'center', gap: 2
                                    }}>
                                        {stat.change}
                                        {isPositive ? <ArrowUpRight size={12} /> : <ArrowDownRight size={12} />}
                                    </span>
                                ) : (
                                    <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>—</span>
                                )}
                            </div>
                            <div style={{ marginTop: 12 }}>
                                <p className="stat-label">{stat.label}</p>
                                <p className="stat-value">{stat.value}</p>
                            </div>
                        </div>
                    );
                })}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 24 }}>
                {/* Recent Activity */}
                <div className="table-card" style={{ display: 'flex', flexDirection: 'column' }}>
                    <div className="table-toolbar">
                        <p className="table-title">Hoạt động gần đây</p>
                        <button className="btn btn-ghost btn-sm">Xem tất cả</button>
                    </div>
                    <div style={{ padding: '12px 20px 20px' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                            {activities.length === 0 ? (
                                <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Chưa có hoạt động.</p>
                            ) : activities.map((act) => {
                                const Icon = act.icon;
                                return (
                                    <div key={act.id} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                                        <div style={{ 
                                            width: 32, height: 32, borderRadius: 8, 
                                            background: `${act.color}15`, 
                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                            color: act.color, flexShrink: 0
                                        }}>
                                            <Icon size={16} style={{ margin: 'auto' }} />
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <p style={{ fontSize: 13, color: 'var(--text-primary)', fontWeight: 500 }}>{act.text}</p>
                                            <p style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>{act.time}</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>

                {/* Quick Shortcuts */}
                <div className="table-card" style={{ display: 'flex', flexDirection: 'column' }}>
                    <div className="table-toolbar">
                        <p className="table-title">Lối tắt quản trị</p>
                    </div>
                    <div style={{ padding: '20px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                        {[
                            { label: 'Tài khoản', sub: 'Quản lý user', icon: Users, href: '/admin/accounts' },
                            { label: 'Vai trò', sub: 'Phân quyền', icon: Shield, href: '/admin/roles' },
                            { label: 'Quyền hạn', sub: 'Chi tiết API', icon: Key, href: '/admin/permissions' },
                            { label: 'Hệ thống', sub: 'Cấu hình', icon: Settings, href: '/admin/settings' },
                        ].map((item, i) => {
                            const Icon = item.icon;
                            return (
                                <a key={i} href={item.href} style={{ 
                                    background: 'var(--bg-elevated)', border: '1px solid var(--border)',
                                    borderRadius: 12, padding: '16px', textDecoration: 'none',
                                    transition: 'all 0.2s', display: 'block'
                                }} onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--border-accent)';
                                    e.currentTarget.style.background = 'var(--bg-card-hover)';
                                }} onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--border)';
                                    e.currentTarget.style.background = 'var(--bg-elevated)';
                                }}>
                                    <Icon size={20} style={{ color: 'var(--accent-light)', marginBottom: 8 }} />
                                    <p style={{ fontSize: 14, fontWeight: 600, color: 'var(--text-primary)' }}>{item.label}</p>
                                    <p style={{ fontSize: 11, color: 'var(--text-muted)' }}>{item.sub}</p>
                                </a>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* Legend / Small Table */}
            <div className="table-card" style={{ marginTop: 24 }}>
                <div className="table-toolbar">
                    <p className="table-title">Danh sách chi nhánh</p>
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Chi nhánh</th>
                            <th>Trạng thái</th>
                            <th>Mã</th>
                            <th>Địa chỉ</th>
                            <th>Điện thoại</th>
                        </tr>
                    </thead>
                    <tbody>
                        {stores.length === 0 ? (
                            <tr>
                                <td colSpan={5} style={{ color: 'var(--text-muted)', padding: '12px 16px' }}>Chưa có dữ liệu.</td>
                            </tr>
                        ) : stores.map((row, i) => (
                            <tr key={row?.id ?? i}>
                                <td className="td-primary">{row?.name ?? '—'}</td>
                                <td>
                                    <span className={`badge ${row?.isActive ? 'badge-green' : 'badge-gray'}`}>
                                        {row?.isActive ? 'Hoạt động' : 'Tạm dừng'}
                                    </span>
                                </td>
                                <td>{row?.code ?? '—'}</td>
                                <td>{row?.address ?? '—'}</td>
                                <td>{row?.phone ?? '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </>
    );
}