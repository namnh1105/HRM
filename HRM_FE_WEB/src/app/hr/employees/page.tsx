'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { Eye, Pencil, Plus, Users, X } from 'lucide-react';
import {
    useCreateEmployeeMutation,
    useGetEmployeesQuery,
    useUpdateEmployeeMutation,
    useGetEmployeeStatsQuery,
    useGetEmployeeByIdQuery,
    useGetContractsByEmployeeQuery,
    useGetDegreesByEmployeeQuery,
    useGetInsuranceRegistrationsByEmployeeQuery,
    useGetStoresQuery,
    useCreateContractMutation,
    useCreateDegreeMutation,
    useSearchEmployeesQuery,
} from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import StatCards from '@/components/ui/StatCards';
import DataTable from '@/components/ui/DataTable';
import { FilterPills, SearchBox } from '@/components/ui/ToolbarControls';
import { useToast } from '@/hooks/useToast';
import ToastStack from '@/components/ToastStack';
import { usePermissions } from '@/hooks/usePermissions';

type EmployeeRow = {
    id: string;
    fullName: string;
    email: string;
    phone?: string;
    employmentStatus: string;
    createdAt?: string;
    [key: string]: any;
};

function StatusBadge({ status }: { status?: string }) {
    const s = (status ?? '').toUpperCase();
    if (s === 'ACTIVE') return <span className="badge badge-green">Đang làm việc</span>;
    if (s === 'INACTIVE') return <span className="badge badge-amber">Không hoạt động</span>;
    if (s === 'TERMINATED') return <span className="badge badge-red">Đã nghỉ</span>;
    return <span className="badge badge-gray">{status || '—'}</span>;
}

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    return isNaN(d.getTime()) ? val : d.toLocaleDateString('vi-VN');
}

function formatGender(val: string | undefined) {
    if (val === 'MALE') return 'Nam';
    if (val === 'FEMALE') return 'Nữ';
    if (val === 'OTHER') return 'Khác';
    return val || '—';
}

const DEGREE_LEVEL_LABELS: Record<string, string> = {
    HIGH_SCHOOL: 'Trung học phổ thông',
    COLLEGE: 'Cao đẳng',
    BACHELOR: 'Cử nhân / Đại học',
    MASTER: 'Thạc sĩ',
    DOCTORATE: 'Tiến sĩ',
    OTHER: 'Khác'
};

export default function EmployeesPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [search, setSearch] = useState('');
    const [filterStatus, setFilterStatus] = useState<'all' | 'active' | 'inactive'>('all');

    const trimmedSearch = search.trim();
    const { data: searchData, isFetching: searchFetching } = useSearchEmployeesQuery(
        { keyword: trimmedSearch, page, size: pageSize },
        { skip: !trimmedSearch }
    );
    const { data, isLoading, isFetching: allFetching, refetch } = useGetEmployeesQuery({ page, size: pageSize });

    // Fetch stats
    const { data: statsData } = useGetEmployeeStatsQuery();
    const stats: any = (statsData as any)?.data;

    // Đồng bộ cách lấy data và pagination giống Roles page
    const activeData = trimmedSearch ? searchData : data;
    const employees = (activeData as any)?.data ?? [];
    const meta = (activeData as any)?.pagination;
    const isFetching = trimmedSearch ? searchFetching : allFetching;

    const [formModal, setFormModal] = useState<{ open: boolean; employee?: EmployeeRow }>({ open: false });
    const [detailRow, setDetailRow] = useState<EmployeeRow | null>(null);

    const filtered = useMemo(() => {
        let list = [...employees];
        if (filterStatus === 'active') list = list.filter(r => r.employmentStatus?.toUpperCase() === 'ACTIVE');
        if (filterStatus === 'inactive') list = list.filter(r => r.employmentStatus?.toUpperCase() !== 'ACTIVE');
        return list;
    }, [employees, filterStatus]);

    return (
        <>
            <PageHeader
                title="Nhân viên"
                subtitle="Quản lý danh sách nhân viên và hồ sơ"
                actions={
                    hasPermission('CREATE_EMPLOYEE') && (
                        <button type="button" className="btn btn-primary" onClick={() => setFormModal({ open: true })}>
                            <Plus size={15} /> Thêm nhân viên
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Tổng nhân viên', value: stats?.total ?? '—' },
                    { label: 'Đang làm việc', value: stats?.active ?? '—', tone: 'green' },
                    { label: 'Thử việc/Nghỉ phép', value: stats?.inactive ?? '—', tone: 'amber' },
                    { label: 'Đã nghỉ việc', value: stats?.deleted ?? '—', tone: 'red' },
                ]}
            />

            <DataTable
                title="Danh sách nhân viên"
                titleIcon={Users}
                isFetching={isFetching}
                toolbarRight={
                    <>
                        <FilterPills
                            options={[
                                { id: 'all', label: 'Tất cả' },
                                { id: 'active', label: 'Đang làm' },
                                { id: 'inactive', label: 'Nghỉ/Khác' },
                            ]}
                            value={filterStatus}
                            onChange={(v) => { setFilterStatus(v); setPage(0); }}
                        />
                        <SearchBox
                            placeholder="Tìm tên, email..."
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
                        itemLabel: 'nhân viên',
                        onPageChange: setPage,
                        pageSize,
                        onPageSizeChange: (s) => { setPageSize(s); setPage(0); }
                    } : null
                }
            >
                <table>
                    <thead>
                        <tr>
                            <th>Họ và tên</th>
                            <th>Email</th>
                            <th>Điện thoại</th>
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
                                        <Users size={40} className="empty-icon" />
                                        <p className="empty-text">Không tìm thấy nhân viên nào</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            filtered.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.fullName}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.email}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.phone || '—'}</td>
                                    <td><StatusBadge status={row.employmentStatus} /></td>
                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(row.createdAt)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 6 }}>
                                            <button className="btn btn-sm btn-ghost" onClick={() => setDetailRow(row)}>
                                                <Eye size={14} /> Xem
                                            </button>
                                            {hasPermission('UPDATE_EMPLOYEE') && (
                                                <button className="btn btn-sm btn-primary" onClick={() => setFormModal({ open: true, employee: row })}>
                                                    <Pencil size={14} /> Sửa
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

            {/* Modals - Giữ lại Logic Form tương ứng */}
            {formModal.open && (
                <EmployeeFormModal
                    initial={formModal.employee}
                    onClose={() => setFormModal({ open: false })}
                    onSaved={refetch}
                    pushToast={pushToast}
                />
            )}
            {detailRow && (
                <EmployeeDetailModal row={detailRow} onClose={() => setDetailRow(null)} pushToast={pushToast} />
            )}

            <ToastStack toasts={toasts} />
        </>
    );
}

