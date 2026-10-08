'use client';

import React, { useMemo, useState } from 'react';
import { Calendar, CheckCircle2, XCircle } from 'lucide-react';
import {
    useApproveLeaveRequestMutation,
    useGetLeaveRequestsQuery,
    useGetLeaveRequestStatsQuery,
} from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { FilterPills, SearchBox } from '@/components/ui/ToolbarControls';
import { useToast } from '@/hooks/useToast';
import ToastStack from '@/components/ToastStack';
import { usePermissions } from '@/hooks/usePermissions';

type LeaveRow = {
    id: string;
    employeeName?: string;
    type?: string;
    startDate?: string;
    endDate?: string;
    reason?: string;
    status?: string;
    [key: string]: any;
};

type ListMode = 'all' | 'PENDING' | 'APPROVED' | 'REJECTED';

function isPendingRow(row: LeaveRow): boolean {
    const s = String(row.status ?? '').toUpperCase();
    return s.includes('PENDING') || s.includes('CHỜ') || s === 'WAITING';
}

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('vi-VN');
}

function StatusBadge({ status }: { status?: string }) {
    const s = (status ?? '').toUpperCase();
    if (s.includes('PENDING') || s === 'WAITING') return <span className="badge badge-amber">Chờ duyệt</span>;
    if (s.includes('APPROVED')) return <span className="badge badge-green">Đã duyệt</span>;
    if (s.includes('REJECTED') || s.includes('CANCEL')) return <span className="badge badge-red">Từ chối / Hủy</span>;
    return <span className="badge badge-gray">{status || '—'}</span>;
}

export default function LeaveRequestsPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [listMode, setListMode] = useState<ListMode>('all');
    const [search, setSearch] = useState('');

    const { data, isLoading, isFetching } = useGetLeaveRequestsQuery({ 
        page, 
        size: pageSize, 
        status: listMode === 'all' ? undefined : listMode 
    });

    // Fetch stats
    const { data: statsData } = useGetLeaveRequestStatsQuery();
    const stats: any = (statsData as any)?.data;
    
    const rows = (data as any)?.data ?? [];
    const meta = (data as any)?.pagination;

    const [approve, { isLoading: isApproving }] = useApproveLeaveRequestMutation();

    const filtered = useMemo(() => {
        let list = [...rows];
        const q = search.toLowerCase();
        if (q) {
            list = list.filter(r => 
                r.employeeName?.toLowerCase().includes(q) || 
                r.reason?.toLowerCase().includes(q)
            );
        }
        return list;
    }, [rows, search]);

    return (
        <>
            <PageHeader
                title="Đơn nghỉ phép"
                subtitle="Duyệt đơn chờ xử lý và theo dõi lịch sử nghỉ phép"
            />

            <StatCards
                items={[
                    { label: 'Tổng đơn nghỉ', value: stats?.total ?? '—' },
                    { label: 'Đang chờ duyệt', value: stats?.active ?? '—', tone: 'amber' },
                    { label: 'Đã xử lý', value: stats?.inactive ?? '—', tone: 'green' },
                    { label: 'Đã xóa', value: stats?.deleted ?? '—', tone: 'red' },
                ]}
            />

            <DataTable
                title="Danh sách nghỉ phép"
                titleIcon={Calendar}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <FilterPills<ListMode>
                            options={[
                                { id: 'all', label: 'Tất cả' },
                                { id: 'PENDING', label: 'Chờ duyệt' },
                                { id: 'APPROVED', label: 'Đã duyệt' },
                                { id: 'REJECTED', label: 'Từ chối' },
                            ]}
                            value={listMode}
                            onChange={(v) => { setListMode(v); setPage(0); }}
                        />
                        <SearchBox
                            placeholder="Tìm tên, lý do..."
                            value={search}
                            onChange={(v) => { setSearch(v); setPage(0); }}
                        />
                    </>
                }
                pagination={
                    meta ? {
                        page,
                        totalPages: meta.totalPages,
                        totalItems: meta.totalItems,
                        itemLabel: 'đơn',
                        onPageChange: setPage,
                        pageSize,
                        onPageSizeChange: (s) => { setPageSize(s); setPage(0); }
                    } : null
                }
            >
                <table>
                    <thead>
                        <tr>
                            <th>Nhân viên</th>
                            <th>Loại nghỉ</th>
                            <th>Từ ngày</th>
                            <th>Đến ngày</th>
                            <th>Lý do</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array.from({ length: 5 }).map((_, i) => (
                                <tr key={i}>
                                    {Array.from({ length: 7 }).map((__, j) => (
                                        <td key={j}><div className="skeleton" style={{ height: 16, width: '80%' }} /></td>
                                    ))}
                                </tr>
                            ))
                        ) : filtered.length === 0 ? (
                            <tr>
                                <td colSpan={7}>
                                    <div className="empty-state">
                                        <Calendar size={40} className="empty-icon" />
                                        <p className="empty-text">Không tìm thấy đơn nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.employeeName || '—'}</td>
                                    <td><span className="badge badge-blue">{row.type || 'Nghỉ phép'}</span></td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.startDate)}</td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.endDate)}</td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 13, maxWidth: 200 }}>
                                        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: 'block', whiteSpace: 'nowrap' }}>
                                            {row.reason}
                                        </span>
                                    </td>
                                    <td><StatusBadge status={row.status} /></td>
                                    <td>
                                        {isPendingRow(row) && hasPermission('APPROVE_LEAVE_REQUEST') && (
                                            <div style={{ display: 'flex', gap: 6 }}>
                                                <button 
                                                    className="btn btn-sm btn-primary" 
                                                    onClick={() => approve({ id: row.id, body: { approved: true } }).unwrap().then(() => pushToast('Đã duyệt'))}
                                                    disabled={isApproving}
                                                >
                                                    <CheckCircle2 size={14} /> Duyệt
                                                </button>
                                                <button 
                                                    className="btn btn-sm btn-ghost" 
                                                    onClick={() => approve({ id: row.id, body: { approved: false } }).unwrap().then(() => pushToast('Đã từ chối'))}
                                                    disabled={isApproving}
                                                >
                                                    <XCircle size={14} /> Từ chối
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </DataTable>

            <ToastStack toasts={toasts} />
        </>
    );
}
