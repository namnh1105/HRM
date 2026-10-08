'use client';

import React from 'react';
import { FilterPills } from '@/components/ui/ToolbarControls';

export type EntityFilterStatus = 'all' | 'active' | 'inactive' | 'deleted';

const OPTIONS: { id: EntityFilterStatus; label: string }[] = [
    { id: 'all', label: 'Tất cả' },
    { id: 'active', label: 'Hoạt động' },
    { id: 'inactive', label: 'Không hoạt động' },
    { id: 'deleted', label: 'Đã xóa' },
];

export default function EntityStatusFilters({
    value,
    onChange,
}: {
    value: EntityFilterStatus;
    onChange: (v: EntityFilterStatus) => void;
}) {
    return <FilterPills options={OPTIONS} value={value} onChange={onChange} />;
}
