export function parseNumberOrNull(v: string): number | null {
    const t = v.trim()
    if (!t) return null
    const n = Number(t)
    return Number.isFinite(n) ? n : null
}

export function parseIntOrNull(v: string): number | null {
    const n = parseNumberOrNull(v)
    if (n === null) return null
    return Number.isInteger(n) ? n : Math.trunc(n)
}