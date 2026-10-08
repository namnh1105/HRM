'use client';

import React, { useMemo, useState } from 'react';
import { Wallet } from 'lucide-react';
import PageHeader from '@/components/ui/PageHeader';
import DataTable from '@/components/ui/DataTable';
import StatCards from '@/components/ui/StatCards';
import ToastStack from '@/components/ToastStack';
import { useToast } from '@/hooks/useToast';
import {
    useAutoGeneratePayrollsMutation,
    useGetPayrollsByMonthYearQuery,
} from '@/store/api/hrApi';
import type { PayrollSummary } from '@/types/payroll';
import { usePermissions } from '@/hooks/usePermissions';

function formatMoney(val?: number) {
    if (val === null || val === undefined) return '—';
    return `${val.toLocaleString('vi-VN')} VNĐ`;
}

function StatusBadge({ status }: { status?: string }) {
    const s = (status ?? '').toUpperCase();
    if (s === 'CONFIRMED') return <span className="badge badge-green">Đã xác nhận</span>;
    if (s === 'PAID') return <span className="badge badge-green">Đã thanh toán</span>;
    if (s === 'CANCELLED') return <span className="badge badge-red">Đã hủy</span>;
    if (s === 'DRAFT') return <span className="badge badge-amber">Nháp</span>;
    return <span className="badge badge-gray">{status || '—'}</span>;
}

