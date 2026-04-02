"use client"

import NotFound from "next/dist/client/components/builtin/not-found"

export default function Error({ error }: { error: unknown }) {
    return NotFound()
}