// --- Internal components for form/detail (giống cách làm trang roles nếu cần tách) ---
function EmployeeFormModal({ initial, onClose, onSaved, pushToast }: any) {
    const [create] = useCreateEmployeeMutation();
    const [update] = useUpdateEmployeeMutation();
    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({
        fullName: initial?.fullName || '',
        email: initial?.email || '',
        phone: initial?.phone || '',
        employmentStatus: initial?.employmentStatus || 'ACTIVE'
    });

    const handleSubmit = async () => {
        if (!formData.fullName || !formData.email) return pushToast('Vui lòng điền đủ thông tin', 'error');
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
                    <p className="modal-title">{initial ? 'Sửa nhân viên' : 'Thêm nhân viên'}</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body">
                    <div className="field-group">
                        <label className="field-label">Họ và tên</label>
                        <input className="field-input" value={formData.fullName} onChange={e => setFormData({ ...formData, fullName: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Email</label>
                        <input className="field-input" type="email" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Điện thoại</label>
                        <input className="field-input" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Trạng thái</label>
                        <select className="field-input" value={formData.employmentStatus} onChange={e => setFormData({ ...formData, employmentStatus: e.target.value })}>
                            <option value="ACTIVE">Đang làm việc</option>
                            <option value="INACTIVE">Không hoạt động</option>
                            <option value="TERMINATED">Đã nghỉ</option>
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

function EmployeeDetailModal({ row, onClose, pushToast }: any) {
    const { hasPermission } = usePermissions();
    const { data: detailData, isLoading: detailLoading, refetch: refetchDetail } = useGetEmployeeByIdQuery(row.id);
    const employee = (detailData as any)?.data ?? row;

    const { data: storesData } = useGetStoresQuery();
    const stores = (storesData as any)?.data ?? [];

    const [updateEmployee, { isLoading: updating }] = useUpdateEmployeeMutation();

    const { data: degreeData, isLoading: degreeLoading, refetch: refetchDegrees } = useGetDegreesByEmployeeQuery({
        employeeId: row.id,
        page: 0,
        size: 5,
    });
    const degrees = (degreeData as any)?.data ?? [];

    const { data: contractData, isLoading: contractLoading, refetch: refetchContracts } = useGetContractsByEmployeeQuery({
        employeeId: row.id,
        page: 0,
        size: 5,
    });
    const contracts = (contractData as any)?.data ?? [];

    const { data: insuranceData, isLoading: insuranceLoading } = useGetInsuranceRegistrationsByEmployeeQuery(row.id);
    const insuranceRegs = (insuranceData as any)?.data ?? [];

    const [createContract, { isLoading: creatingContract }] = useCreateContractMutation();
    const [createDegree, { isLoading: creatingDegree }] = useCreateDegreeMutation();

    const CONTRACT_TYPE_OPTIONS = [
        { value: 'PROBATION', label: 'Thử việc' },
        { value: 'DEFINITE_TERM', label: 'Xác định thời hạn' },
        { value: 'INDEFINITE_TERM', label: 'Không xác định thời hạn' },
        { value: 'SEASONAL', label: 'Thời vụ' },
        { value: 'PART_TIME', label: 'Bán thời gian' },
    ];

    const CONTRACT_TYPE_LABELS = CONTRACT_TYPE_OPTIONS.reduce<Record<string, string>>((acc, cur) => {
        acc[cur.value] = cur.label;
        return acc;
    }, {});

    const [degreeFile, setDegreeFile] = useState<File | null>(null);
    const [contractFile, setContractFile] = useState<File | null>(null);

    const [activeTab, setActiveTab] = useState<'overview' | 'work' | 'bank' | 'insurance' | 'degrees' | 'contracts' | 'transfer'>('overview');
    const [editing, setEditing] = useState<{ [key: string]: boolean }>({});
    const [showCreateDegree, setShowCreateDegree] = useState(false);
    const [showCreateContract, setShowCreateContract] = useState(false);

    const [degreeForm, setDegreeForm] = useState({
        degreeName: '',
        degreeLevel: '',
        major: '',
        institution: '',
        graduationDate: '',
        gpa: '',
        attachmentUrl: '',
        note: '',
    });

    const [contractForm, setContractForm] = useState({
        contractType: 'PROBATION',
        startDate: new Date().toISOString().slice(0, 10),
        endDate: '',
        signingDate: '',
        baseSalary: '',
        salaryCoefficient: '',
        note: '',
        attachmentUrl: '',
    });

    const [overviewForm, setOverviewForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        gender: '',
        dateOfBirth: '',
        idCardNumber: '',
        permanentAddress: '',
        currentAddress: '',
    });

    const [workForm, setWorkForm] = useState({
        position: '',
        employmentStatus: '',
        joinDate: '',
        leaveDate: '',
    });

    const [bankForm, setBankForm] = useState({
        bankName: '',
        bankAccountNumber: '',
        taxCode: '',
    });

    const [insuranceForm, setInsuranceForm] = useState({
        socialInsuranceNumber: '',
        healthInsuranceNumber: '',
    });

    const [transferForm, setTransferForm] = useState({
        storeId: '',
        position: '',
        employmentStatus: '',
    });

    useEffect(() => {
        setOverviewForm({
            firstName: employee.firstName ?? '',
            lastName: employee.lastName ?? '',
            email: employee.email ?? '',
            phone: employee.phone ?? '',
            gender: employee.gender ?? '',
            dateOfBirth: employee.dateOfBirth ? String(employee.dateOfBirth).slice(0, 10) : '',
            idCardNumber: employee.idCardNumber ?? '',
            permanentAddress: employee.permanentAddress ?? '',
            currentAddress: employee.currentAddress ?? '',
        });
        setWorkForm({
            position: employee.position ?? '',
            employmentStatus: employee.employmentStatus ?? '',
            joinDate: employee.joinDate ? String(employee.joinDate).slice(0, 10) : '',
            leaveDate: employee.leaveDate ? String(employee.leaveDate).slice(0, 10) : '',
        });
        setBankForm({
            bankName: employee.bankName ?? '',
            bankAccountNumber: employee.bankAccountNumber ?? '',
            taxCode: employee.taxCode ?? '',
        });
        setInsuranceForm({
            socialInsuranceNumber: employee.socialInsuranceNumber ?? '',
            healthInsuranceNumber: employee.healthInsuranceNumber ?? '',
        });
        setTransferForm({
            storeId: employee.storeId ?? '',
            position: employee.position ?? '',
            employmentStatus: employee.employmentStatus ?? '',
        });
    }, [employee]);

    const infoItem = (label: string, value?: React.ReactNode) => (
        <div>
            <p className="field-label" style={{ marginBottom: 4 }}>{label}</p>
            <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>{value ?? '—'}</div>
        </div>
    );

    const sectionStyle: React.CSSProperties = {
        border: '1px solid var(--border)',
        borderRadius: 12,
        padding: 12,
        background: 'var(--card)',
    };

    const sectionTitleStyle: React.CSSProperties = {
        fontWeight: 600,
        marginBottom: 10,
    };

    const gridStyle: React.CSSProperties = {
        display: 'grid',
        gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
        gap: 12,
    };

    const formatMoney = (val?: number) => {
        if (val === null || val === undefined) return '—';
        return `${val.toLocaleString('vi-VN')} VNĐ`;
    };

    const handleUpdate = async (payload: Record<string, any>, section: string) => {
        try {
            const sanitized = Object.fromEntries(
                Object.entries(payload).filter(([, value]) => value !== '' && value !== undefined && value !== null)
            );
            await updateEmployee({ id: employee.id, body: sanitized }).unwrap();
            pushToast?.('Cập nhật thành công');
            setEditing((prev) => ({ ...prev, [section]: false }));
            refetchDetail();
        } catch {
            pushToast?.('Cập nhật thất bại', 'error');
        }
    };

    const handleCreateDegree = async () => {
        if (!degreeForm.degreeName) return pushToast?.('Vui lòng nhập tên bằng', 'error');
        try {
            const payload = new FormData();
            payload.append('employeeId', employee.id);
            payload.append('degreeName', degreeForm.degreeName);
            if (degreeForm.degreeLevel) payload.append('degreeLevel', degreeForm.degreeLevel);
            if (degreeForm.major) payload.append('major', degreeForm.major);
            if (degreeForm.institution) payload.append('institution', degreeForm.institution);
            if (degreeForm.graduationDate) payload.append('graduationDate', degreeForm.graduationDate);
            if (degreeForm.gpa) payload.append('gpa', String(degreeForm.gpa));
            if (degreeForm.note) payload.append('note', degreeForm.note);
            if (degreeFile) payload.append('file', degreeFile);
            await createDegree(payload).unwrap();
            pushToast?.('Đã thêm bằng cấp');
            setShowCreateDegree(false);
            setDegreeFile(null);
            setDegreeForm({
                degreeName: '',
                degreeLevel: '',
                major: '',
                institution: '',
                graduationDate: '',
                gpa: '',
                attachmentUrl: '',
                note: '',
            });
            refetchDegrees();
        } catch {
            pushToast?.('Thêm bằng cấp thất bại', 'error');
        }
    };

    const handleCreateContract = async () => {
        if (!contractForm.baseSalary) {
            return pushToast?.('Vui lòng nhập lương cơ bản', 'error');
        }
        try {
            const payload = new FormData();
            payload.append('employeeId', employee.id);
            payload.append('contractType', contractForm.contractType);
            payload.append('startDate', contractForm.startDate);
            if (contractForm.endDate) payload.append('endDate', contractForm.endDate);
            if (contractForm.signingDate) payload.append('signingDate', contractForm.signingDate);
            payload.append('baseSalary', String(contractForm.baseSalary));
            if (contractForm.salaryCoefficient) payload.append('salaryCoefficient', String(contractForm.salaryCoefficient));
            if (contractForm.note) payload.append('note', contractForm.note);
            if (contractFile) payload.append('file', contractFile);
            await createContract(payload).unwrap();
            pushToast?.('Đã thêm hợp đồng');
            setShowCreateContract(false);
            setContractFile(null);
            setContractForm({
                contractType: 'PROBATION',
                startDate: new Date().toISOString().slice(0, 10),
                endDate: '',
                signingDate: '',
                baseSalary: '',
                salaryCoefficient: '',
                note: '',
                attachmentUrl: '',
            });
            refetchContracts();
        } catch {
            pushToast?.('Thêm hợp đồng thất bại', 'error');
        }
    };

    const handleDegreeUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0] || null;
        setDegreeFile(file);
    };

    const handleContractUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0] || null;
        setContractFile(file);
    };

    const genericStatusBadge = (status?: string) => {
        const s = (status ?? '').toUpperCase();
        if (s.includes('ACTIVE')) return <span className="badge badge-green">{status || '—'}</span>;
        if (s.includes('EXPIRED') || s.includes('ENDED') || s.includes('TERMINATED')) {
            return <span className="badge badge-red">{status || '—'}</span>;
        }
        if (s.includes('PENDING')) return <span className="badge badge-amber">{status || '—'}</span>;
        return <span className="badge badge-gray">{status || '—'}</span>;
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">Chi tiết nhân viên</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body" style={{ display: 'grid', gap: 14 }}>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                        {[
                            { id: 'overview', label: 'Tổng quan' },
                            { id: 'work', label: 'Công việc' },
                            { id: 'bank', label: 'Ngân hàng' },
                            { id: 'insurance', label: 'Bảo hiểm' },
                            { id: 'degrees', label: 'Bằng cấp' },
                            { id: 'contracts', label: 'Hợp đồng' },
                            { id: 'transfer', label: 'Điều chuyển' },
                        ].map((tab) => (
                            <button
                                key={tab.id}
                                className={`btn btn-sm ${activeTab === tab.id ? 'btn-primary' : 'btn-ghost'}`}
                                onClick={() => setActiveTab(tab.id as any)}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    {activeTab === 'overview' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Thông tin cơ bản & địa chỉ</div>
                                {hasPermission('UPDATE_EMPLOYEE') && (
                                    <button
                                        className="btn btn-sm btn-ghost"
                                        onClick={() => setEditing((prev) => ({ ...prev, overview: !prev.overview }))}
                                    >
                                        {editing.overview ? 'Hủy' : 'Chỉnh sửa'}
                                    </button>
                                )}
                            </div>
                            {editing.overview ? (
                                <div style={{ display: 'grid', gap: 10 }}>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">Họ</label>
                                            <input className="field-input" value={overviewForm.lastName} onChange={e => setOverviewForm({ ...overviewForm, lastName: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Tên</label>
                                            <input className="field-input" value={overviewForm.firstName} onChange={e => setOverviewForm({ ...overviewForm, firstName: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Email</label>
                                            <input className="field-input" type="email" value={overviewForm.email} onChange={e => setOverviewForm({ ...overviewForm, email: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Điện thoại</label>
                                            <input className="field-input" value={overviewForm.phone} onChange={e => setOverviewForm({ ...overviewForm, phone: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Giới tính</label>
                                            <select className="field-input" value={overviewForm.gender} onChange={e => setOverviewForm({ ...overviewForm, gender: e.target.value })}>
                                                <option value="">Chọn giới tính</option>
                                                <option value="MALE">Nam</option>
                                                <option value="FEMALE">Nữ</option>
                                                <option value="OTHER">Khác</option>
                                            </select>
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Ngày sinh</label>
                                            <input className="field-input" type="date" value={overviewForm.dateOfBirth} onChange={e => setOverviewForm({ ...overviewForm, dateOfBirth: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Số CCCD/CMND</label>
                                            <input className="field-input" value={overviewForm.idCardNumber} onChange={e => setOverviewForm({ ...overviewForm, idCardNumber: e.target.value })} />
                                        </div>
                                    </div>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">Thường trú</label>
                                            <input className="field-input" value={overviewForm.permanentAddress} onChange={e => setOverviewForm({ ...overviewForm, permanentAddress: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Hiện tại</label>
                                            <input className="field-input" value={overviewForm.currentAddress} onChange={e => setOverviewForm({ ...overviewForm, currentAddress: e.target.value })} />
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <button className="btn btn-primary" disabled={updating} onClick={() => handleUpdate(overviewForm, 'overview')}>Lưu</button>
                                    </div>
                                </div>
                            ) : (
                                <div style={gridStyle}>
                                    {infoItem('Họ tên', employee.fullName)}
                                    {infoItem('Email', employee.email)}
                                    {infoItem('Điện thoại', employee.phone)}
                                    {infoItem('Giới tính', formatGender(employee.gender))}
                                    {infoItem('Ngày sinh', formatDate(employee.dateOfBirth))}
                                    {infoItem('Số CCCD/CMND', employee.idCardNumber)}
                                    {infoItem('Trạng thái', <StatusBadge status={employee.employmentStatus} />)}
                                    {infoItem('Thường trú', employee.permanentAddress)}
                                    {infoItem('Hiện tại', employee.currentAddress)}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'work' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Thông tin công việc</div>
                                {hasPermission('UPDATE_EMPLOYEE') && (
                                    <button
                                        className="btn btn-sm btn-ghost"
                                        onClick={() => setEditing((prev) => ({ ...prev, work: !prev.work }))}
                                    >
                                        {editing.work ? 'Hủy' : 'Chỉnh sửa'}
                                    </button>
                                )}
                            </div>
                            {editing.work ? (
                                <div style={{ display: 'grid', gap: 10 }}>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">Chức vụ</label>
                                            <input className="field-input" value={workForm.position} onChange={e => setWorkForm({ ...workForm, position: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Trạng thái</label>
                                            <select className="field-input" value={workForm.employmentStatus} onChange={e => setWorkForm({ ...workForm, employmentStatus: e.target.value })}>
                                                <option value="">—</option>
                                                <option value="ACTIVE">Đang làm việc</option>
                                                <option value="INACTIVE">Không hoạt động</option>
                                                <option value="TERMINATED">Đã nghỉ</option>
                                            </select>
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Ngày vào làm</label>
                                            <input className="field-input" type="date" value={workForm.joinDate} onChange={e => setWorkForm({ ...workForm, joinDate: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Ngày nghỉ</label>
                                            <input className="field-input" type="date" value={workForm.leaveDate} onChange={e => setWorkForm({ ...workForm, leaveDate: e.target.value })} />
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <button className="btn btn-primary" disabled={updating} onClick={() => handleUpdate(workForm, 'work')}>Lưu</button>
                                    </div>
                                </div>
                            ) : (
                                <div style={gridStyle}>
                                    {infoItem('Chức vụ', employee.position)}
                                    {infoItem('Chi nhánh đang làm', employee.storeName)}
                                    {infoItem('Ngày vào làm', formatDate(employee.joinDate))}
                                    {infoItem('Ngày nghỉ', formatDate(employee.leaveDate))}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'bank' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Thẻ ngân hàng & Thuế</div>
                                {hasPermission('UPDATE_EMPLOYEE') && (
                                    <button
                                        className="btn btn-sm btn-ghost"
                                        onClick={() => setEditing((prev) => ({ ...prev, bank: !prev.bank }))}
                                    >
                                        {editing.bank ? 'Hủy' : 'Chỉnh sửa'}
                                    </button>
                                )}
                            </div>
                            {editing.bank ? (
                                <div style={{ display: 'grid', gap: 10 }}>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">Tên ngân hàng</label>
                                            <input className="field-input" value={bankForm.bankName} onChange={e => setBankForm({ ...bankForm, bankName: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Số tài khoản</label>
                                            <input className="field-input" value={bankForm.bankAccountNumber} onChange={e => setBankForm({ ...bankForm, bankAccountNumber: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Mã số thuế</label>
                                            <input className="field-input" value={bankForm.taxCode} onChange={e => setBankForm({ ...bankForm, taxCode: e.target.value })} />
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <button className="btn btn-primary" disabled={updating} onClick={() => handleUpdate(bankForm, 'bank')}>Lưu</button>
                                    </div>
                                </div>
                            ) : (
                                <div style={gridStyle}>
                                    {infoItem('Tên ngân hàng', employee.bankName)}
                                    {infoItem('Số tài khoản', employee.bankAccountNumber)}
                                    {infoItem('Mã số thuế', employee.taxCode)}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'insurance' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Bảo hiểm</div>
                                {hasPermission('UPDATE_EMPLOYEE') && (
                                    <button
                                        className="btn btn-sm btn-ghost"
                                        onClick={() => setEditing((prev) => ({ ...prev, insurance: !prev.insurance }))}
                                    >
                                        {editing.insurance ? 'Hủy' : 'Chỉnh sửa'}
                                    </button>
                                )}
                            </div>
                            {editing.insurance ? (
                                <div style={{ display: 'grid', gap: 10 }}>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">BHXH</label>
                                            <input className="field-input" value={insuranceForm.socialInsuranceNumber} onChange={e => setInsuranceForm({ ...insuranceForm, socialInsuranceNumber: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">BHYT</label>
                                            <input className="field-input" value={insuranceForm.healthInsuranceNumber} onChange={e => setInsuranceForm({ ...insuranceForm, healthInsuranceNumber: e.target.value })} />
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <button className="btn btn-primary" disabled={updating} onClick={() => handleUpdate(insuranceForm, 'insurance')}>Lưu</button>
                                    </div>
                                </div>
                            ) : (
                                <div style={gridStyle}>
                                    {infoItem('BHXH', employee.socialInsuranceNumber)}
                                    {infoItem('BHYT', employee.healthInsuranceNumber)}
                                </div>
                            )}
                            <div style={{ marginTop: 12 }}>
                                <div style={{ fontWeight: 600, marginBottom: 8 }}>Đăng ký bảo hiểm</div>
                                {insuranceLoading ? (
                                    <div className="skeleton" style={{ height: 16, width: '60%' }} />
                                ) : insuranceRegs.length === 0 ? (
                                    <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Chưa có đăng ký bảo hiểm</p>
                                ) : (
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Loại</th>
                                                <th>Số đăng ký</th>
                                                <th>Từ ngày</th>
                                                <th>Đến ngày</th>
                                                <th>Trạng thái</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {insuranceRegs.map((reg: any) => (
                                                <tr key={reg.id}>
                                                    <td className="td-primary">{reg.insuranceName || '—'}</td>
                                                    <td style={{ color: 'var(--text-muted)' }}>{reg.registrationNumber || '—'}</td>
                                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(reg.startDate)}</td>
                                                    <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(reg.endDate)}</td>
                                                    <td>{genericStatusBadge(reg.status)}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </div>
                    )}

                    {activeTab === 'degrees' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Bằng cấp</div>
                                {hasPermission('CREATE_DEGREE') && (
                                    <button className="btn btn-sm btn-primary" onClick={() => setShowCreateDegree(true)}>Thêm bằng cấp</button>
                                )}
                            </div>
                            {degreeLoading ? (
                                <div className="skeleton" style={{ height: 16, width: '60%' }} />
                            ) : degrees.length === 0 ? (
                                <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Chưa có bằng cấp</p>
                            ) : (
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Tên bằng</th>
                                            <th>Cấp độ</th>
                                            <th>Chuyên ngành</th>
                                            <th>Trường</th>
                                            <th>Ngày tốt nghiệp</th>
                                            <th>GPA</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {degrees.map((deg: any) => (
                                            <tr key={deg.id}>
                                                <td className="td-primary">{deg.degreeName || '—'}</td>
                                                <td style={{ color: 'var(--text-muted)' }}>{DEGREE_LEVEL_LABELS[deg.degreeLevel] || deg.degreeLevel || '—'}</td>
                                                <td style={{ color: 'var(--text-muted)' }}>{deg.major || '—'}</td>
                                                <td style={{ color: 'var(--text-muted)' }}>{deg.institution || '—'}</td>
                                                <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(deg.graduationDate)}</td>
                                                <td style={{ color: 'var(--text-muted)' }}>{deg.gpa ?? '—'}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    )}

                    {activeTab === 'contracts' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Hợp đồng</div>
                                {hasPermission('CREATE_CONTRACT') && (
                                    <button
                                        className="btn btn-sm btn-primary"
                                        onClick={() => setShowCreateContract(true)}
                                    >
                                        Thêm hợp đồng
                                    </button>
                                )}
                            </div>
                            {contractLoading ? (
                                <div className="skeleton" style={{ height: 16, width: '60%' }} />
                            ) : contracts.length === 0 ? (
                                <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>Chưa có hợp đồng</p>
                            ) : (
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Loại</th>
                                            <th>Từ ngày</th>
                                            <th>Đến ngày</th>
                                            <th>Lương cơ bản</th>
                                            <th>Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {contracts.map((c: any) => (
                                            <tr key={c.id}>
                                                <td style={{ color: 'var(--text-muted)' }}>
                                                    {CONTRACT_TYPE_LABELS[c.contractType || c.type || ''] || c.contractType || c.type || '—'}
                                                </td>
                                                <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(c.startDate)}</td>
                                                <td style={{ color: 'var(--text-muted)', fontSize: 12 }}>{formatDate(c.endDate)}</td>
                                                <td style={{ color: 'var(--text-muted)' }}>{formatMoney(c.baseSalary)}</td>
                                                <td>{genericStatusBadge(c.status)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    )}

                    {activeTab === 'transfer' && (
                        <div style={sectionStyle}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={sectionTitleStyle}>Điều chuyển công tác</div>
                                {hasPermission('UPDATE_EMPLOYEE') && (
                                    <button
                                        className="btn btn-sm btn-ghost"
                                        onClick={() => setEditing((prev) => ({ ...prev, transfer: !prev.transfer }))}
                                    >
                                        {editing.transfer ? 'Hủy' : 'Chỉnh sửa'}
                                    </button>
                                )}
                            </div>
                            {editing.transfer ? (
                                <div style={{ display: 'grid', gap: 10 }}>
                                    <div style={gridStyle}>
                                        <div className="field-group">
                                            <label className="field-label">Chi nhánh</label>
                                            <select className="field-input" value={transferForm.storeId} onChange={e => setTransferForm({ ...transferForm, storeId: e.target.value })}>
                                                <option value="">—</option>
                                                {stores.map((s: any) => (
                                                    <option key={s.id} value={s.id}>{s.name}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Chức vụ</label>
                                            <input className="field-input" value={transferForm.position} onChange={e => setTransferForm({ ...transferForm, position: e.target.value })} />
                                        </div>
                                        <div className="field-group">
                                            <label className="field-label">Trạng thái</label>
                                            <select className="field-input" value={transferForm.employmentStatus} onChange={e => setTransferForm({ ...transferForm, employmentStatus: e.target.value })}>
                                                <option value="">—</option>
                                                <option value="ACTIVE">Đang làm việc</option>
                                                <option value="INACTIVE">Không hoạt động</option>
                                                <option value="TERMINATED">Đã nghỉ</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', gap: 8 }}>
                                        <button className="btn btn-primary" disabled={updating} onClick={() => handleUpdate(transferForm, 'transfer')}>Lưu điều chuyển</button>
                                    </div>
                                </div>
                            ) : (
                                <div style={gridStyle}>
                                    {infoItem('Chi nhánh hiện tại', employee.storeName)}
                                    {infoItem('Chức vụ', employee.position)}
                                    {infoItem('Trạng thái', <StatusBadge status={employee.employmentStatus} />)}
                                </div>
                            )}
                        </div>
                    )}

                    <div style={sectionStyle}>
                        <div style={sectionTitleStyle}>Thông tin hệ thống</div>
                        {detailLoading ? (
                            <div className="skeleton" style={{ height: 16, width: '60%' }} />
                        ) : (
                            <div style={gridStyle}>
                                {infoItem('ID', <code style={{ fontSize: 11 }}>{employee.id}</code>)}
                                {infoItem('Ngày tạo', formatDate(employee.createdAt))}
                                {infoItem('Ngày cập nhật', formatDate(employee.updatedAt))}
                            </div>
                        )}
                    </div>
                </div>
                <div className="modal-footer">
                    <button className="btn btn-ghost" onClick={onClose}>Đóng</button>
                </div>
            </div>
            <CreateDegreeModal
                open={showCreateDegree}
                onClose={() => setShowCreateDegree(false)}
                onSubmit={handleCreateDegree}
                loading={creatingDegree}
                form={degreeForm}
                setForm={setDegreeForm}
                onUpload={handleDegreeUpload}
                file={degreeFile}
            />
            <CreateContractModal
                open={showCreateContract}
                onClose={() => setShowCreateContract(false)}
                onSubmit={handleCreateContract}
                loading={creatingContract}
                form={contractForm}
                setForm={setContractForm}
                contractTypes={CONTRACT_TYPE_OPTIONS}
                onUpload={handleContractUpload}
                file={contractFile}
            />
        </div>
    );
}

function CreateDegreeModal({ open, onClose, onSubmit, loading, form, setForm, file, onUpload }: any) {
    if (!open) return null;
    return (
        <div className="modal-overlay" onClick={(e) => { e.stopPropagation(); onClose(); }}>
            <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">Thêm bằng cấp</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body">
                    <div className="field-group">
                        <label className="field-label">Tên bằng</label>
                        <input className="field-input" value={form.degreeName} onChange={e => setForm({ ...form, degreeName: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Cấp độ</label>
                        <select className="field-input" value={form.degreeLevel} onChange={e => setForm({ ...form, degreeLevel: e.target.value })}>
                            <option value="">Chọn cấp độ</option>
                            <option value="HIGH_SCHOOL">Trung học phổ thông</option>
                            <option value="COLLEGE">Cao đẳng</option>
                            <option value="BACHELOR">Cử nhân / Đại học</option>
                            <option value="MASTER">Thạc sĩ</option>
                            <option value="DOCTORATE">Tiến sĩ</option>
                            <option value="OTHER">Khác</option>
                        </select>
                    </div>
                    <div className="field-group">
                        <label className="field-label">Chuyên ngành</label>
                        <input className="field-input" value={form.major} onChange={e => setForm({ ...form, major: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Trường</label>
                        <input className="field-input" value={form.institution} onChange={e => setForm({ ...form, institution: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ngày tốt nghiệp</label>
                        <input className="field-input" type="date" value={form.graduationDate} onChange={e => setForm({ ...form, graduationDate: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">GPA</label>
                        <input className="field-input" type="number" value={form.gpa} onChange={e => setForm({ ...form, gpa: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Đính kèm tài liệu</label>
                        <input className="field-input" type="file" onChange={onUpload} />
                        {file && <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Đã chọn: {file.name}</p>}
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ghi chú</label>
                        <input className="field-input" value={form.note} onChange={e => setForm({ ...form, note: e.target.value })} />
                    </div>
                </div>
                <div className="modal-footer">
                    <button className="btn btn-ghost" onClick={onClose}>Hủy</button>
                    <button className="btn btn-primary" onClick={onSubmit} disabled={loading}>Lưu</button>
                </div>
            </div>
        </div>
    );
}

function CreateContractModal({ open, onClose, onSubmit, loading, form, setForm, contractTypes, file, onUpload }: any) {
    if (!open) return null;
    return (
        <div className="modal-overlay" onClick={(e) => { e.stopPropagation(); onClose(); }}>
            <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">Thêm hợp đồng</p>
                    <button className="btn btn-icon btn-ghost" onClick={onClose}><X size={16} /></button>
                </div>
                <div className="modal-body">
                    <div className="field-group">
                        <label className="field-label">Loại hợp đồng</label>
                        <select className="field-input" value={form.contractType} onChange={e => setForm({ ...form, contractType: e.target.value })}>
                            {contractTypes.map((opt: any) => (
                                <option key={opt.value} value={opt.value}>{opt.label}</option>
                            ))}
                        </select>
                    </div>
                    <div className="field-group">
                        <label className="field-label">Từ ngày</label>
                        <input className="field-input" type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Đến ngày (Tùy chọn)</label>
                        <input className="field-input" type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ngày ký (Tùy chọn)</label>
                        <input className="field-input" type="date" value={form.signingDate} onChange={e => setForm({ ...form, signingDate: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Lương cơ bản</label>
                        <input className="field-input" type="number" value={form.baseSalary} onChange={e => setForm({ ...form, baseSalary: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Hệ số lương</label>
                        <input className="field-input" type="number" value={form.salaryCoefficient} onChange={e => setForm({ ...form, salaryCoefficient: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Ghi chú</label>
                        <input className="field-input" value={form.note} onChange={e => setForm({ ...form, note: e.target.value })} />
                    </div>
                    <div className="field-group">
                        <label className="field-label">Đính kèm tài liệu</label>
                        <input className="field-input" type="file" onChange={onUpload} />
                        {file && <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Đã chọn: {file.name}</p>}
                    </div>
                </div>
                <div className="modal-footer">
                    <button className="btn btn-ghost" onClick={onClose}>Hủy</button>
                    <button className="btn btn-primary" onClick={onSubmit} disabled={loading}>Lưu</button>
                </div>
            </div>
        </div>
    );
}
