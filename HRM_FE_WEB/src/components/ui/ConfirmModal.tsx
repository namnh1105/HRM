'use client';

import React from 'react';
import { X } from 'lucide-react';

export default function ConfirmModal({
    title,
    desc,
    onConfirm,
    onClose,
    danger = false,
    confirmLabel = 'Xác nhận',
}: {
    title: string;
    desc: string;
    onConfirm: () => void;
    onClose: () => void;
    danger?: boolean;
    confirmLabel?: string;
}) {
    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" style={{ maxWidth: 400 }} onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <p className="modal-title">{title}</p>
                    <button type="button" className="btn btn-icon btn-ghost" onClick={onClose}>
                        <X size={16} />
                    </button>
                </div>
                <p style={{ fontSize: 13, color: 'var(--text-muted)', lineHeight: 1.6 }}>{desc}</p>
                <div className="modal-footer">
                    <button type="button" className="btn btn-ghost" onClick={onClose}>
                        Hủy
                    </button>
                    <button
                        type="button"
                        className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`}
                        id="confirm-action-btn"
                        onClick={() => {
                            onConfirm();
                            onClose();
                        }}
                    >
                        {confirmLabel}
                    </button>
                </div>
            </div>
        </div>
    );
}
