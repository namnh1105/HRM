'use client';

import React, { useMemo, useState } from 'react';
import { FileText, Pencil, Plus, X } from 'lucide-react';
import { useCreateContractMutation, useGetContractsQuery, useUpdateContractMutation, useGetContractStatsQuery } from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { FilterPills, SearchBox } from '@/components/ui/ToolbarControls';
import { useToast } from '@/hooks/useToast';
import ToastStack from '@/components/ToastStack';
import { usePermissions } from '@/hooks/usePermissions';

type ContractRow = {
    id: string;
    contractCode?: string;
    employeeName?: string;
    employee?: { id?: string; fullName?: string };
    contractType?: string;
    startDate?: string;
    endDate?: string;
    status?: string;
    baseSalary?: number;
    salaryCoefficient?: number;
    signingDate?: string;
    note?: string;
    attachmentUrl?: string;
    [key: string]: any;
};

const CONTRACT_TYPE_OPTIONS = [
    { value: 'PROBATION', label: 'Thử việc' },
    { value: 'DEFINITE_TERM', label: 'Xác định thời hạn' },
    { value: 'INDEFINITE_TERM', label: 'Không xác định thời hạn' },
    { value: 'SEASONAL', label: 'Thời vụ' },
    { value: 'PART_TIME', label: 'Bán thời gian' },
    { value: 'MONTHLY', label: 'Trả lương theo tháng' },
];

const CONTRACT_TYPE_LABELS = CONTRACT_TYPE_OPTIONS.reduce<Record<string, string>>((acc, cur) => {
    acc[cur.value] = cur.label;
    return acc;
}, {});

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('vi-VN');
}

function StatusBadge({ status }: { status?: string }) {
    const s = (status ?? '').toUpperCase();
    if (s === 'ACTIVE') return <span className="badge badge-green">Hiệu lực</span>;
    if (s === 'EXPIRED') return <span className="badge badge-red">Hết hạn</span>;
    if (s === 'TERMINATED') return <span className="badge badge-amber">Đã chấm dứt</span>;
    if (s === 'RENEWED') return <span className="badge badge-blue">Đã gia hạn</span>;
    if (s.includes('ACTIVE')) return <span className="badge badge-green">Hiệu lực</span>;
    if (s.includes('EXPIRED') || s.includes('ENDED')) return <span className="badge badge-red">Hết hạn</span>;
    if (s.includes('PENDING')) return <span className="badge badge-amber">Chờ duyệt</span>;
    return <span className="badge badge-gray">{status || '—'}</span>;
}

