'use client';

import React, { useMemo, useState } from 'react';
import { Building2, Pencil, Plus, Trash2, X } from 'lucide-react';
import {
    useCreateStoreMutation,
    useDeleteStoreMutation,
    useGetStoresQuery,
    useUpdateStoreMutation,
    useSearchStoresQuery,
} from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { FilterPills, SearchBox } from '@/components/ui/ToolbarControls';
import { useToast } from '@/hooks/useToast';
import ToastStack from '@/components/ToastStack';
import { usePermissions } from '@/hooks/usePermissions';

type StoreRow = {
    id: string;
    name: string;
    code: string;
    address?: string;
    phone?: string;
    email?: string;
    isActive?: boolean | string;
    createdAt?: string;
    [key: string]: any;
};

function isActiveStore(row: StoreRow): boolean {
    const v = row.isActive;
    if (typeof v === 'boolean') return v;
    return String(v).toLowerCase() === 'true' || String(v).toLowerCase() === 'active';
}

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('vi-VN');
}

export default function StoresPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState<'all' | 'active' | 'inactive'>('all');

    const trimmedSearch = search.trim();
    const { data: searchData, isFetching: searchFetching } = useSearchStoresQuery(
        { keyword: trimmedSearch },
        { skip: !trimmedSearch }
    );
    const { data, isLoading, isFetching: allFetching, refetch } = useGetStoresQuery();
    const [createStore] = useCreateStoreMutation();
    const [updateStore] = useUpdateStoreMutation();
    const [deleteStore] = useDeleteStoreMutation();

    const stores: StoreRow[] = (trimmedSearch ? (searchData as any) : data)?.data ?? [];
    const isFetching = trimmedSearch ? searchFetching : allFetching;

    const stats = useMemo(() => {
        const total = stores.length;
        const active = stores.filter(isActiveStore).length;
        const inactive = total - active;
        return { total, active, inactive };
    }, [stores]);

    const filtered = useMemo(() => {
        let list = [...stores];
        if (filter === 'active') list = list.filter(isActiveStore);
        if (filter === 'inactive') list = list.filter(r => !isActiveStore(r));
        return list;
    }, [stores, filter]);

    const [formModal, setFormModal] = useState<{ open: boolean; store?: StoreRow }>({ open: false });
    const [confirmDelete, setConfirmDelete] = useState<StoreRow | null>(null);

    const handleDelete = async (row: StoreRow) => {
        try {
            await deleteStore(row.id).unwrap();
            pushToast('Đã xóa chi nhánh');
            refetch();
        } catch {
            pushToast('Xóa thất bại', 'error');
        }
    };

    const handleSubmit = async (formData: any, initial?: StoreRow) => {
        try {
            if (initial?.id) {
                await updateStore({ id: initial.id, body: formData }).unwrap();
                pushToast('Cập nhật thành công');
            } else {
                await createStore(formData).unwrap();
                pushToast('Thêm mới thành công');
            }
            refetch();
            setFormModal({ open: false });
        } catch {
            pushToast('Thao tác thất bại', 'error');
        }
    };

    return (
        <>
            <PageHeader
                title="Chi nhánh"
                subtitle="Quản lý danh sách chi nhánh và thông tin liên hệ"
                actions={
                    hasPermission('CREATE_STORE') && (
                        <button type="button" className="btn btn-primary" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Thêm chi nhánh
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Tổng chi nhánh', value: stats.total },
                    { label: 'Đang hoạt động', value: stats.active, tone: 'green' },
                    { label: 'Ngưng hoạt động', value: stats.inactive, tone: 'amber' },
                ]}
            />

            <DataTable
                title="Danh sách chi nhánh"
                titleIcon={Building2}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <FilterPills
                            options={[
                                { id: 'all', label: 'Tất cả' },
                                { id: 'active', label: 'Đang hoạt động' },
                                { id: 'inactive', label: 'Ngưng hoạt động' },
                            ]}
                            value={filter}
                            onChange={(v) => setFilter(v)}
                        />
                        <SearchBox
                            placeholder="Tìm tên, mã, địa chỉ..."
                            value={search}
                            onChange={setSearch}
                        />
                    </>
                }
                pagination={null}
            >
                <table>
                    <thead>
                        <tr>
                            <th>Tên chi nhánh</th>
                            <th>Mã</th>
                            <th>Địa chỉ</th>
                            <th>Liên hệ</th>
                            <th>Trạng thái</th>
                            <th>Ngày tạo</th>
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
                                        <Building2 size={40} className="empty-icon" />
                                        <p className="empty-text">Không tìm thấy chi nhánh nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.name}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.code}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.address || '—'}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>
                                        {row.phone || '—'}{row.email ? ` • ${row.email}` : ''}
                                    </td>
                                    <td>
                                        {isActiveStore(row)
                                            ? <span className="badge badge-green">Đang hoạt động</span>
                                            : <span className="badge badge-amber">Ngưng hoạt động</span>
                                        }
                                    </td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.createdAt)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 6 }}>
                                            {hasPermission('UPDATE_STORE') && (
                                                <button className="btn btn-sm btn-primary" onClick={() => setFormModal({ open: true, store: row })}>
                                                    <Pencil size={14} /> Sửa
                                                </button>
                                            )}
                                            {hasPermission('DELETE_STORE') && (
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
                <StoreFormModal
                    initial={formModal.store}
                    onClose={() => setFormModal({ open: false })}
                    onSubmit={handleSubmit}
                />
            )}

            {confirmDelete && (
                <div className="modal-overlay" onClick={() => setConfirmDelete(null)}>
                    <div className="modal" style={{ maxWidth: 420 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <p className="modal-title">Xác nhận xóa</p>
                            <button className="btn btn-icon btn-ghost" onClick={() => setConfirmDelete(null)}><X size={16} /></button>
                        </div>
                        <div className="modal-body">
                            <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                                Bạn có chắc muốn xóa chi nhánh <strong>{confirmDelete.name}</strong>?
                            </p>
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

function StoreFormModal({ initial, onClose, onSubmit }: any) {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        name: initial?.name || '',
        code: initial?.code || '',
        address: initial?.address || '',
        phone: initial?.phone || '',
        email: initial?.email || '',
        isActive: initial?.isActive ?? true,
    });

    const handleSubmit = async () => {
        if (!formData.name || (!initial && !formData.code)) return;
        setLoading(true);
        await onSubmit(formData, initial);
        setLoading(false);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{initial ? 'Sửa chi nhánh' : 'Thêm chi nhánh'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body">
                    <div className="field-group">
                        <label className="field-label">Tên chi nhánh</label>
                        <input className="field-input" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Mã chi nhánh</label>
                        <input className="field-input" value={formData.code} onChange={e => setFormData({ ...formData, code: e.target.value })} disabled={!!initial} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Địa chỉ</label>
                        <input className="field-input" value={formData.address} onChange={e => setFormData({ ...formData, address: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Số điện thoại</label>
                        <input className="field-input" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Email</label>
                        <input className="field-input" type="email" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                    </div>
                    {initial && (
                        <div className="field-group">
                            <label className="field-label">Trạng thái</label>
                            <select
                                className="field-input"
                                value={formData.isActive ? 'true' : 'false'}
                                onChange={e => setFormData({ ...formData, isActive: e.target.value === 'true' })}
                            >
                                <option value="true">Đang hoạt động</option>
                                <option value="false">Ngưng hoạt động</option>
                            </select>
                        </div>
                    )}
                </div>
                <div className="modal-footer">
                    <button className="btn btn-ghost" onClick={onClose}>Hủy</button>
                    <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>Lưu</button>
                </div>
            </div>
        </div>
    );
}
