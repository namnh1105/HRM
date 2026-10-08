'use client';

import React from 'react';

export type StatCardItem = {
    label: string;
    value: React.ReactNode;
    tone?: 'default' | 'green' | 'amber' | 'red';
    sub?: React.ReactNode;
};

const toneColor: Record<NonNullable<StatCardItem['tone']>, string | undefined> = {
    default: undefined,
    green: 'var(--green)',
    amber: 'var(--amber)',
    red: 'var(--red)',
};

export default function StatCards({ items }: { items: StatCardItem[] }) {
    if (!items.length) return null;
    return (
        <div className="stat-grid">
            {items.map((item, idx) => (
                <div key={idx} className="stat-card">
                    <p className="stat-label">{item.label}</p>
                    <p className="stat-value" style={item.tone ? { color: toneColor[item.tone] } : undefined}>
                        {item.value}
                    </p>
                    {item.sub ? <p className="stat-sub">{item.sub}</p> : null}
                </div>
            ))}
        </div>
    );
}