export default function ContractsPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState<'all' | 'active' | 'inactive'>('all');

    const { data, isLoading, isFetching, refetch } = useGetContractsQuery({ page, size: pageSize });
    
    // Fetch stats
    const { data: statsData } = useGetContractStatsQuery();
    const stats: any = (statsData as any)?.data;
    
    const contracts = (data as any)?.data ?? [];
    const meta = (data as any)?.pagination;

    const [formModal, setFormModal] = useState<{ open: boolean; contract?: ContractRow }>({ open: false });

    const filtered = useMemo(() => {
        let list = [...contracts];
        const q = search.toLowerCase();
        if (q) {
            list = list.filter(r => 
                r.contractCode?.toLowerCase().includes(q) || 
                r.code?.toLowerCase().includes(q) || 
                r.employeeName?.toLowerCase().includes(q)
            );
        }
        if (filter === 'active') list = list.filter(r => String(r.status || '').toUpperCase().includes('ACTIVE'));
        if (filter === 'inactive') list = list.filter(r => !String(r.status || '').toUpperCase().includes('ACTIVE'));
        return list;
    }, [contracts, search, filter]);

    return (
        <>
            <PageHeader
                title="Hợp đồng lao động"
                subtitle="Quản lý danh sách hợp đồng và trạng thái hiệu lực"
                actions={
                    hasPermission('CREATE_CONTRACT') && (
                        <button type="button" className="btn btn-primary" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Thêm hợp đồng
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Tổng hợp đồng', value: stats?.total ?? '—' },
                    { label: 'Đang hiệu lực', value: stats?.active ?? '—', tone: 'green' },
                    { label: 'Sắp hết hạn', value: stats?.inactive ?? '—', tone: 'amber' },
                    { label: 'Đã thanh lý', value: stats?.deleted ?? '—', tone: 'red' },
                ]}
            />

            <DataTable
                title="Danh sách hợp đồng"
                titleIcon={FileText}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <FilterPills
                            options={[
                                { id: 'all', label: 'Tất cả' },
                                { id: 'active', label: 'Hiệu lực' },
                                { id: 'inactive', label: 'Hết hạn/Khác' },
                            ]}
                            value={filter}
                            onChange={(v) => { setFilter(v); setPage(0); }}
                        />
                        <SearchBox
                            placeholder="Tìm mã HĐ, tên..."
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
                        itemLabel: 'hợp đồng',
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
                            <th>Loại</th>
                            <th>Từ ngày</th>
                            <th>Đến ngày</th>
                            <th>Trạng thái</th>
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
                                        <FileText size={40} className="empty-icon" />
                                        <p className="empty-text">Không tìm thấy hợp đồng nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.employeeName || row.employee?.fullName || '—'}</td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 13 }}>
                                        {CONTRACT_TYPE_LABELS[row.contractType || row.type || ''] || row.contractType || row.type}
                                    </td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.startDate)}</td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.endDate)}</td>
                                    <td><StatusBadge status={row.status} /></td>
                                    <td>
                                        {hasPermission('UPDATE_CONTRACT') && (
                                            <button className="btn btn-sm btn-primary" onClick={() => setFormModal({ open: true, contract: row })}>
                                                <Pencil size={14} /> Sửa
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </DataTable>

            {formModal.open && (
                <ContractFormModal 
                    initial={formModal.contract} 
                    onClose={() => setFormModal({ open: false })} 
                    onSaved={refetch}
                    pushToast={pushToast}
                />
            )}

            <ToastStack toasts={toasts} />
        </>
    );
}

