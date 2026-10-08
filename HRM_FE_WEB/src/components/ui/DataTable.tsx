'use client';

import React from 'react';
import type { LucideIcon } from 'lucide-react';
import TableCard from '@/components/ui/TableCard';
import TablePagination, { type TablePaginationProps } from '@/components/ui/TablePagination';

export type DataTableProps = {
    title: string;
    titleIcon?: LucideIcon;
    isFetching?: boolean;
    loadingText?: string;
    toolbarRight?: React.ReactNode;
    children: React.ReactNode;
    /** Truyền `null` / bỏ qua để không hiển thị thanh phân trang */
    pagination?: Pick<
        TablePaginationProps,
        | 'page'
        | 'totalPages'
        | 'totalItems'
        | 'itemLabel'
        | 'onPageChange'
        | 'pageSize'
        | 'onPageSizeChange'
        | 'siblingCount'
    > | null;
};

/**
 * Vỏ bảng dùng chung: toolbar (tiêu đề + `toolbarRight`) + nội dung + phân trang.
 * Nội dung là `<table>…</table>` trực tiếp (với thead/tbody, skeleton, empty-state).
 */
export default function DataTable({
    title,
    titleIcon,
    isFetching,
    loadingText,
    toolbarRight,
    children,
    pagination,
}: DataTableProps) {
    const footer =
        pagination && pagination.totalPages >= 1 ? (
            <TablePagination
                page={pagination.page}
                totalPages={pagination.totalPages}
                totalItems={pagination.totalItems}
                itemLabel={pagination.itemLabel}
                onPageChange={pagination.onPageChange}
                pageSize={pagination.pageSize}
                onPageSizeChange={pagination.onPageSizeChange}
                siblingCount={pagination.siblingCount}
            />
        ) : null;

    return (
        <TableCard
            title={title}
            titleIcon={titleIcon}
            isFetching={isFetching}
            loadingText={loadingText}
            toolbarRight={toolbarRight}
            footer={footer}
        >
            {children}
        </TableCard>
    );
}
