'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Building2, Mail, Lock, Eye, EyeOff, ArrowRight, AlertCircle } from 'lucide-react';
import { useLoginMutation } from '@/store/api/authApi';
import { useAppDispatch } from '@/store/hooks';
import { mapToUserInfo, setCredentials } from '@/store/features/authSlice';
import { saveTokens, setStoredUser } from '@/utils/tokenStorage';
import { getRoleCodes, hasRole } from '@/utils/roleUtils';

export default function LoginPage() {
    const router = useRouter();
    const dispatch = useAppDispatch();
    const [login, { isLoading }] = useLoginMutation();

    const [form, setForm] = useState({ email: '', password: '' });
    const [showPw, setShowPw] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        try {
            const res = await login({ email: form.email, password: form.password }).unwrap();
            if (res.success && res.data) {
                const mappedUser = mapToUserInfo(res.data.user);
                const roleCodes = getRoleCodes(mappedUser.roles);
                dispatch(setCredentials({
                    accessToken: res.data.accessToken,
                    refreshToken: res.data.refreshToken,
                    user: mappedUser,
                }));
                saveTokens(res.data.accessToken, res.data.refreshToken);
                setStoredUser(mappedUser);
                if (hasRole(roleCodes, 'ADMIN')) {
                    router.replace('/admin/dashboard');
                } else if (hasRole(roleCodes, 'HR')) {
                    router.replace('/hr/dashboard');
                } else {
                    router.replace('/403');
                }
            } else {
                setError(res.message || 'Đăng nhập thất bại');
            }
        } catch (err: unknown) {
            const msg =
                typeof err === 'object' &&
                err !== null &&
                'data' in err &&
                typeof (err as { data?: { message?: unknown } }).data?.message === 'string'
                    ? (err as { data: { message: string } }).data.message
                    : typeof err === 'object' && err !== null && 'error' in err && typeof (err as { error?: unknown }).error === 'string'
                        ? (err as { error: string }).error
                        : 'Email hoặc mật khẩu không đúng';
            setError(msg);
        }
    };

    return (
        <div className="relative min-h-screen bg-[#f8f9fa] text-[#0f172a] flex items-center justify-center px-4 py-12">
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-24 -left-20 w-[420px] h-[420px] rounded-full bg-emerald-200/40 blur-3xl" />
                <div className="absolute -bottom-24 -right-20 w-[420px] h-[420px] rounded-full bg-emerald-300/30 blur-3xl" />
            </div>

            <div className="relative w-full max-w-md">
                <div className="glass-panel rounded-2xl p-8 shadow-xl border border-white/50">
                    <div className="text-center mb-8">
                        <div className="mx-auto w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-600 to-emerald-400 flex items-center justify-center shadow-lg shadow-emerald-900/20 mb-4">
                            <Building2 size={20} className="text-white" />
                        </div>
                        <h1 className="text-xl font-black tracking-tight text-slate-900">GiaKhanh HRM</h1>
                        <p className="text-xs text-slate-500 mt-1">Đăng nhập vào hệ thống quản trị</p>
                    </div>

                    {error && (
                        <div className="flex items-center gap-2 bg-red-50 border border-red-200 text-red-600 rounded-lg px-3 py-2 mb-4">
                            <AlertCircle size={14} className="shrink-0" />
                            <p className="text-xs">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                        <div className="flex flex-col gap-1">
                            <label className="text-xs font-medium text-slate-600">Email</label>
                            <div className="relative">
                                <Mail size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                                <input
                                    id="login-email"
                                    type="email"
                                    autoComplete="email"
                                    placeholder="admin@company.com"
                                    value={form.email}
                                    onChange={e => setForm(p => ({ ...p, email: e.target.value }))}
                                    required
                                    className="w-full rounded-lg border border-slate-200 bg-white/80 px-9 py-2 text-sm text-slate-900 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-200"
                                />
                            </div>
                        </div>

                        <div className="flex flex-col gap-1">
                            <label className="text-xs font-medium text-slate-600">Mật khẩu</label>
                            <div className="relative">
                                <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                                <input
                                    id="login-password"
                                    type={showPw ? 'text' : 'password'}
                                    autoComplete="current-password"
                                    placeholder="••••••••"
                                    value={form.password}
                                    onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
                                    required
                                    className="w-full rounded-lg border border-slate-200 bg-white/80 px-9 py-2 pr-10 text-sm text-slate-900 outline-none transition focus:border-emerald-400 focus:ring-2 focus:ring-emerald-200"
                                />
                                <button
                                    type="button"
                                    id="toggle-password"
                                    onClick={() => setShowPw(p => !p)}
                                    className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-500 hover:text-emerald-600"
                                >
                                    {showPw ? <EyeOff size={15} /> : <Eye size={15} />}
                                </button>
                            </div>
                        </div>

                        <button
                            id="login-submit"
                            type="submit"
                            disabled={isLoading}
                            className="w-full rounded-lg bg-emerald-600 text-white font-semibold text-sm py-2.5 flex items-center justify-center gap-2 shadow-md shadow-emerald-900/20 transition hover:bg-emerald-700 disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            {isLoading ? 'Đang đăng nhập…' : (
                                <><span>Đăng nhập</span><ArrowRight size={16} /></>
                            )}
                        </button>
                    </form>

                    <p className="text-center text-[11px] text-slate-400 mt-6">
                        GiaKhanh © {new Date().getFullYear()} — Hệ thống quản lý nhân sự
                    </p>
                </div>
            </div>
        </div>
    );
}
