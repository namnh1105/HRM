'use client';

import React, { useMemo, useState } from 'react';
import { Clock3, Pencil, Plus, Trash2, X } from 'lucide-react';
import {
    useCreateWorkShiftMutation,
    useDeleteWorkShiftMutation,
    useGetWorkShiftsQuery,
    useUpdateWorkShiftMutation,
    useGetWorkShiftStatsQuery,
} from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { FilterPills, SearchBox } from '@/components/ui/ToolbarControls';
import { useToast } from '@/hooks/useToast';
import ToastStack from '@/components/ToastStack';
import { usePermissions } from '@/hooks/usePermissions';

type WorkShiftRow = {
    id: string;
    name: string;
    startTime: string;
    endTime: string;
    enabled: boolean | string;
    createdAt?: string;
    [key: string]: any;
};

function isEnabled(r: WorkShiftRow): boolean {
    const v = r.isActive ?? r.enabled;
    if (typeof v === 'boolean') return v;
    return String(v).toLowerCase() === 'true' || String(v).toLowerCase() === 'enabled';
}

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('vi-VN');
}

export default function WorkShiftsPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState<'all' | 'enabled' | 'disabled'>('all');

    const { data: statsData, isLoading: statsLoading } = useGetWorkShiftStatsQuery();
    const stats = (statsData as any)?.data;

    const { data, isLoading, isFetching, refetch } = useGetWorkShiftsQuery({ page, size: pageSize });
    const [deleteShift, { isLoading: isDeleting }] = useDeleteWorkShiftMutation();

    const shifts = (data as any)?.data ?? [];
    const meta = (data as any)?.pagination;

    const [formModal, setFormModal] = useState<{ open: boolean; shift?: WorkShiftRow }>({ open: false });
    const [confirmDelete, setConfirmDelete] = useState<WorkShiftRow | null>(null);

    const filtered = useMemo(() => {
        let list = [...shifts];
        const q = search.toLowerCase();
        if (q) {
            list = list.filter(r => r.name?.toLowerCase().includes(q));
        }
        if (filter === 'enabled') list = list.filter(isEnabled);
        if (filter === 'disabled') list = list.filter(r => !isEnabled(r));
        return list;
    }, [shifts, search, filter]);

    const handleDelete = async (row: WorkShiftRow) => {
        try {
            await deleteShift(row.id).unwrap();
            pushToast('Đã xóa ca làm');
            refetch();
        } catch {
            pushToast('Xóa thất bại', 'error');
        }
    };

    return (
        <>
            <PageHeader
                title="Ca làm việc"
                subtitle="Cấu hình danh sách ca làm và giờ hành chính"
                actions={
                    hasPermission('CREATE_WORK_SHIFT') && (
                        <button type="button" className="btn btn-primary" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Thêm ca
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Tổng số ca', value: statsLoading ? '—' : (stats?.total ?? 0) },
                    { label: 'Đang hoạt động', value: statsLoading ? '—' : (stats?.active ?? 0), tone: 'green' },
                    { label: 'Ngưng hoạt động', value: statsLoading ? '—' : (stats?.inactive ?? 0), tone: 'amber' },
                    { label: 'Đã xóa', value: statsLoading ? '—' : (stats?.deleted ?? 0), tone: 'red' },
                ]}
            />

            <DataTable
                title="Danh sách ca làm"
                titleIcon={Clock3}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <FilterPills
                            options={[
                                { id: 'all', label: 'Tất cả' },
                                { id: 'enabled', label: 'Đang bật' },
                                { id: 'disabled', label: 'Đang tắt' },
                            ]}
                            value={filter}
                            onChange={(v) => { setFilter(v); setPage(0); }}
                        />
                        <SearchBox
                            placeholder="Tìm tên ca..."
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
                        itemLabel: 'ca làm',
                        onPageChange: setPage,
                        pageSize,
                        onPageSizeChange: (s) => { setPageSize(s); setPage(0); }
                    } : null
                }
            >
                <table>
                    <thead>
                        <tr>
                            <th>Tên ca</th>
                            <th>Giờ vào</th>
                            <th>Giờ ra</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array.from({ length: 5 }).map((_, i) => (
                                <tr key={i}>
                                    {Array.from({ length: 6 }).map((__, j) => (
                                        <td key={j}><div className="skeleton" style={{ height: 16, width: '80%' }} /></td>
                                    ))}
                                </tr>
                            ))
                        ) : filtered.length === 0 ? (
                            <tr>
                                <td colSpan={6}>
                                    <div className="empty-state">
                                        <Clock3 size={40} className="empty-icon" />
                                        <p className="empty-text">Không tìm thấy ca làm nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.name}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.startTime}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.endTime}</td>
                                    <td>
                                        {isEnabled(row)
                                            ? <span className="badge badge-green">Đang bật</span>
                                            : <span className="badge badge-amber">Đang tắt</span>
                                        }
                                    </td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.createdAt)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 6 }}>
                                            {hasPermission('UPDATE_WORK_SHIFT') && (
                                                <button className="btn btn-sm btn-primary" onClick={() => setFormModal({ open: true, shift: row })}>
                                                    <Pencil size={14} /> Sửa
                                                </button>
                                            )}
                                            {hasPermission('DELETE_WORK_SHIFT') && (
                                                <button className="btn btn-sm btn-danger" onClick={() => setConfirmDelete(row)}>
                                                    <Trash2 size={14} /> Xóa
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </DataTable>

            {formModal.open && (
                <WorkShiftFormModal 
                    initial={formModal.shift} 
                    onClose={() => setFormModal({ open: false })} 
                    onSaved={refetch}
                    pushToast={pushToast}
                />
            )}

            {confirmDelete && (
                <div className="modal-overlay" onClick={() => setConfirmDelete(null)}>
                    <div className="modal" style={{ maxWidth: 400 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <p className="modal-title">Xác nhận xóa</p>
                            <button className="btn btn-icon btn-ghost" onClick={() => setConfirmDelete(null)}><X size={16} /></button>
                        </div>
                        <div className="modal-body">
                            <p style={{fontSize: 13, color: 'var(--text-muted)'}}>Bạn có chắc muốn xóa ca làm <strong>{confirmDelete.name}</strong>?</p>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-ghost" onClick={() => setConfirmDelete(null)}>Hủy</button>
                            <button className="btn btn-danger" onClick={() => { handleDelete(confirmDelete); setConfirmDelete(null); }}>Xóa</button>
                        </div>
                    </div>
                </div>
            )}

            <ToastStack toasts={toasts} />
        </>
    );
}

function WorkShiftFormModal({ initial, onClose, onSaved, pushToast }: any) {
    const [create] = useCreateWorkShiftMutation();
    const [update] = useUpdateWorkShiftMutation();
    const [loading, setLoading] = useState(false);
    
    const [formData, setFormData] = useState({
        name: initial?.name || '',
        code: initial?.code || '',
        startTime: initial?.startTime || '08:00',
        endTime: initial?.endTime || '17:00',
        breakDuration: initial?.breakDuration ?? 1.0,
        description: initial?.description || '',
        isActive: initial ? (initial.isActive ?? true) : true,
        isNightShift: initial?.isNightShift ?? false,
    });

    const handleSubmit = async () => {
        if (!formData.name) return pushToast('Vui lòng nhập tên ca', 'error');
        if (!initial?.id && !formData.code) return pushToast('Vui lòng nhập mã ca', 'error');
        setLoading(true);
        try {
            if (initial?.id) {
                await update({ id: initial.id, body: formData }).unwrap();
                pushToast('Cập nhật thành công');
            } else {
                await create(formData).unwrap();
                pushToast('Thêm mới thành công');
            }
            onSaved();
            onClose();
        } catch {
            pushToast('Thao tác thất bại', 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{initial ? 'Sửa ca làm' : 'Thêm ca làm'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body">
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                        <div className="field-group">
                            <label className="field-label">Tên ca</label>
                            <input className="field-input" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
                        </div>
                        {!initial?.id && (
                            <div className="field-group">
                                <label className="field-label">Mã ca (Cố định)</label>
                                <input className="field-input" value={formData.code} onChange={e => setFormData({...formData, code: e.target.value.toUpperCase()})} />
                            </div>
                        )}
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                        <div className="field-group">
                            <label className="field-label">Giờ vào</label>
                            <input className="field-input" type="time" value={formData.startTime} onChange={e => setFormData({...formData, startTime: e.target.value})} />
                        </div>
                        <div className="field-group">
                            <label className="field-label">Giờ ra</label>
                            <input className="field-input" type="time" value={formData.endTime} onChange={e => setFormData({...formData, endTime: e.target.value})} />
                        </div>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                        <div className="field-group">
                            <label className="field-label">Nghỉ giữa ca (giờ)</label>
                            <input className="field-input" type="number" step="0.5" value={formData.breakDuration} onChange={e => setFormData({...formData, breakDuration: parseFloat(e.target.value)})} />
                        </div>
                        <div className="field-group" style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 20, paddingTop: 28 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <input id="ws-active" type="checkbox" checked={formData.isActive} onChange={e => setFormData({...formData, isActive: e.target.checked})} />
                                <label htmlFor="ws-active" className="field-label" style={{ margin: 0 }}>Kích hoạt</label>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <input id="ws-night" type="checkbox" checked={formData.isNightShift} onChange={e => setFormData({...formData, isNightShift: e.target.checked})} />
                                <label htmlFor="ws-night" className="field-label" style={{ margin: 0 }}>Ca đêm</label>
                            </div>
                        </div>
                    </div>
                    <div className="field-group">
                        <label className="field-label">Mô tả</label>
                        <textarea className="field-input" rows={2} value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} />
                    </div>
                </div>
                <div className="modal-footer">
                    <button className="btn btn-ghost" onClick={onClose}>Hủy</button>
                    <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>Lưu</button>
                </div>
            </div>
        </div>
    );
}
