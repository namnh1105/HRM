'use client';

import React, { useState } from 'react';
import AppGlobalStyles from './AppGlobalStyles';
import MainSidebar from './MainSidebar';
import MainTopbar from './MainTopbar';

export default function MainShell({ children }: { children: React.ReactNode }) {
    const [collapsed, setCollapsed] = useState(false);

    return (
        <div className="hrm-shell">
            <AppGlobalStyles />
            <MainSidebar collapsed={collapsed} setCollapsed={setCollapsed} />
            <div className="hrm-content">
                <MainTopbar />
                <main className="hrm-main">
                    {children}
                </main>
            </div>
        </div>
    );
}
