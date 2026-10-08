'use client';
import React, { useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/store/hooks';
import { getRoleCodes, hasRole } from '@/utils/roleUtils';
import MainShell from '@/components/MainShell';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
    const router = useRouter();
    const user = useAppSelector((s) => s.auth?.user);
    const roleCodes = useMemo(() => getRoleCodes(user?.roles), [user?.roles]);
    const hasAdminRole = useMemo(() => hasRole(roleCodes, 'ADMIN'), [roleCodes]);
    const forbidden = Boolean(user) && !hasAdminRole;

    useEffect(() => {
        if (forbidden) {
            router.replace('/403');
        }
    }, [forbidden, router]);

    if (forbidden) return null;

    return <MainShell>{children}</MainShell>;
}