function ContractFormModal({ initial, onClose, onSaved, pushToast }: any) {
    const [create] = useCreateContractMutation();
    const [update] = useUpdateContractMutation();
    const [loading, setLoading] = useState(false);

    const generateContractCode = () => {
        const uuid = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : String(Date.now());
        return `CTR-${uuid}`;
    };

    const [attachmentFile, setAttachmentFile] = useState<File | null>(null);
    
    const [formData, setFormData] = useState({
        contractCode: initial?.contractCode || initial?.code || generateContractCode(),
        employeeId: initial?.employeeId || initial?.employee?.id || '',
        contractType: initial?.contractType || initial?.type || 'PROBATION',
        startDate: initial?.startDate?.slice(0, 10) || new Date().toISOString().slice(0, 10),
        endDate: initial?.endDate?.slice(0, 10) || '',
        signingDate: initial?.signingDate?.slice(0, 10) || '',
        baseSalary: initial?.baseSalary ?? '',
        salaryCoefficient: initial?.salaryCoefficient ?? '',
        allowance: initial?.allowance ?? '',
        status: initial?.status || 'ACTIVE',
        note: initial?.note || '',
        attachmentUrl: initial?.attachmentUrl || '',
    });

    const handleSubmit = async () => {
        if (!initial && !formData.employeeId) return pushToast('Vui lòng điền đủ thông tin', 'error');
        if (!initial && !formData.baseSalary) return pushToast('Vui lòng nhập lương cơ bản', 'error');
        setLoading(true);
        try {
            if (initial?.id) {
                const updateBody = new FormData();
                if (formData.endDate) updateBody.append('endDate', formData.endDate);
                if (formData.baseSalary) updateBody.append('baseSalary', String(formData.baseSalary));
                if (formData.salaryCoefficient) updateBody.append('salaryCoefficient', String(formData.salaryCoefficient));
                if (formData.allowance) updateBody.append('allowance', String(formData.allowance));
                if (formData.status) updateBody.append('status', formData.status);
                if (formData.note) updateBody.append('note', formData.note);
                if (attachmentFile) updateBody.append('file', attachmentFile);
                await update({ id: initial.id, body: updateBody }).unwrap();
                pushToast('Cập nhật thành công');
            } else {
                const createBody = new FormData();
                createBody.append('contractCode', formData.contractCode || generateContractCode());
                createBody.append('employeeId', formData.employeeId);
                createBody.append('contractType', formData.contractType);
                createBody.append('startDate', formData.startDate);
                if (formData.endDate) createBody.append('endDate', formData.endDate);
                if (formData.signingDate) createBody.append('signingDate', formData.signingDate);
                createBody.append('baseSalary', String(formData.baseSalary));
                if (formData.salaryCoefficient) createBody.append('salaryCoefficient', String(formData.salaryCoefficient));
                if (formData.allowance) createBody.append('allowance', String(formData.allowance));
                if (formData.note) createBody.append('note', formData.note);
                if (attachmentFile) createBody.append('file', attachmentFile);
                await create(createBody).unwrap();
                pushToast('Thêm mới thành công');
            }
            onSaved();
            onClose();
        } catch {
            pushToast('Thao tác thất bại', 'error');
        } finally {
            setAttachmentFile(null);
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{initial ? 'Sửa hợp đồng' : 'Thêm hợp đồng'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body" style={{ overflowY: 'auto', maxHeight: '65vh', paddingRight: 4 }}>
                    {!initial && (
                        <div className="field-group">
                            <label className="field-label">ID Nhân viên (UUID)</label>
                            <input className="field-input" value={formData.employeeId} onChange={e => setFormData({...formData, employeeId: e.target.value})} placeholder="Nhập UUID nhân viên" />
                        </div>
                    )}
                    <div className="field-group">
                        <label className="field-label">Loại hợp đồng</label>
                        <select className="field-input" value={formData.contractType} onChange={e => setFormData({...formData, contractType: e.target.value})}>
                            {CONTRACT_TYPE_OPTIONS.map((opt) => (
                                <option key={opt.value} value={opt.value}>{opt.label}</option>
                            ))}
                        </select>
                    </div>
                    <div className="field-group">
                        <label className="field-label">Từ ngày</label>
                        <input className="field-input" type="date" value={formData.startDate} onChange={e => setFormData({...formData, startDate: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Đến ngày (Tùy chọn)</label>
                        <input className="field-input" type="date" value={formData.endDate} onChange={e => setFormData({...formData, endDate: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ngày ký (Tùy chọn)</label>
                        <input className="field-input" type="date" value={formData.signingDate} onChange={e => setFormData({...formData, signingDate: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Lương cơ bản</label>
                        <input className="field-input" type="number" value={formData.baseSalary} onChange={e => setFormData({...formData, baseSalary: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Hệ số lương</label>
                        <input className="field-input" type="number" value={formData.salaryCoefficient} onChange={e => setFormData({...formData, salaryCoefficient: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Phụ cấp</label>
                        <input className="field-input" type="number" value={formData.allowance} onChange={e => setFormData({...formData, allowance: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ghi chú</label>
                        <input className="field-input" value={formData.note} onChange={e => setFormData({...formData, note: e.target.value})} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Đính kèm tài liệu</label>
                        <input
                            className="field-input"
                            type="file"
                            onChange={(e) => {
                                const file = e.target.files?.[0] || null;
                                setAttachmentFile(file);
                            }}
                        />
                        {attachmentFile && (
                            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Đã chọn: {attachmentFile.name}</p>
                        )}
                    </div>
                    <div className="field-group">
                        <label className="field-label">Trạng thái</label>
                        <select className="field-input" value={formData.status} onChange={e => setFormData({...formData, status: e.target.value})}>
                            <option value="ACTIVE">Hiệu lực</option>
                            <option value="EXPIRED">Hết hạn</option>
                            <option value="TERMINATED">Đã chấm dứt</option>
                            <option value="RENEWED">Đã gia hạn</option>
                        </select>
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
