'use client';

import React from 'react';
import { Search } from 'lucide-react';

export type FilterOption<T extends string = string> = {
    id: T;
    label: string;
};

export function FilterPills<T extends string>({
    options,
    value,
    onChange,
}: {
    options: FilterOption<T>[];
    value: T;
    onChange: (value: T) => void;
}) {
    return (
        <>
            {options.map((opt) => (
                <button
                    key={opt.id}
                    className={`btn btn-sm ${value === opt.id ? 'btn-primary' : 'btn-ghost'}`}
                    type="button"
                    onClick={() => onChange(opt.id)}
                >
                    {opt.label}
                </button>
            ))}
        </>
    );
}

export function SearchBox({
    value,
    onChange,
    placeholder = 'Tìm kiếm…',
    id,
    className,
    wrapperStyle,
    inputStyle,
}: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    id?: string;
    className?: string;
    wrapperStyle?: React.CSSProperties;
    inputStyle?: React.CSSProperties;
}) {
    return (
        <div className={`toolbar-search${className ? ` ${className}` : ''}`} style={wrapperStyle}>
            <Search size={14} className="toolbar-search-icon" />
            <input
                id={id}
                className="toolbar-search-input"
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                style={inputStyle}
            />
        </div>
    );
}

export function PageInfo({ text }: { text: string }) {
    return <span className="page-info">{text}</span>;
}
