export type PageIndexItem = number | 'ellipsis';

/**
 * `page` và các số trả về là chỉ số 0-based (0 … totalPages - 1).
 */
export function getPaginationItems(page: number, totalPages: number, siblingCount = 1): PageIndexItem[] {
    if (totalPages < 1) return [];
    if (totalPages === 1) return [0];

    const first = 0;
    const last = totalPages - 1;
    const left = Math.max(first + 1, page - siblingCount);
    const right = Math.min(last - 1, page + siblingCount);

    const set = new Set<number>([first, last, page]);
    for (let i = left; i <= right; i++) {
        if (i > first && i < last) set.add(i);
    }

    const sorted = [...set].sort((a, b) => a - b);
    const out: PageIndexItem[] = [];
    for (let i = 0; i < sorted.length; i++) {
        if (i > 0) {
            const prev = sorted[i - 1];
            const cur = sorted[i];
            if (cur - prev > 1) out.push('ellipsis');
        }
        out.push(sorted[i]);
    }
    return out;
}
