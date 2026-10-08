'use client';
import React, { useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { mapToUserInfo, restoreAuth } from '@/store/features/authSlice';
import { clearStoredUser, clearTokens, getStoredUser, restoreAccessToken, getRefreshToken } from '@/utils/tokenStorage';
import { getRoleCodes, hasRole } from '@/utils/roleUtils';
import { logout } from '@/store/features/authSlice';

export default function AuthProvider({ children }: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const router = useRouter();
    const pathname = usePathname();
    const [isInitialized, setIsInitialized] = useState(false);
    const { isAuthenticated, user } = useAppSelector((s) => s.auth || { isAuthenticated: false, user: null });

    useEffect(() => {
        const accessToken = restoreAccessToken();
        const refreshToken = getRefreshToken();
        const storedUser = getStoredUser();

        if (accessToken && storedUser) {
            const user = mapToUserInfo(storedUser);
            dispatch(restoreAuth({ accessToken, refreshToken, user }));
        }
        setIsInitialized(true);
    }, [dispatch]);

    useEffect(() => {
        if (!isInitialized) return;

        const isLoginPage = pathname === '/login';
        const isRootPage = pathname === '/' || pathname === '/dashboard';
        const isProtectedPage = pathname.startsWith('/admin') || pathname.startsWith('/hr');

        if (isAuthenticated && (isLoginPage || isRootPage)) {
            const roleCodes = getRoleCodes(user?.roles);
            if (hasRole(roleCodes, 'ADMIN')) router.replace('/admin/dashboard');
            else if (hasRole(roleCodes, 'HR')) router.replace('/hr/dashboard');
            else {
                // Token/user state exists but doesn't map to any supported app area.
                // Keep /login usable so user can re-auth as another account.
                if (isLoginPage || isRootPage) {
                    clearTokens();
                    clearStoredUser();
                    dispatch(logout());
                    router.replace('/login');
                } else {
                    router.replace('/403');
                }
            }
        } else if (!isAuthenticated && isProtectedPage && pathname !== '/login') {
            // Anonymous user shouldn't be on protected pages
            router.replace('/login');
        }
    }, [isInitialized, isAuthenticated, pathname, router, user?.roles]);

    // Don't flash protected content before initialization
    if (!isInitialized) {
        return (
            <div style={{
                height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
                background: '#0d0f14', color: '#f1f5f9', fontFamily: 'Inter, sans-serif'
            }}>
                <div className="loader"></div>
                <style jsx>{`
                    .loader {
                        width: 40px; height: 40px;
                        border: 3px solid rgba(99,102,241,0.1);
                        border-top-color: #6366f1;
                        border-radius: 50%;
                        animation: spin 1s linear infinite;
                    }
                    @keyframes spin { to { transform: rotate(360deg); } }
                `}</style>
            </div>
        );
    }

    return <>{children}</>;
}
