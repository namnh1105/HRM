'use client';
import React, { useState, useMemo } from 'react';
import {
    Key, Plus, Trash2, Pencil,
    X, ChevronRight, Lock, Unlock, RotateCcw,
} from 'lucide-react';
import {
    useGetAllPermissionsQuery,
    useCreatePermissionMutation,
    useUpdatePermissionMutation,
    useDeletePermissionMutation,
    useActivatePermissionMutation,
    useDeactivatePermissionMutation,
    useRestorePermissionMutation,
    useGetPermissionStatsQuery,
} from '@/store/api/permissionApi';
import type { PermissionResponse, CreatePermissionRequest, UpdatePermissionRequest } from '@/types';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { SearchBox } from '@/components/ui/ToolbarControls';
import EntityStatusFilters, { type EntityFilterStatus } from '@/components/ui/EntityStatusFilters';
import ToastStack from '@/components/ToastStack';
import { useToast } from '@/hooks/useToast';
import { usePermissions } from '@/hooks/usePermissions';

const RESOURCES = ['USER', 'ROLE', 'PERMISSION', 'EMPLOYEE', 'ATTENDANCE', 'WORK_SHIFT',
    'LEAVE_REQUEST', 'PAYROLL', 'CONTRACT', 'DEPARTMENT', 'PROFILE', 'STORE', 'AUDIT_LOG',
    'DEGREE', 'INSURANCE', 'RELATIVE', 'USER_ROLE', 'SYSTEM'];
const ACTIONS = ['VIEW', 'CREATE', 'UPDATE', 'DELETE', 'MANAGE', 'APPROVE', 'RESTORE',
    'PERMANENT_DELETE', 'ASSIGN', 'REVOKE', 'READ', 'SUPER_ADMIN', 'ADMIN'];

