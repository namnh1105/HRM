'use client';

import React from 'react';

export default function AppGlobalStyles() {
    return (
        <style jsx global>{`
            *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

            :root {
                --sidebar-w: 240px;
                --sidebar-collapsed-w: 64px;
                --topbar-h: 60px;

                --bg-base: #f8fafc;
                --bg-surface: #ffffff;
                --bg-card: #ffffff;
                --bg-card-hover: #f1f5f9;
                --bg-elevated: #f1f5f9;

                --border: rgba(15,23,42,0.08);
                --border-accent: rgba(16,185,129,0.35);

                --accent: #3b82f6;
                --accent-light: #60a5fa;
                --accent-glow: rgba(59,130,246,0.16);

                --text-primary: #0f172a;
                --text-secondary: #334155;
                --text-muted: #64748b;

                --green: #22c55e;
                --red: #ef4444;
                --amber: #f59e0b;

                --radius: 12px;
                --radius-sm: 8px;
                --shadow: 0 6px 30px rgba(15,23,42,0.08);

                --font: 'Inter', system-ui, sans-serif;
                --transition: 0.2s ease;
            }

            html, body { height: 100%; font-family: var(--font); background: var(--bg-base); color: var(--text-primary); }

            .hrm-shell {
                display: flex;
                height: 100vh;
                overflow: hidden;
            }

            .hrm-sidebar {
                position: relative;
                width: var(--sidebar-w);
                min-width: var(--sidebar-w);
                background: #0f172a;
                border-right: 1px solid rgba(255,255,255,0.05);
                display: flex;
                flex-direction: column;
                transition: width var(--transition), min-width var(--transition);
                overflow: hidden;
                z-index: 20;
            }
            .hrm-sidebar.collapsed {
                width: var(--sidebar-collapsed-w);
                min-width: var(--sidebar-collapsed-w);
            }

            .sidebar-brand {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 20px 18px 16px;
                border-bottom: 1px solid rgba(255,255,255,0.08);
                white-space: nowrap;
                overflow: hidden;
            }
            .brand-icon { color: var(--accent); flex-shrink: 0; }
            .brand-text {
                font-size: 17px;
                font-weight: 700;
                background: linear-gradient(135deg, #818cf8, #c084fc);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
                letter-spacing: -0.3px;
            }

            .sidebar-nav {
                flex: 1;
                padding: 12px 8px;
                display: flex;
                flex-direction: column;
                gap: 2px;
                overflow-y: auto;
                overflow-x: hidden;
            }

            .nav-section-label {
                font-size: 10px;
                font-weight: 600;
                letter-spacing: 0.08em;
                color: rgba(148,163,184,0.7);
                text-transform: uppercase;
                padding: 12px 10px 4px;
                white-space: nowrap;
            }
            .nav-divider {
                border: none;
                border-top: 1px solid rgba(255,255,255,0.08);
                margin: 8px 4px;
            }

            .nav-item {
                position: relative;
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 9px 10px;
                border-radius: var(--radius-sm);
                color: #94a3b8;
                text-decoration: none;
                font-size: 14px;
                font-weight: 500;
                cursor: pointer;
                border: none;
                background: none;
                width: 100%;
                white-space: nowrap;
                overflow: hidden;
                transition: background var(--transition), color var(--transition);
            }
            .nav-item:hover { background: rgba(255,255,255,0.05); color: #ffffff; }
            .nav-item.active {
                background: rgba(59,130,246,0.2);
                color: #ffffff;
            }
            .nav-active-bar {
                position: absolute;
                right: 0; top: 20%; bottom: 20%;
                width: 3px;
                background: var(--accent);
                border-radius: 4px 0 0 4px;
            }
            .nav-icon { flex-shrink: 0; }
            .nav-label { flex: 1; }
            .nav-badge {
                font-size: 10px;
                padding: 1px 6px;
                background: var(--accent);
                color: #fff;
                border-radius: 10px;
                font-weight: 600;
            }

            .sidebar-footer {
                padding: 8px;
                border-top: 1px solid rgba(255,255,255,0.08);
                display: flex;
                flex-direction: column;
                gap: 4px;
            }
            .user-pill {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 8px 10px;
                border-radius: var(--radius-sm);
                overflow: hidden;
                white-space: nowrap;
            }
            .user-avatar {
                width: 30px; height: 30px;
                border-radius: 50%;
                background: rgba(59,130,246,0.2);
                border: 1px solid rgba(59,130,246,0.4);
                display: flex; align-items: center; justify-content: center;
                font-size: 12px; font-weight: 700;
                color: var(--accent-light);
                flex-shrink: 0;
            }
            .user-info { overflow: hidden; }
            .user-email { font-size: 12px; font-weight: 500; color: #f8fafc; overflow: hidden; text-overflow: ellipsis; }
            .user-role { font-size: 11px; color: #94a3b8; }
            .logout-btn { color: var(--red) !important; }
            .logout-btn:hover { background: rgba(239,68,68,0.1) !important; }

            .collapse-btn {
                position: absolute;
                top: 50%;
                right: -12px;
                transform: translateY(-50%);
                width: 24px; height: 24px;
                border-radius: 50%;
                background: #0f172a;
                border: 1px solid rgba(255,255,255,0.1);
                color: #94a3b8;
                display: flex; align-items: center; justify-content: center;
                cursor: pointer;
                z-index: 10;
                transition: background var(--transition), color var(--transition);
            }
            .collapse-btn:hover { background: rgba(59,130,246,0.2); color: var(--accent-light); }

            .hrm-content {
                flex: 1;
                display: flex;
                flex-direction: column;
                min-width: 0;
                overflow: hidden;
            }

            .hrm-topbar {
                height: var(--topbar-h);
                background: var(--bg-surface);
                border-bottom: 1px solid var(--border);
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 0 24px;
                gap: 16px;
                flex-shrink: 0;
            }
            .topbar-search {
                position: relative;
                display: flex;
                align-items: center;
            }
            .search-icon { position: absolute; left: 12px; color: var(--text-muted); pointer-events: none; }
            .search-input {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius-sm);
                color: var(--text-primary);
                font-size: 13px;
                padding: 7px 14px 7px 36px;
                width: 260px;
                outline: none;
                transition: border var(--transition), box-shadow var(--transition);
            }
            .search-input::placeholder { color: var(--text-muted); }
            .search-input:focus { border-color: var(--border-accent); box-shadow: 0 0 0 3px var(--accent-glow); }

            .topbar-actions { display: flex; align-items: center; gap: 8px; }
            .topbar-btn {
                width: 36px; height: 36px;
                border-radius: var(--radius-sm);
                background: #ffffff;
                border: 1px solid var(--border);
                color: var(--text-secondary);
                display: flex; align-items: center; justify-content: center;
                cursor: pointer;
                transition: background var(--transition), color var(--transition);
            }
            .topbar-btn:hover { background: var(--bg-elevated); color: var(--text-primary); }

            .hrm-main {
                flex: 1;
                overflow-y: auto;
                padding: 28px;
                background: var(--bg-base);
            }

            .page-header {
                display: flex;
                align-items: flex-start;
                justify-content: space-between;
                margin-bottom: 24px;
                gap: 16px;
                flex-wrap: wrap;
            }
            .page-title {
                font-size: 22px;
                font-weight: 700;
                color: var(--text-primary);
                line-height: 1.2;
            }
            .page-subtitle {
                font-size: 13px;
                color: var(--text-muted);
                margin-top: 2px;
            }

            .stat-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                gap: 16px;
                margin-bottom: 28px;
            }
            .stat-card {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius);
                padding: 20px;
                display: flex;
                flex-direction: column;
                gap: 8px;
                box-shadow: var(--shadow);
            }
            .stat-label { font-size: 12px; color: var(--text-muted); font-weight: 500; text-transform: uppercase; letter-spacing: 0.04em; }
            .stat-value { font-size: 26px; font-weight: 700; color: var(--text-primary); }
            .stat-sub { font-size: 12px; color: var(--text-muted); }

            .table-card {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius);
                overflow: hidden;
                box-shadow: var(--shadow);
            }
            .table-toolbar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 16px 20px;
                border-bottom: 1px solid var(--border);
                gap: 12px;
                flex-wrap: wrap;
            }
            .toolbar-left, .toolbar-right {
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .table-title { font-size: 15px; font-weight: 600; color: var(--text-primary); }

            table { width: 100%; border-collapse: collapse; }
            thead { background: #f1f5f9; }
            th {
                text-align: left;
                font-size: 11px;
                font-weight: 600;
                letter-spacing: 0.06em;
                text-transform: uppercase;
                color: var(--text-muted);
                padding: 12px 16px;
                border-bottom: 1px solid var(--border);
                white-space: nowrap;
            }
            td {
                padding: 13px 16px;
                font-size: 13px;
                color: var(--text-secondary);
                border-bottom: 1px solid var(--border);
                vertical-align: middle;
            }
            tr:last-child td { border-bottom: none; }
            tbody tr:hover { background: #f8fafc; }
            .td-primary { color: var(--text-primary); font-weight: 500; }

            .badge {
                display: inline-flex;
                align-items: center;
                gap: 4px;
                padding: 3px 8px;
                border-radius: 20px;
                font-size: 11px;
                font-weight: 600;
            }
            .badge-green { background: rgba(34,197,94,0.12); color: #16a34a; border: 1px solid rgba(34,197,94,0.25); }
            .badge-red { background: rgba(239,68,68,0.12); color: #dc2626; border: 1px solid rgba(239,68,68,0.25); }
            .badge-amber { background: rgba(245,158,11,0.12); color: #d97706; border: 1px solid rgba(245,158,11,0.25); }
            .badge-blue { background: rgba(16,185,129,0.12); color: var(--accent); border: 1px solid rgba(16,185,129,0.25); }
            .badge-gray { background: rgba(148,163,184,0.1); color: var(--text-muted); border: 1px solid var(--border); }

            .role-list { display: flex; flex-wrap: wrap; gap: 4px; }

            .btn {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 8px 14px;
                border-radius: var(--radius-sm);
                font-size: 13px;
                font-weight: 500;
                cursor: pointer;
                border: none;
                transition: background var(--transition), transform 0.1s;
                white-space: nowrap;
            }
            .btn:active { transform: scale(0.97); }
            .btn-primary { background: var(--accent); color: #fff; }
            .btn-primary:hover { background: #0ea371; }
            .btn-ghost { background: #f8fafc; color: var(--text-secondary); border: 1px solid var(--border); }
            .btn-ghost:hover { background: #f1f5f9; color: var(--text-primary); }
            .btn-danger { background: rgba(239,68,68,0.15); color: #f87171; border: 1px solid rgba(239,68,68,0.3); }
            .btn-danger:hover { background: rgba(239,68,68,0.25); }
            .btn-icon {
                padding: 6px;
                width: 30px; height: 30px;
                justify-content: center;
            }
            .btn-sm { padding: 5px 10px; font-size: 12px; }

            .field-group { display: flex; flex-direction: column; gap: 6px; }
            .field-label { font-size: 12px; font-weight: 500; color: var(--text-secondary); }
            .field-input {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius-sm);
                color: var(--text-primary);
                font-size: 13px;
                padding: 9px 12px;
                outline: none;
                width: 100%;
                transition: border var(--transition), box-shadow var(--transition);
            }
            .field-input::placeholder { color: var(--text-muted); }
            .field-input:focus { border-color: var(--border-accent); box-shadow: 0 0 0 3px var(--accent-glow); }

            .toolbar-search {
                position: relative;
                display: flex;
                align-items: center;
            }
            .toolbar-search-icon { position: absolute; left: 10px; color: var(--text-muted); pointer-events: none; }
            .toolbar-search-input {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius-sm);
                color: var(--text-primary);
                font-size: 13px;
                padding: 7px 12px 7px 32px;
                width: 220px;
                outline: none;
                transition: border var(--transition), box-shadow var(--transition);
            }
            .toolbar-search-input::placeholder { color: var(--text-muted); }
            .toolbar-search-input:focus { border-color: var(--border-accent); box-shadow: 0 0 0 3px var(--accent-glow); }

            .modal-overlay {
                position: fixed;
                inset: 0;
                background: rgba(15,23,42,0.4);
                backdrop-filter: blur(6px);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 100;
                padding: 20px;
            }
            .modal {
                background: #ffffff;
                border: 1px solid var(--border);
                border-radius: var(--radius);
                padding: 28px;
                width: 100%;
                max-width: 480px;
                box-shadow: var(--shadow);
                display: flex;
                flex-direction: column;
                gap: 20px;
            }
            .modal-wide { max-width: 700px; }
            .modal-header { display: flex; align-items: center; justify-content: space-between; }
            .modal-title { font-size: 17px; font-weight: 700; color: var(--text-primary); }
            .modal-body { display: flex; flex-direction: column; gap: 16px; }
            .modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding-top: 4px; border-top: 1px solid var(--border); }

            .skeleton {
                background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
                background-size: 400% 100%;
                animation: shimmer 1.4s ease infinite;
                border-radius: 4px;
            }
            @keyframes shimmer {
                0% { background-position: 100% 0; }
                100% { background-position: -100% 0; }
            }

            .empty-state {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 12px;
                padding: 60px 20px;
                color: var(--text-muted);
            }
            .empty-icon { opacity: 0.3; }
            .empty-text { font-size: 14px; }

            .pagination {
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 14px 20px;
                border-top: 1px solid var(--border);
                justify-content: flex-end;
            }
            .page-btn {
                width: 32px; height: 32px;
                border-radius: var(--radius-sm);
                background: #ffffff;
                border: 1px solid var(--border);
                color: var(--text-secondary);
                display: flex; align-items: center; justify-content: center;
                cursor: pointer;
                font-size: 13px;
                transition: background var(--transition);
            }
            .page-btn:hover { background: var(--accent-glow); color: var(--accent); }
            .page-btn.active { background: var(--accent); color: #fff; border-color: var(--accent); }
            .page-info { font-size: 12px; color: var(--text-muted); margin-right: auto; }

            .table-pagination-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                flex-wrap: wrap;
                gap: 12px;
                padding: 12px 20px;
                border-top: 1px solid var(--border);
                background: #fafbfc;
            }
            .table-pagination-info {
                font-size: 12px;
                color: var(--text-muted);
            }
            .table-pagination-nav {
                display: flex;
                align-items: center;
                gap: 4px;
                flex-wrap: wrap;
            }
            .table-pagination-btn {
                min-width: 32px;
                height: 32px;
                padding: 0 6px;
                border-radius: 6px;
                background: #ffffff;
                border: 1px solid var(--border);
                color: var(--text-secondary);
                display: inline-flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                font-size: 13px;
                font-weight: 500;
                transition: background var(--transition), color var(--transition), border-color var(--transition);
            }
            .table-pagination-btn:hover:not(:disabled) {
                background: var(--bg-elevated);
                color: var(--accent);
                border-color: rgba(59, 130, 246, 0.4);
            }
            .table-pagination-btn:disabled {
                opacity: 0.35;
                cursor: not-allowed;
            }
            .table-pagination-btn--num { min-width: 32px; }
            .table-pagination-btn.is-active {
                background: #ffffff;
                color: #1677ff;
                border-color: #1677ff;
                font-weight: 600;
            }
            .table-pagination-ellipsis {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                min-width: 24px;
                color: var(--text-muted);
                font-size: 13px;
                user-select: none;
            }
            /* Page size selector */
            .table-pagination-sizer {
                position: relative;
                display: inline-flex;
                align-items: center;
                margin-left: 8px;
            }
            .table-pagination-sizer-select {
                appearance: none;
                -webkit-appearance: none;
                height: 32px;
                padding: 0 28px 0 10px;
                border-radius: 6px;
                background: #ffffff;
                border: 1px solid var(--border);
                color: var(--text-secondary);
                font-size: 12px;
                font-weight: 500;
                cursor: pointer;
                outline: none;
                transition: border-color var(--transition), box-shadow var(--transition);
                white-space: nowrap;
            }
            .table-pagination-sizer-select:hover {
                border-color: rgba(59,130,246,0.4);
                color: var(--text-primary);
            }
            .table-pagination-sizer-select:focus {
                border-color: #1677ff;
                box-shadow: 0 0 0 2px rgba(22,119,255,0.15);
            }
            .table-pagination-sizer-icon {
                position: absolute;
                right: 8px;
                pointer-events: none;
                color: var(--text-muted);
            }

            .toast-container {
                position: fixed;
                bottom: 24px;
                right: 24px;
                display: flex;
                flex-direction: column;
                gap: 10px;
                z-index: 200;
            }
            .toast {
                display: flex;
                align-items: center;
                gap: 10px;
                padding: 12px 18px;
                border-radius: var(--radius-sm);
                font-size: 13px;
                font-weight: 500;
                color: #fff;
                box-shadow: var(--shadow);
                animation: slide-up 0.3s ease;
            }
            .toast-success { background: #0f766e; border: 1px solid #34d39940; }
            .toast-error { background: #b91c1c; border: 1px solid #ef444440; }
            @keyframes slide-up {
                from { transform: translateY(20px); opacity: 0; }
                to { transform: translateY(0); opacity: 1; }
            }

            ::-webkit-scrollbar { width: 6px; }
            ::-webkit-scrollbar-track { background: transparent; }
            ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
            ::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
        `}</style>
    );
}
