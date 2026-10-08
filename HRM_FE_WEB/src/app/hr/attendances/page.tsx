'use client';

import React, { useEffect, useState } from 'react';
import { ClipboardList } from 'lucide-react';
import {
    useGetAttendancesByStoreHistoryQuery,
    useGetEmployeesByStoreQuery,
    useGetStoresQuery,
} from '@/store/api/hrApi';
import PageHeader from '@/components/ui/PageHeader';
import DataTable from '@/components/ui/DataTable';

type AttendanceRow = {
    id?: string;
    employeeId?: string;
    employeeName?: string;
    workDate?: string;
    checkInTime?: string;
    checkOutTime?: string;
    status?: string;
};

function formatDate(val: string | undefined) {
    if (!val) return '—';
    const d = new Date(val);
    if (isNaN(d.getTime())) return val;
    return d.toLocaleDateString('vi-VN');
}

function formatTime(val: string | undefined) {
    if (!val) return '—';
    // HH:mm:ss or full datetime
    if (/^\d{2}:\d{2}/.test(val)) return val.slice(0, 5);
    const d = new Date(val);
    if (isNaN(d.getTime())) return val;
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function StatusBadge({ status }: { status?: string }) {
    const s = (status ?? '').toUpperCase();
    if (s === 'PRESENT' || s === 'ON_TIME') return <span className="badge badge-green">Đúng giờ</span>;
    if (s === 'LATE') return <span className="badge badge-amber">Đi muộn</span>;
    if (s === 'ABSENT') return <span className="badge badge-red">Vắng mặt</span>;
    if (s === 'EARLY_LEAVE') return <span className="badge badge-amber">Về sớm</span>;
    if (!status) return <span style={{ color: 'var(--text-muted)' }}>—</span>;
    return <span className="badge badge-gray">{status}</span>;
}

export default function AttendancesPage() {
    const now = new Date();
    const [month, setMonth] = useState<number>(now.getMonth() + 1);
    const [year, setYear] = useState<number>(now.getFullYear());
    const [monthPicker, setMonthPicker] = useState<string>(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`);

    const { data: storesData } = useGetStoresQuery();
    const stores = (storesData as any)?.data ?? [];
    const [storeId, setStoreId] = useState<string>('');

    useEffect(() => {
        if (!storeId && stores.length > 0) {
            setStoreId(stores[0].id);
        }
    }, [stores, storeId]);

    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const daysInMonth = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${String(daysInMonth).padStart(2, '0')}`;

    const { data: employeesData } = useGetEmployeesByStoreQuery(
        { storeId, page: 0, size: 500 },
        { skip: !storeId }
    );
    const employees = (employeesData as any)?.data ?? [];

    const { data: attendanceData, isFetching } = useGetAttendancesByStoreHistoryQuery(
        { storeId, startDate, endDate, page: 0, size: 5000 },
        { skip: !storeId || !startDate || !endDate }
    );
    const attendances: AttendanceRow[] = (attendanceData as any)?.data ?? [];

    const daysArray = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    // Group attendances by employee and date
    const attendanceMap = React.useMemo(() => {
        const map: Record<string, Record<string, AttendanceRow[]>> = {};
        for (const att of attendances) {
            if (!att.employeeId && !att.employeeName) continue;
            // Depending on the exact DTO, employeeId might be mapped or we can use employeeName as fallback
            const empId = att.employeeId || att.employeeName || 'unknown';
            if (!map[empId]) map[empId] = {};
            if (att.workDate) {
                const dateKey = att.workDate.slice(0, 10);
                if (!map[empId][dateKey]) map[empId][dateKey] = [];
                map[empId][dateKey].push(att);
            }
        }
        return map;
    }, [attendances]);

    const employeesToRender = React.useMemo(() => {
        const map = new Map<string, any>();
        employees.forEach((e: any) => map.set(e.id, e));
        attendances.forEach(att => {
            if (att.employeeId && !map.has(att.employeeId)) {
                map.set(att.employeeId, { id: att.employeeId, fullName: att.employeeName || 'Nhân viên' });
            }
        });
        return Array.from(map.values());
    }, [employees, attendances]);

    return (
        <>
            <PageHeader
                title="Bảng chấm công"
                subtitle="Theo dõi chi tiết giờ vào/ra theo từng cơ sở."
            />

            <DataTable
                title={`Tháng ${String(month).padStart(2, '0')}/${year}`}
                titleIcon={ClipboardList}
                isFetching={isFetching}
                toolbarRight={
                    <div className="field-group" style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                        <select
                            className="field-input"
                            value={storeId}
                            onChange={(e) => setStoreId(e.target.value)}
                            style={{ minWidth: 180 }}
                        >
                            {stores.length === 0 && <option value="">Chưa có chi nhánh</option>}
                            {stores.map((s: any) => (
                                <option key={s.id} value={s.id}>{s.name}</option>
                            ))}
                        </select>
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
                            style={{ width: 150 }}
                        />
                    </div>
                }
            >
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ whiteSpace: 'nowrap', fontSize: 12 }}>
                        <thead>
                            <tr>
                                <th style={{ position: 'sticky', left: 0, zIndex: 1, background: 'var(--surface)' }}>Nhân viên</th>
                                {daysArray.map(d => (
                                    <th key={d} style={{ textAlign: 'center', width: 35, padding: '8px 4px' }}>{d}</th>
                                ))}
                                <th style={{ textAlign: 'center', color: 'var(--color-green)' }}>✓</th>
                                <th style={{ textAlign: 'center', color: 'var(--color-amber)' }}>U</th>
                                <th style={{ textAlign: 'center', color: 'var(--color-red)' }}>V</th>
                                <th style={{ textAlign: 'center', color: 'var(--color-blue)' }}>P</th>
                            </tr>
                        </thead>
                        <tbody>
                            {employeesToRender.length === 0 ? (
                                <tr>
                                    <td colSpan={daysInMonth + 5}>
                                        <div className="empty-state">
                                            <ClipboardList size={40} className="empty-icon" />
                                            <p className="empty-text">Không có nhân viên nào</p>
                                        </div>
                                    </td>
                                </tr>
                            ) : (
                                employeesToRender.map((emp: any) => {
                                    const empId = emp.id || emp.fullName || 'unknown';
                                    const empAttendances = attendanceMap[empId] || {};
                                    let countPresent = 0;
                                    let countLate = 0;
                                    let countAbsent = 0;
                                    let countPermission = 0;

                                    return (
                                        <tr key={emp.id}>
                                            <td style={{ position: 'sticky', left: 0, background: 'var(--surface)', fontWeight: 500 }} className="td-primary">
                                                {emp.fullName || emp.email}
                                            </td>
                                            {daysArray.map(d => {
                                                const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
                                                const atts = empAttendances[dateStr] || [];
                                                
                                                if (atts.length === 0) {
                                                    return (
                                                        <td key={d} style={{ padding: 4, minWidth: 60 }}>
                                                            <div style={{ textAlign: 'center', padding: 4, color: 'var(--text-muted)' }}>—</div>
                                                        </td>
                                                    );
                                                }

                                                return (
                                                    <td key={d} style={{ padding: 4, minWidth: 60, verticalAlign: 'top' }}>
                                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                                            {atts.map((att, i) => {
                                                                let cellContent: React.ReactNode = '—';
                                                                let cellStyle: React.CSSProperties = { textAlign: 'center', padding: '4px 6px', color: 'var(--text-muted)' };
                                                                const status = (att.status || '').toUpperCase();
                                                                
                                                                if (status === 'ABSENT') {
                                                                    countAbsent++;
                                                                    cellContent = 'V';
                                                                    cellStyle = { ...cellStyle, color: 'white', background: 'var(--color-red)', borderRadius: 4 };
                                                                } else if (status.includes('LEAVE') && !status.includes('EARLY')) {
                                                                    countPermission++;
                                                                    cellContent = 'P';
                                                                    cellStyle = { ...cellStyle, color: 'white', background: 'var(--color-blue)', borderRadius: 4 };
                                                                } else {
                                                                    const isLate = status === 'LATE';
                                                                    const isPresent = status === 'ON_TIME' || status === 'PRESENT';
                                                                    
                                                                    if (isLate) countLate++;
                                                                    else countPresent++; 

                                                                    if (att.checkInTime || att.checkOutTime) {
                                                                        const inTime = formatTime(att.checkInTime);
                                                                        const outTime = att.checkOutTime ? formatTime(att.checkOutTime) : '...';
                                                                        cellContent = `${inTime} - ${outTime}`;
                                                                    } else {
                                                                        cellContent = '✓';
                                                                    }

                                                                    if (isLate || status === 'EARLY_LEAVE') {
                                                                        cellStyle = { ...cellStyle, color: 'var(--color-amber)', background: 'rgba(245, 158, 11, 0.15)', borderRadius: 4, fontWeight: 600, fontSize: 11, whiteSpace: 'nowrap' };
                                                                    } else if (isPresent) {
                                                                        cellStyle = { ...cellStyle, color: 'var(--color-green)', background: 'rgba(16, 185, 129, 0.15)', borderRadius: 4, fontWeight: 600, fontSize: 11, whiteSpace: 'nowrap' };
                                                                    }
                                                                }

                                                                return (
                                                                    <div key={i} style={cellStyle}>
                                                                        {cellContent}
                                                                    </div>
                                                                );
                                                            })}
                                                        </div>
                                                    </td>
                                                );
                                            })}
                                            <td style={{ textAlign: 'center', fontWeight: 'bold', color: 'var(--color-green)' }}>{countPresent}</td>
                                            <td style={{ textAlign: 'center', fontWeight: 'bold', color: 'var(--color-amber)' }}>{countLate}</td>
                                            <td style={{ textAlign: 'center', fontWeight: 'bold', color: 'var(--color-red)' }}>{countAbsent}</td>
                                            <td style={{ textAlign: 'center', fontWeight: 'bold', color: 'var(--color-blue)' }}>{countPermission}</td>
                                        </tr>
                                    );
                                })
                            )}
                        </tbody>
                    </table>
                </div>
            </DataTable>
        </>
    );
}
