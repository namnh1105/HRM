'use client';
import React, { useState, useMemo } from 'react';
import {
    useGetAllRolesQuery,
    useCreateRoleMutation,
    useUpdateRoleMutation,
    useDeleteRoleMutation,
    useActivateRoleMutation,
    useDeactivateRoleMutation,
    useRestoreRoleMutation,
    useGetRolePermissionsQuery,
    useSyncPermissionsToRoleMutation,
    useGetRoleStatsQuery,
} from '@/store/api/roleApi';
import {
    ChevronRight,
    Lock,
    Unlock,
    RotateCcw,
    Plus,
    Shield,
    Trash2,
    Pencil,
    X,
    Key,
    CheckSquare,
} from 'lucide-react';
import { useGetActivePermissionsQuery } from '@/store/api/permissionApi';
import type { RoleResponse, CreateRoleRequest, UpdateRoleRequest } from '@/types';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { SearchBox } from '@/components/ui/ToolbarControls';
import EntityStatusFilters, { type EntityFilterStatus } from '@/components/ui/EntityStatusFilters';
import ToastStack from '@/components/ToastStack';
import { useToast } from '@/hooks/useToast';
import { usePermissions } from '@/hooks/usePermissions';

// ─── Role Form Modal ──────────────────────────────────────────────────────────
function RoleFormModal({
    initial,
    onClose,
    pushToast,
}: {
    initial?: RoleResponse;
    onClose: () => void;
    pushToast: (msg: string, type?: 'success' | 'error') => void;
}) {
    const [form, setForm] = useState({
        code: initial?.code ?? '',
        displayName: initial?.displayName ?? '',
        description: initial?.description ?? '',
        isActive: initial?.isActive ?? true,
    });
    const [createRole, { isLoading: creating }] = useCreateRoleMutation();
    const [updateRole, { isLoading: updating }] = useUpdateRoleMutation();

    const busy = creating || updating;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.code.trim() || !form.displayName.trim()) return;
        try {
            if (initial) {
                await updateRole({ roleId: initial.id, body: form }).unwrap();
                pushToast('Đã cập nhật role');
            } else {
                await createRole(form).unwrap();
                pushToast('Đã tạo role mới');
            }
            onClose();
        } catch {
            pushToast('Thao tác thất bại', 'error');
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{initial ? 'Cập nhật Role' : 'Tạo Role mới'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="field-group">
                            <label className="field-label">Mã code *</label>
                            <input
                                id="role-code-input"
                                className="field-input"
                                placeholder="vd: ROLE_MANAGER"
                                value={form.code}
                                onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label className="field-label">Tên hiển thị *</label>
                            <input
                                id="role-name-input"
                                className="field-input"
                                placeholder="vd: Quản lý cửa hàng"
                                value={form.displayName}
                                onChange={(e) => setForm({ ...form, displayName: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label className="field-label">Mô tả</label>
                            <input
                                id="role-desc-input"
                                className="field-input"
                                placeholder="Mô tả ngắn về role"
                                value={form.description}
                                onChange={(e) => setForm({ ...form, description: e.target.value })}
                            />
                        </div>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', fontSize: 13 }}>
                            <input
                                type="checkbox"
                                checked={form.isActive}
                                onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
                                style={{ accentColor: 'var(--accent)', width: 16, height: 16 }}
                            />
                            <span style={{ color: 'var(--text-secondary)' }}>Kích hoạt ngay</span>
                        </label>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-ghost" onClick={onClose}>Hủy</button>
                        <button type="submit" className="btn btn-primary" id="role-submit-btn" disabled={busy}>
                            {busy ? 'Đang lưu…' : initial ? 'Cập nhật' : 'Tạo mới'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

// ─── Permission Manager Modal ─────────────────────────────────────────────────
// ─── Permission Manager Modal ─────────────────────────────────────────────────
function PermissionManagerModal({
    role,
    onClose,
    pushToast,
}: {
    role: RoleResponse;
    onClose: () => void;
    pushToast: (msg: string, type?: 'success' | 'error') => void;
}) {
    const { data: roleData, isLoading: roleLoading } = useGetRolePermissionsQuery(role.id);
    const { data: allPermsData, isLoading: permsLoading } = useGetActivePermissionsQuery();
    const [syncPermissions, { isLoading: saving }] = useSyncPermissionsToRoleMutation();
    const [permSearch, setPermSearch] = useState('');

    // Local state for selected permission IDs
    const [selectedIds, setSelectedIds] = useState<Set<string>>(() => {
        const perms = role.permissions ?? [];
        return new Set(
            perms
                .map((p) => (typeof p === 'string' ? p : p.id))
                .filter((id): id is string => Boolean(id))
        );
    });

    const [initialized, setInitialized] = useState(false);

    // Sync local state when roleData (full fetch) is loaded
    React.useEffect(() => {
        if (!initialized && roleData?.data && Array.isArray(roleData.data)) {
            const ids = roleData.data.map(p => String(p.id));
            setSelectedIds(new Set(ids));
            setInitialized(true);
        }
    }, [roleData, initialized]);

    const filteredPerms = useMemo(() => {
        const all = allPermsData?.data ?? [];
        if (!permSearch.trim()) return all;
        const q = permSearch.toLowerCase();
        return all.filter(
            (p) =>
                p.code.toLowerCase().includes(q) ||
                p.displayName.toLowerCase().includes(q) ||
                p.resource.toLowerCase().includes(q)
        );
    }, [allPermsData, permSearch]);

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
        const map: Record<string, typeof filteredPerms> = {};
        filteredPerms.forEach((p) => {
            if (!map[p.resource]) map[p.resource] = [];
            map[p.resource].push(p);
        });
        return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
    }, [filteredPerms]);

    // Auto expand first group
    React.useEffect(() => {
        if (!permSearch && grouped.length > 0 && expandedResources.size === 0) {
            setExpandedResources(new Set([grouped[0][0]]));
        }
    }, [grouped, permSearch]);

    const handleToggle = (permId: string) => {
        setSelectedIds(prev => {
            const next = new Set(prev);
            if (next.has(permId)) next.delete(permId);
            else next.add(permId);
            return next;
        });
    };

    const handleSave = async () => {
        try {
            await syncPermissions({
                roleId: role.id,
                permissionIds: Array.from(selectedIds)
            }).unwrap();
            pushToast('Đã cập nhật danh sách quyền');
            onClose();
        } catch (err: unknown) {
            const msg =
                typeof err === 'object' &&
                    err !== null &&
                    'data' in err &&
                    typeof (err as { data?: { message?: unknown } }).data?.message === 'string'
                    ? (err as { data: { message: string } }).data.message
                    : 'Cập nhật thất bại';
            pushToast(msg, 'error');
        }
    };

    const isLoading = roleLoading || permsLoading;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal modal-wide" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <p className="modal-title">Quản lý Quyền — {role.displayName}</p>
                        <p style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                            Đã chọn <span style={{ color: 'var(--accent-light)', fontWeight: 600 }}>{selectedIds.size}</span> quyền
                        </p>
                    </div>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>

                <div style={{ marginBottom: 12 }}>
                    <SearchBox
                        id="perm-manager-search"
                        placeholder="Tìm quyền…"
                        value={permSearch}
                        onChange={setPermSearch}
                        wrapperStyle={{ width: '100%' }}
                        inputStyle={{ width: '100%' }}
                    />
                </div>

                <div style={{ maxHeight: 420, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 12 }}>
                    {isLoading ? (
                        Array.from({ length: 4 }).map((_, i) => (
                            <div key={i} className="skeleton" style={{ height: 60, borderRadius: 8 }} />
                        ))
                    ) : grouped.length === 0 ? (
                        <div className="empty-state"><p className="empty-text">Không có quyền nào khớp</p></div>
                    ) : (
                        grouped.map(([resource, perms]) => {
                            const isExpanded = expandedResources.has(resource) || !!permSearch;
                            return (
                                <div key={resource} style={{ borderBottom: '1px solid var(--border-subtle)', paddingBottom: 8 }}>
                                    <div
                                        style={{
                                            display: 'flex', alignItems: 'center', gap: 8, padding: '8px 4px',
                                            cursor: 'pointer', userSelect: 'none'
                                        }}
                                        onClick={() => toggleResource(resource)}
                                    >
                                        <div style={{
                                            transition: 'transform 0.2s', transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
                                            color: isExpanded ? 'var(--accent-light)' : 'var(--text-muted)'
                                        }}>
                                            <ChevronRight size={14} />
                                        </div>
                                        <p style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.07em', color: 'var(--accent-light)', flex: 1 }}>
                                            {resource}
                                        </p>
                                        <span className="badge badge-gray" style={{ fontSize: 9 }}>{perms.length}</span>
                                    </div>

                                    {isExpanded && (
                                        <div style={{
                                            display: 'grid',
                                            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                                            gap: 8,
                                            marginTop: 8,
                                            paddingLeft: 22
                                        }}>
                                            {perms.map((perm) => {
                                                const pId = String(perm.id);
                                                const has = selectedIds.has(pId);
                                                return (
                                                    <div
                                                        key={pId}
                                                        style={{
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            gap: 12,
                                                            padding: '12px 14px',
                                                            borderRadius: 10,
                                                            background: has ? 'var(--accent-glow)' : 'var(--bg-card)',
                                                            cursor: 'pointer',
                                                            border: `1.5px solid ${has ? 'var(--accent-light)' : 'var(--border-subtle)'}`,
                                                            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                                                            boxShadow: has ? '0 4px 12px var(--accent-glow)' : 'none',
                                                        }}
                                                        id={`toggle-perm-${pId}`}
                                                        onClick={(e) => {
                                                            e.preventDefault();
                                                            handleToggle(pId);
                                                        }}
                                                    >
                                                        <div style={{
                                                            width: 22, height: 22, borderRadius: 6,
                                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                            background: has ? 'var(--accent-light)' : '#fff',
                                                            border: `2.5px solid ${has ? 'var(--accent-light)' : 'var(--border)'}`,
                                                            transition: 'all 0.2s',
                                                            flexShrink: 0,
                                                            boxShadow: has ? '0 0 0 2px var(--accent-glow)' : 'none'
                                                        }}>
                                                            {has && <CheckSquare size={16} style={{ color: '#fff' }} />}
                                                        </div>
                                                        <div style={{ flex: 1, minWidth: 0 }}>
                                                            <p style={{
                                                                fontSize: 13,
                                                                fontWeight: 600,
                                                                color: has ? 'var(--accent-light)' : 'var(--text-primary)',
                                                                marginBottom: 2
                                                            }}>
                                                                {perm.displayName}
                                                            </p>
                                                            <code style={{
                                                                fontSize: 10,
                                                                color: 'var(--text-muted)',
                                                                opacity: 0.8
                                                            }}>{perm.code}</code>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    )}
                                </div>
                            );
                        })
                    )}
                </div>

                <div className="modal-footer" style={{ marginTop: 20 }}>
                    <button className="btn btn-ghost" onClick={onClose} disabled={saving}>Hủy</button>
                    <button className="btn btn-primary" onClick={handleSave} disabled={isLoading || saving}>
                        {saving ? 'Đang lưu…' : 'Cập nhật quyền'}
                    </button>
                </div>
            </div>
        </div>
    );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function RolesPage() {
    const { hasPermission } = usePermissions();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [search, setSearch] = useState('');
    const [filterStatus, setFilterStatus] = useState<EntityFilterStatus>('all');

    const { data, isLoading, isFetching } = useGetAllRolesQuery({
        page,
        size: pageSize,
        includeDeleted: filterStatus === 'all' || filterStatus === 'deleted'
    });

    const [deleteRole] = useDeleteRoleMutation();
    const [activateRole] = useActivateRoleMutation();
    const [deactivateRole] = useDeactivateRoleMutation();
    const [restoreRole] = useRestoreRoleMutation();

    const [formModal, setFormModal] = useState<{ open: boolean; role?: RoleResponse }>({ open: false });
    const [permModal, setPermModal] = useState<RoleResponse | null>(null);
    const { toasts, push: pushToast } = useToast();

    // Fetch stats
    const { data: statsData } = useGetRoleStatsQuery();
    const stats = statsData?.data;

    const roles = data?.data ?? [];
    const meta = data?.pagination;

    const filtered = useMemo(() => {
        let list = [...roles];
        const q = search.toLowerCase();

        if (q) {
            list = list.filter(
                (r) =>
                    r.code.toLowerCase().includes(q) ||
                    r.displayName.toLowerCase().includes(q) ||
                    r.description?.toLowerCase().includes(q)
            );
        }

        if (filterStatus === 'active') list = list.filter(r => r.isActive && !r.isDeleted);
        if (filterStatus === 'inactive') list = list.filter(r => !r.isActive && !r.isDeleted);
        if (filterStatus === 'deleted') list = list.filter(r => r.isDeleted);

        return list;
    }, [roles, search, filterStatus]);

    const handleDelete = async (role: RoleResponse) => {
        if (!confirm(`Xóa role "${role.displayName}"?`)) return;
        try {
            await deleteRole(role.id).unwrap();
            pushToast('Đã xóa role');
        } catch {
            pushToast('Xóa thất bại', 'error');
        }
    };

    const handleRestore = async (role: RoleResponse) => {
        try {
            await restoreRole(role.id).unwrap();
            pushToast('Đã khôi phục role');
        } catch {
            pushToast('Khôi phục thất bại', 'error');
        }
    };

    const handleToggleActive = async (role: RoleResponse) => {
        try {
            if (role.isActive) {
                await deactivateRole(role.id).unwrap();
                pushToast('Đã vô hiệu hóa');
            } else {
                await activateRole(role.id).unwrap();
                pushToast('Đã kích hoạt');
            }
        } catch {
            pushToast('Thao tác thất bại', 'error');
        }
    };

    return (
        <>
            <PageHeader
                title="Quản lý vai trò"
                subtitle="Tạo và cấu hình các vai trò trong hệ thống"
                actions={
                    hasPermission('MANAGE_ROLES') && (
                        <button type="button" className="btn btn-primary" id="create-role-btn" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Tạo Role mới
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Số lượng vai trò', value: stats?.total ?? '—' },
                    { label: 'Đang hoạt động', value: stats?.active ?? '—', tone: 'green' },
                    { label: 'Ngừng hoạt động', value: stats?.inactive ?? '—', tone: 'amber' },
                    { label: 'Đã xóa', value: stats?.deleted ?? '—', tone: 'red' },
                ]}
            />

            <DataTable
                title="Danh sách Roles"
                titleIcon={Shield}
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
                        <SearchBox
                            placeholder="Tìm mã, tên, mô tả…"
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
                            itemLabel: 'roles',
                            onPageChange: setPage,
                            pageSize,
                            onPageSizeChange: (s) => { setPageSize(s); setPage(0); },
                        }
                        : null
                }
            >
                <table>
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Tên role</th>
                            <th>Mô tả</th>
                            <th>Loại</th>
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
                                        <Shield size={40} className="empty-icon" />
                                        <p className="empty-text">Không có role nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((role) => (
                                <tr key={role.id}>
                                    <td><code style={{ fontSize: 12, color: 'var(--accent-light)', background: 'var(--accent-glow)', padding: '2px 6px', borderRadius: 4 }}>{role.code}</code></td>
                                    <td className="td-primary">{role.displayName}</td>
                                    <td style={{ color: 'var(--text-muted)', maxWidth: 200 }}>
                                        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', display: 'block', whiteSpace: 'nowrap' }}>
                                            {role.description ?? '—'}
                                        </span>
                                    </td>
                                    <td>
                                        {role.isSystem ? (
                                            <span className="badge badge-amber">Hệ thống</span>
                                        ) : (
                                            <span className="badge badge-gray">Tùy chỉnh</span>
                                        )}
                                    </td>
                                    <td>
                                        {role.isDeleted ? (
                                            <span className="badge badge-red">Đã xóa</span>
                                        ) : role.isActive ? (
                                            <span className="badge badge-green">Hoạt động</span>
                                        ) : (
                                            <span className="badge badge-amber">Vô hiệu</span>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 6 }}>
                                            {hasPermission('MANAGE_PERMISSIONS') && (
                                                <button
                                                    className="btn btn-ghost btn-sm"
                                                    title="Xem quyền"
                                                    id={`manage-perm-${role.id}`}
                                                    onClick={() => setPermModal(role)}
                                                >
                                                    <Key size={13} /> Xem quyền
                                                </button>
                                            )}
                                            {!role.isDeleted ? (
                                                <>
                                                    {hasPermission('MANAGE_ROLES') && (
                                                        <button
                                                            className="btn btn-icon btn-ghost"
                                                            title="Cập nhật"
                                                            id={`edit-role-${role.id}`}
                                                            onClick={() => setFormModal({ open: true, role })}
                                                            style={{ color: 'var(--accent-light)' }}
                                                        >
                                                            <Pencil size={14} />
                                                        </button>
                                                    )}
                                                    {hasPermission('MANAGE_ROLES') && (
                                                        <button
                                                            className="btn btn-icon btn-ghost"
                                                            title={role.isActive ? 'Khóa' : 'Mở khóa'}
                                                            id={`toggle-role-${role.id}`}
                                                            onClick={() => handleToggleActive(role)}
                                                            style={{ color: role.isActive ? 'var(--amber)' : 'var(--green)' }}
                                                        >
                                                            {role.isActive ? <Lock size={15} /> : <Unlock size={15} />}
                                                        </button>
                                                    )}
                                                    {hasPermission('MANAGE_ROLES') && (
                                                        <button
                                                            className="btn btn-icon btn-danger-subtle"
                                                            title="Xóa"
                                                            id={`delete-role-${role.id}`}
                                                            onClick={() => handleDelete(role)}
                                                        >
                                                            <Trash2 size={14} />
                                                        </button>
                                                    )}
                                                </>
                                            ) : (
                                                hasPermission('MANAGE_ROLES') && (
                                                    <button
                                                        className="btn btn-icon btn-ghost"
                                                        title="Khôi phục"
                                                        id={`restore-role-${role.id}`}
                                                        onClick={() => handleRestore(role)}
                                                        style={{ color: 'var(--blue)' }}
                                                    >
                                                        <RotateCcw size={15} />
                                                    </button>
                                                )
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </DataTable>

            {/* Modals */}
            {formModal.open && (
                <RoleFormModal
                    initial={formModal.role}
                    onClose={() => setFormModal({ open: false })}
                    pushToast={pushToast}
                />
            )}
            {permModal && (
                <PermissionManagerModal
                    role={permModal}
                    onClose={() => setPermModal(null)}
                    pushToast={pushToast}
                />
            )}

            <ToastStack toasts={toasts} />
        </>
    );
}
