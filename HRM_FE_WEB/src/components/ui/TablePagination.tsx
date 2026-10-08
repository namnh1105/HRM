'use client';

import React from 'react';
import { ChevronLeft, ChevronRight, ChevronDown } from 'lucide-react';
import { getPaginationItems } from '@/utils/pagination';

export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const;
export type PageSizeOption = (typeof PAGE_SIZE_OPTIONS)[number];

export type TablePaginationProps = {
    /** Trang hiện tại, 0-based */
    page: number;
    totalPages: number;
    totalItems: number;
    itemLabel: string;
    onPageChange: (nextPage: number) => void;
    /** Số bản ghi mỗi trang (default: 10) */
    pageSize?: number;
    /** Callback khi thay đổi số bản ghi/trang */
    onPageSizeChange?: (size: number) => void;
    siblingCount?: number;
    className?: string;
};

export default function TablePagination({
    page,
    totalPages,
    totalItems,
    itemLabel,
    onPageChange,
    pageSize = 10,
    onPageSizeChange,
    siblingCount = 1,
    className,
}: TablePaginationProps) {
    if (totalPages < 1) return null;

    const items = getPaginationItems(page, totalPages, siblingCount);
    const last = totalPages - 1;

    return (
        <div className={`table-pagination-row${className ? ` ${className}` : ''}`}>
            {/* Info bên trái */}
            <span className="table-pagination-info">
                {totalItems} {itemLabel}
            </span>

            {/* Nav + Page size selector bên phải */}
            <div className="table-pagination-nav" role="navigation" aria-label="Phân trang">
                {/* Prev */}
                <button
                    type="button"
                    className="table-pagination-btn"
                    disabled={page <= 0}
                    onClick={() => onPageChange(page - 1)}
                    aria-label="Trang trước"
                >
                    <ChevronLeft size={14} strokeWidth={2} />
                </button>

                {/* Page numbers */}
                {items.map((item, idx) =>
                    item === 'ellipsis' ? (
                        <span key={`e-${idx}`} className="table-pagination-ellipsis" aria-hidden>
                            …
                        </span>
                    ) : (
                        <button
                            key={item}
                            type="button"
                            className={`table-pagination-btn table-pagination-btn--num${item === page ? ' is-active' : ''}`}
                            onClick={() => onPageChange(item)}
                            aria-label={`Trang ${item + 1}`}
                            aria-current={item === page ? 'page' : undefined}
                        >
                            {item + 1}
                        </button>
                    )
                )}

                {/* Next */}
                <button
                    type="button"
                    className="table-pagination-btn"
                    disabled={page >= last}
                    onClick={() => onPageChange(page + 1)}
                    aria-label="Trang sau"
                >
                    <ChevronRight size={14} strokeWidth={2} />
                </button>

                {/* Page size selector */}
                {onPageSizeChange && (
                    <div className="table-pagination-sizer">
                        <select
                            className="table-pagination-sizer-select"
                            value={pageSize}
                            onChange={(e) => {
                                onPageSizeChange(Number(e.target.value));
                                onPageChange(0); // reset về trang 1
                            }}
                            aria-label="Số bản ghi mỗi trang"
                        >
                            {PAGE_SIZE_OPTIONS.map((s) => (
                                <option key={s} value={s}>
                                    {s} / trang
                                </option>
                            ))}
                        </select>
                        <ChevronDown size={12} className="table-pagination-sizer-icon" />
                    </div>
                )}
            </div>
        </div>
    );
}
