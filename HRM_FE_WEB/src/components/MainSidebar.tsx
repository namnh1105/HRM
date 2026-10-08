'use client';

import React, { useMemo } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
    Users,
    Shield,
    Key,
    LayoutDashboard,
    LogOut,
    ChevronLeft,
    ChevronRight,
    Building2,
    Clock3,
    CalendarCheck2,
    FileText,
    Wallet,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { logout } from '@/store/features/authSlice';
import { clearTokens, clearStoredUser } from '@/utils/tokenStorage';
import { getRoleCodes, hasRole } from '@/utils/roleUtils';

type NavItem = {
    label: string;
    href: string;
    icon: LucideIcon;
    roles?: string[];
    section?: string;
    badge?: string;
};

const NAV_ITEMS: NavItem[] = [
    // ADMIN SECTION
    {
        label: 'Admin Dashboard',
        href: '/admin/dashboard',
        icon: LayoutDashboard,
        roles: ['ADMIN'],
        section: 'Quản trị hệ thống',
    },
    {
        label: 'Tài khoản',
        href: '/admin/accounts',
        icon: Users,
        roles: ['ADMIN'],
        section: 'Quản trị hệ thống',
    },
    {
        label: 'Vai trò',
        href: '/admin/roles',
        icon: Shield,
        roles: ['ADMIN'],
        section: 'Quản trị hệ thống',
    },
    {
        label: 'Quyền hạn',
        href: '/admin/permissions',
        icon: Key,
        roles: ['ADMIN'],
        section: 'Quản trị hệ thống',
    },

    // HR SECTION
    {
        label: 'HR Dashboard',
        href: '/hr/dashboard',
        icon: LayoutDashboard,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Nhân viên',
        href: '/hr/employees',
        icon: Users,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Chi nhánh',
        href: '/hr/stores',
        icon: Building2,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Chấm công',
        href: '/hr/attendances',
        icon: Clock3,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Ca làm',
        href: '/hr/work-shifts',
        icon: CalendarCheck2,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Nghỉ phép',
        href: '/hr/leave-requests',
        icon: Key,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Hợp đồng',
        href: '/hr/contracts',
        icon: FileText,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
    {
        label: 'Bảng lương',
        href: '/hr/payrolls',
        icon: Wallet,
        roles: ['HR'],
        section: 'Quản lý nhân sự',
    },
];

interface MainSidebarProps {
    collapsed: boolean;
    setCollapsed: (collapsed: boolean) => void;
}

export default function MainSidebar({ collapsed, setCollapsed }: MainSidebarProps) {
    const pathname = usePathname();
    const dispatch = useAppDispatch();
    const user = useAppSelector((s) => s.auth?.user);
    const roleCodes = useMemo(() => getRoleCodes(user?.roles), [user?.roles]);

    const filteredItems = useMemo(() => {
        return NAV_ITEMS.filter((item) => {
            if (!item.roles) return true;
            return item.roles.some((role) => hasRole(roleCodes, role));
        });
    }, [roleCodes]);

    // Group items by section
    const groupedItems = useMemo(() => {
        const sections: { title: string; items: NavItem[] }[] = [];
        filteredItems.forEach((item) => {
            const sectionTitle = item.section || 'Chung';
            let section = sections.find((s) => s.title === sectionTitle);
            if (!section) {
                section = { title: sectionTitle, items: [] };
                sections.push(section);
            }
            section.items.push(item);
        });
        return sections;
    }, [filteredItems]);

    const handleLogout = () => {
        dispatch(logout());
        clearTokens();
        clearStoredUser();
    };

    const isAdmin = hasRole(roleCodes, 'ADMIN');
    const isHr = hasRole(roleCodes, 'HR');

    return (
        <aside className={`hrm-sidebar ${collapsed ? 'collapsed' : ''}`}>
            {/* Logo */}
            <div className="sidebar-brand">
                <Building2 size={22} className="brand-icon" />
                {!collapsed && <span className="brand-text">GiaKhanh</span>}
            </div>

            {/* Nav */}
            <nav className="sidebar-nav">
                {groupedItems.map((section, sIdx) => (
                    <React.Fragment key={section.title}>
                        {!collapsed ? (
                            <p className="nav-section-label">{section.title}</p>
                        ) : (
                            sIdx > 0 && <hr className="nav-divider" />
                        )}
                        {section.items.map((item) => {
                            const Icon = item.icon;
                            const active = pathname === item.href || pathname.startsWith(item.href + '/');
                            return (
                                <Link key={item.href} href={item.href} className={`nav-item ${active ? 'active' : ''}`}>
                                    <Icon size={18} className="nav-icon" />
                                    {!collapsed && <span className="nav-label">{item.label}</span>}
                                    {!collapsed && item.badge && <span className="nav-badge">{item.badge}</span>}
                                    {active && <span className="nav-active-bar" />}
                                </Link>
                            );
                        })}
                    </React.Fragment>
                ))}
            </nav>

            {/* User pill */}
            <div className="sidebar-footer">
                {!collapsed && (
                    <div className="user-pill">
                        <div className="user-avatar">
                            {user?.email?.[0]?.toUpperCase() ?? 'U'}
                        </div>
                        <div className="user-info">
                            <p className="user-email">{user?.email ?? 'user'}</p>
                            <p className="user-role">
                                {roleCodes.length > 0 ? roleCodes.join(', ') : 'User'}
                            </p>
                        </div>
                    </div>
                )}
                <button className="nav-item logout-btn" onClick={handleLogout} title="Đăng xuất">
                    <LogOut size={18} className="nav-icon" />
                    {!collapsed && <span className="nav-label">Đăng xuất</span>}
                </button>
            </div>

            {/* Collapse toggle */}
            <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
                {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
            </button>
        </aside>
    );
}
