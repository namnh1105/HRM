'use client';

import React from 'react';
import { UserCheck, UserX } from 'lucide-react';

export default function ToastStack({
    toasts,
    withIcons = true,
}: {
    toasts: { id: number; msg: string; type: 'success' | 'error' }[];
    withIcons?: boolean;
}) {
    if (!toasts.length) return null;
    return (
        <div className="toast-container">
            {toasts.map((t) => (
                <div key={t.id} className={`toast toast-${t.type}`}>
                    {withIcons ? (t.type === 'success' ? <UserCheck size={16} /> : <UserX size={16} />) : null}
                    {t.msg}
                </div>
            ))}
        </div>
    );
}