function PermFormModal({ initial, onClose, pushToast }: {
    initial?: PermissionResponse; onClose: () => void;
    pushToast: (m: string, t?: 'success' | 'error') => void;
}) {
    const blank = { code: '', displayName: '', description: '', resource: 'USER', action: 'VIEW', isActive: true };
    const [form, setForm] = useState<CreatePermissionRequest>(
        initial ? {
            code: initial.code, displayName: initial.displayName,
            description: initial.description ?? '', resource: initial.resource,
            action: initial.action, isActive: initial.isActive
        } : blank
    );
    const [create, { isLoading: c }] = useCreatePermissionMutation();
    const [update, { isLoading: u }] = useUpdatePermissionMutation();
    const busy = c || u;

    const set = <K extends keyof CreatePermissionRequest>(k: K, v: CreatePermissionRequest[K]) =>
        setForm((p) => ({ ...p, [k]: v }));

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            if (initial) { await update({ permissionId: initial.id, body: form as UpdatePermissionRequest }).unwrap(); pushToast('Đã cập nhật'); }
            else { await create(form).unwrap(); pushToast('Đã tạo quyền mới'); }
            onClose();
        } catch { pushToast('Thao tác thất bại', 'error'); }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{initial ? 'Cập nhật Quyền' : 'Tạo Quyền mới'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="field-group">
                            <label className="field-label">Mã code *</label>
                            <input id="perm-code" className="field-input" placeholder="VIEW_USER"
                                value={form.code} onChange={e => set('code', e.target.value.toUpperCase())} required />
                        </div>
                        <div className="field-group">
                            <label className="field-label">Tên hiển thị *</label>
                            <input id="perm-name" className="field-input" placeholder="Xem danh sách người dùng"
                                value={form.displayName} onChange={e => set('displayName', e.target.value)} required />
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                            <div className="field-group">
                                <label className="field-label">Resource *</label>
                                <select id="perm-resource" className="field-input" value={form.resource}
                                    onChange={e => set('resource', e.target.value)}>
                                    {RESOURCES.map(r => <option key={r}>{r}</option>)}
                                </select>
                            </div>
                            <div className="field-group">
                                <label className="field-label">Action *</label>
                                <select id="perm-action" className="field-input" value={form.action}
                                    onChange={e => set('action', e.target.value)}>
                                    {ACTIONS.map(a => <option key={a}>{a}</option>)}
                                </select>
                            </div>
                        </div>
                        <div className="field-group">
                            <label className="field-label">Mô tả</label>
                            <input id="perm-desc" className="field-input" placeholder="Mô tả ngắn..."
                                value={form.description} onChange={e => set('description', e.target.value)} />
                        </div>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', fontSize: 13 }}>
                            <input type="checkbox" checked={form.isActive}
                                onChange={e => set('isActive', e.target.checked)}
                                style={{ accentColor: 'var(--accent)', width: 16, height: 16 }} />
                            <span style={{ color: 'var(--text-secondary)' }}>Kích hoạt ngay</span>
                        </label>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-ghost" onClick={onClose}>Hủy</button>
                        <button type="submit" id="perm-submit" className="btn btn-primary" disabled={busy}>
                            {busy ? 'Đang lưu…' : initial ? 'Cập nhật' : 'Tạo mới'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default function PermissionsPage() {
    const { hasPermission } = usePermissions();
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState('');
    const [filterStatus, setFilterStatus] = useState<EntityFilterStatus>('all');
    const [filterResource, setFilterResource] = useState('all');

    const { data, isLoading, isFetching } = useGetAllPermissionsQuery({
        page,
        size: 100,
        includeDeleted: filterStatus === 'all' || filterStatus === 'deleted'
    });

    const [deletePermission] = useDeletePermissionMutation();
    const [activatePermission] = useActivatePermissionMutation();
    const [deactivatePermission] = useDeactivatePermissionMutation();
    const [restorePermission] = useRestorePermissionMutation();

    const [formModal, setFormModal] = useState<{ open: boolean; perm?: PermissionResponse }>({ open: false });
    const { toasts, push: pushToast } = useToast();

    // Fetch stats
    const { data: statsData } = useGetPermissionStatsQuery();
    const stats = statsData?.data;

    const perms = data?.data ?? [];
    const meta = data?.pagination;

    const resources = useMemo(() => ['all', ...Array.from(new Set(perms.map(p => p.resource))).sort()], [perms]);

    const filtered = useMemo(() => {
        let list = [...perms];

        if (filterResource !== 'all') list = list.filter(p => p.resource === filterResource);

        if (search.trim()) {
            const q = search.toLowerCase();
            list = list.filter(p =>
                p.code.toLowerCase().includes(q) ||
                p.displayName.toLowerCase().includes(q) ||
                p.resource.toLowerCase().includes(q) ||
                p.action.toLowerCase().includes(q)
            );
        }

        if (filterStatus === 'active') list = list.filter(p => p.isActive && !p.isDeleted);
        if (filterStatus === 'inactive') list = list.filter(p => !p.isActive && !p.isDeleted);
        if (filterStatus === 'deleted') list = list.filter(p => p.isDeleted);

        return list;
    }, [perms, search, filterResource, filterStatus]);

    const handleRestore = async (p: PermissionResponse) => {
        try {
            await restorePermission(p.id).unwrap();
            pushToast('Đã khôi phục');
        } catch { pushToast('Thất bại', 'error'); }
    };

    // Local state for expanded/collapsed resources
    const [expandedResources, setExpandedResources] = useState<Set<string>>(new Set());

    const toggleResource = (resource: string) => {
        setExpandedResources(prev => {
            const next = new Set(prev);
            if (next.has(resource)) next.delete(resource);
            else next.add(resource);
            return next;
        });
    };

    const grouped = useMemo(() => {
        const map: Record<string, PermissionResponse[]> = {};
        filtered.forEach(p => { if (!map[p.resource]) map[p.resource] = []; map[p.resource].push(p); });
        return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
    }, [filtered]);

    // Auto expand first group
    React.useEffect(() => {
        if (!search && grouped.length > 0 && expandedResources.size === 0) {
            setExpandedResources(new Set([grouped[0][0]]));
        }
    }, [grouped, search]);

    const handleDelete = async (p: PermissionResponse) => {
        if (!confirm(`Xóa quyền "${p.code}"?`)) return;
        try { await deletePermission(p.id).unwrap(); pushToast('Đã xóa'); }
        catch { pushToast('Xóa thất bại', 'error'); }
    };

    const handleToggle = async (p: PermissionResponse) => {
        try {
            if (p.isActive) { await deactivatePermission(p.id).unwrap(); pushToast('Đã khóa'); }
            else { await activatePermission(p.id).unwrap(); pushToast('Đã mở khóa'); }
        } catch { pushToast('Thao tác thất bại', 'error'); }
    };

    return (
        <>
            <PageHeader
                title="Quản lý quyền hạn"
                subtitle="Cấu hình các quyền hệ thống, nhóm theo tài nguyên"
                actions={
                    hasPermission('MANAGE_PERMISSIONS') && (
                        <button type="button" className="btn btn-primary" id="create-perm-btn" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Tạo Quyền mới
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Tổng quyền', value: stats?.total ?? '—', sub: 'Tất cả permissions' },
                    { label: 'Đang hoạt động', value: stats?.active ?? '—', tone: 'green', sub: 'Active' },
                    { label: 'Ngừng hoạt động', value: stats?.inactive ?? '—', tone: 'amber', sub: 'Inactive' },
                    { label: 'Đã xóa', value: stats?.deleted ?? '—', tone: 'red', sub: 'Deleted' },
                ]}
            />

            <DataTable
                title="Danh sách Quyền"
                titleIcon={Key}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <EntityStatusFilters
                            value={filterStatus}
                            onChange={(v) => {
                                setFilterStatus(v);
                                setPage(0);
                            }}
                        />
                        <select
                            id="filter-resource"
                            className="field-input"
                            style={{ width: 'auto', padding: '6px 10px', fontSize: 12 }}
                            value={filterResource}
                            onChange={(e) => {
                                setFilterResource(e.target.value);
                                setPage(0);
                            }}
                        >
                            {resources.map((r) => (
                                <option key={r} value={r}>
                                    {r === 'all' ? 'Tất cả Resource' : r}
                                </option>
                            ))}
                        </select>
                        <SearchBox
                            id="perms-search"
                            placeholder="Tìm quyền…"
                            value={search}
                            onChange={(v) => {
                                setSearch(v);
                                setPage(0);
                            }}
                        />
                    </>
                }
                pagination={
                    meta
                        ? {
                            page,
                            totalPages: Math.max(1, meta.totalPages),
                            totalItems: meta.totalItems,
                            itemLabel: 'quyền',
                            onPageChange: setPage,
                        }
                        : null
                }
            >
                {isLoading ? (
                    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {Array.from({ length: 6 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 36, borderRadius: 8 }} />)}
                    </div>
                ) : grouped.length === 0 ? (
                    <div className="empty-state"><Key size={40} className="empty-icon" /><p className="empty-text">Không có quyền nào</p></div>
                ) : (
                    grouped.map(([resource, items]) => {
                        const isExpanded = expandedResources.has(resource) || !!search;
                        return (
                            <div key={resource} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                                {/* Resource header */}
                                <div
                                    style={{
                                        display: 'flex', alignItems: 'center', gap: 10,
                                        padding: '12px 20px', background: 'var(--bg-elevated)',
                                        cursor: 'pointer', userSelect: 'none'
                                    }}
                                    onClick={() => toggleResource(resource)}
                                >
                                    <div style={{
                                        width: 20, height: 20, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                        transition: 'transform 0.2s', transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
                                        color: isExpanded ? 'var(--accent-light)' : 'var(--text-muted)'
                                    }}>
                                        <ChevronRight size={14} />
                                    </div>
                                    <span style={{
                                        fontSize: 11, fontWeight: 700, letterSpacing: '0.07em',
                                        textTransform: 'uppercase', color: 'var(--accent-light)', flex: 1
                                    }}>
                                        {resource}
                                    </span>
                                    <span className="badge badge-gray">{items.length}</span>
                                </div>

                                {isExpanded && (
                                    <table>
                                        <thead>
                                            <tr>
                                                <th style={{ width: '25%' }}>Code</th>
                                                <th>Tên hiển thị</th>
                                                <th style={{ width: '10%' }}>Action</th>
                                                <th style={{ width: '10%' }}>Loại</th>
                                                <th style={{ width: '12%' }}>Trạng thái</th>
                                                <th style={{ width: '15%' }}>Hành động</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {items.map(perm => (
                                                <tr key={perm.id}>
                                                    <td>
                                                        <code style={{
                                                            fontSize: 11, color: 'var(--accent-light)',
                                                            background: 'var(--accent-glow)', padding: '2px 6px', borderRadius: 4
                                                        }}>
                                                            {perm.code}
                                                        </code>
                                                    </td>
                                                    <td className="td-primary">{perm.displayName}</td>
                                                    <td><span className="badge badge-blue">{perm.action}</span></td>
                                                    <td>
                                                        {perm.isSystem
                                                            ? <span className="badge badge-amber">Hệ thống</span>
                                                            : <span className="badge badge-gray">Tùy chỉnh</span>}
                                                    </td>
                                                    <td>
                                                        {perm.isDeleted ? (
                                                            <span className="badge badge-red">Đã xóa</span>
                                                        ) : perm.isActive ? (
                                                            <span className="badge badge-green">Hoạt động</span>
                                                        ) : (
                                                            <span className="badge badge-amber">Vô hiệu</span>
                                                        )}
                                                    </td>
                                                    <td>
                                                        <div style={{ display: 'flex', gap: 6 }}>
                                                            {!perm.isDeleted ? (
                                                                <>
                                                                    {hasPermission('MANAGE_PERMISSIONS') && (
                                                                        <button className="btn btn-icon btn-ghost" title="Cập nhật"
                                                                            id={`edit-perm-${perm.id}`}
                                                                            onClick={(e) => { e.stopPropagation(); setFormModal({ open: true, perm }); }}
                                                                            style={{ color: 'var(--accent-light)' }}>
                                                                            <Pencil size={14} />
                                                                        </button>
                                                                    )}
                                                                    {hasPermission('MANAGE_PERMISSIONS') && (
                                                                        <button className="btn btn-icon btn-ghost"
                                                                            title={perm.isActive ? 'Khóa' : 'Mở khóa'}
                                                                            id={`toggle-perm-${perm.id}`}
                                                                            onClick={(e) => { e.stopPropagation(); handleToggle(perm); }}
                                                                            style={{ color: perm.isActive ? 'var(--amber)' : 'var(--green)' }}>
                                                                            {perm.isActive ? <Lock size={15} /> : <Unlock size={15} />}
                                                                        </button>
                                                                    )}
                                                                    {hasPermission('MANAGE_PERMISSIONS') && (
                                                                        <button className="btn btn-icon btn-danger-subtle" title="Xóa"
                                                                            id={`delete-perm-${perm.id}`}
                                                                            onClick={(e) => { e.stopPropagation(); handleDelete(perm); }}>
                                                                            <Trash2 size={14} />
                                                                        </button>
                                                                    )}
                                                                </>
                                                            ) : (
                                                                hasPermission('MANAGE_PERMISSIONS') && (
                                                                    <button className="btn btn-icon btn-ghost" title="Khôi phục"
                                                                        id={`restore-perm-${perm.id}`}
                                                                        onClick={(e) => { e.stopPropagation(); handleRestore(perm); }}
                                                                        style={{ color: 'var(--blue)' }}>
                                                                        <RotateCcw size={15} />
                                                                    </button>
                                                                )
                                                            )}
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        );
                    })
                )}

            </DataTable>

            {formModal.open && (
                <PermFormModal initial={formModal.perm} onClose={() => setFormModal({ open: false })} pushToast={pushToast} />
            )}

            <ToastStack toasts={toasts} />
        </>
    );
}