export default function PayrollsPage() {
    const { hasPermission } = usePermissions();
    const { toasts, push: pushToast } = useToast();
    const now = new Date();
    const [month, setMonth] = useState<number>(now.getMonth() + 1);
    const [year, setYear] = useState<number>(now.getFullYear());
    const [monthPicker, setMonthPicker] = useState<string>(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`);

    const [latePenaltyPerShift, setLatePenaltyPerShift] = useState<number>(0);
    const [latePenaltyPerHour, setLatePenaltyPerHour] = useState<number>(0);
    const [allowance, setAllowance] = useState<number>(0);
    const [overwriteExisting, setOverwriteExisting] = useState<boolean>(true);

    const { data, isLoading, isFetching, refetch } = useGetPayrollsByMonthYearQuery(
        { month, year },
        { skip: !month || !year }
    );

    const [autoGenerate, { isLoading: generating }] = useAutoGeneratePayrollsMutation();

    const rows: PayrollSummary[] = (data as any)?.data ?? [];

    const stats = useMemo(() => {
        const totalNet = rows.reduce((sum, r) => sum + (r.netSalary ?? 0), 0);
        const totalLatePenalty = rows.reduce((sum, r) => sum + (r.latePenalty ?? 0), 0);
        const totalAllowance = rows.reduce((sum, r) => sum + (r.allowance ?? 0), 0);
        return {
            totalNet,
            totalLatePenalty,
            totalAllowance,
            count: rows.length,
        };
    }, [rows]);

    const handleGenerate = async () => {
        if (!month || !year) {
            pushToast('Vui lòng chọn tháng/năm', 'error');
            return;
        }
        try {
            await autoGenerate({
                month,
                year,
                latePenaltyPerShift,
                latePenaltyPerHour,
                allowance,
                overwriteExisting,
            }).unwrap();
            pushToast('Đã tạo bảng lương');
            refetch();
        } catch {
            pushToast('Tạo bảng lương thất bại', 'error');
        }
    };

    return (
        <>
            <PageHeader
                title="Bảng lương"
                subtitle="Tính lương theo chấm công và hợp đồng"
                actions={
                    hasPermission('CREATE_PAYROLL') && (
                        <button
                            type="button"
                            className="btn btn-primary"
                            onClick={handleGenerate}
                            disabled={generating}
                        >
                            Tạo bảng lương
                        </button>
                    )
                }
            />

            <StatCards
                items={[
                    { label: 'Số nhân viên', value: stats.count },
                    { label: 'Tổng phụ cấp', value: formatMoney(stats.totalAllowance), tone: 'green' },
                    { label: 'Tổng phạt đi muộn', value: formatMoney(stats.totalLatePenalty), tone: 'red' },
                    { label: 'Tổng thực lãnh', value: formatMoney(stats.totalNet), tone: 'green' },
                ]}
            />

            <DataTable
                title={`Bảng lương tháng ${String(month).padStart(2, '0')}/${year}`}
                titleIcon={Wallet}
                isFetching={isFetching}
                toolbarRight={
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                        <input
                            className="field-input"
                            type="month"
                            value={monthPicker}
                            onChange={(e) => {
                                const value = e.target.value;
                                setMonthPicker(value);
                                if (value) {
                                    const [y, m] = value.split('-');
                                    setYear(Number(y));
                                    setMonth(Number(m));
                                }
                            }}
                            style={{ width: 170 }}
                        />
                        <input
                            className="field-input"
                            type="number"
                            min={0}
                            value={latePenaltyPerShift}
                            onChange={(e) => setLatePenaltyPerShift(Number(e.target.value))}
                            placeholder="Phạt muộn/ca"
                            style={{ width: 130 }}
                        />
                        <input
                            className="field-input"
                            type="number"
                            min={0}
                            value={latePenaltyPerHour}
                            onChange={(e) => setLatePenaltyPerHour(Number(e.target.value))}
                            placeholder="Phạt muộn/giờ (Tháng)"
                            style={{ width: 180 }}
                        />
                        <input
                            className="field-input"
                            type="number"
                            min={0}
                            value={allowance}
                            onChange={(e) => setAllowance(Number(e.target.value))}
                            placeholder="Phụ cấp"
                            style={{ width: 130 }}
                        />
                        <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--text-muted)' }}>
                            <input
                                type="checkbox"
                                checked={overwriteExisting}
                                onChange={(e) => setOverwriteExisting(e.target.checked)}
                            />
                            Ghi đè lương nháp
                        </label>
                        <button className="btn btn-ghost" onClick={() => refetch()} disabled={isLoading}>
                            Làm mới
                        </button>
                    </div>
                }
            >
                <table>
                    <thead>
                        <tr>
                            <th>Nhân viên</th>
                            <th>Lương cơ bản</th>
                            <th>Hệ số</th>
                            <th>Ca dự kiến</th>
                            <th>Ca thực tế</th>
                            <th>Giờ làm</th>
                            <th>Đi muộn</th>
                            <th>Phạt</th>
                            <th>Phụ cấp</th>
                            <th>Thực lãnh</th>
                            <th>Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array.from({ length: 6 }).map((_, i) => (
                                <tr key={i}>
                                    {Array.from({ length: 11 }).map((__, j) => (
                                        <td key={j}><div className="skeleton" style={{ height: 16, width: '80%' }} /></td>
                                    ))}
                                </tr>
                            ))
                        ) : rows.length === 0 ? (
                            <tr>
                                <td colSpan={11}>
                                    <div className="empty-state">
                                        <Wallet size={40} className="empty-icon" />
                                        <p className="empty-text">Chưa có dữ liệu bảng lương</p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            rows.map((row) => (
                                <tr key={row.id}>
                                    <td className="td-primary">{row.employeeName}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{formatMoney(row.baseSalary)}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{row.salaryCoefficient ?? '1.0'}</td>
                                    <td>{row.workingDays ?? 0}</td>
                                    <td>{row.actualWorkingDays ?? 0}</td>
                                    <td style={{ fontWeight: '500' }}>{row.workingHours ?? 0} giờ</td>
                                    <td>{row.lateCount ?? 0}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{formatMoney(row.latePenalty)}</td>
                                    <td style={{ color: 'var(--text-muted)' }}>{formatMoney(row.allowance)}</td>
                                    <td>{formatMoney(row.netSalary)}</td>
                                    <td><StatusBadge status={row.status} /></td>
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
