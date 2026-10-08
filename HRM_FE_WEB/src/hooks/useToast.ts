'use client';

import { useState, useCallback } from 'react';

export function useToast() {
    const [toasts, setToasts] = useState<{ id: number; msg: string; type: 'success' | 'error' }[]>([]);
    const push = useCallback((msg: string, type: 'success' | 'error' = 'success') => {
        const id = Date.now();
        setToasts((p) => [...p, { id, msg, type }]);
        setTimeout(() => setToasts((p) => p.filter((t) => t.id !== id)), 3500);
    }, []);
    return { toasts, push };
}
