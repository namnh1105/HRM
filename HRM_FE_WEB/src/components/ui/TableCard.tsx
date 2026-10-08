'use client';

import React from 'react';
import type { LucideIcon } from 'lucide-react';

export default function TableCard({
    title,
    titleIcon: TitleIcon,
    loadingText,
    isFetching,
    toolbarRight,
    children,
    footer,
}: {
    title: string;
    titleIcon?: LucideIcon;
    isFetching?: boolean;
    loadingText?: string;
    toolbarRight?: React.ReactNode;
    children: React.ReactNode;
    footer?: React.ReactNode;
}) {
    return (
        <div className="table-card">
            <div className="table-toolbar">
                <div className="toolbar-left">
                    <p className="table-title">
                        {TitleIcon ? <TitleIcon size={16} style={{ display: 'inline', marginRight: 6 }} /> : null}
                        {title}
                        {isFetching ? (
                            <span style={{ fontSize: 11, color: 'var(--text-muted)', marginLeft: 8 }}>
                                {loadingText ?? 'Đang tải...'}
                            </span>
                        ) : null}
                    </p>
                </div>
                {toolbarRight ? <div className="toolbar-right">{toolbarRight}</div> : null}
            </div>
            {children}
            {footer ? footer : null}
        </div>
    );
}
