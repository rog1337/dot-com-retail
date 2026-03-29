"use client"

import Section from "@/src/app/(main)/account/Section"
import { useEffect, useState, useCallback } from "react"
import { logger } from "@lib/logger"
import { useToastStore } from "@store/toastStore"
import { accountApi } from "@lib/api/accountApi"
import { type Order } from "@_types/order"
import OrderRow from "@/src/app/(main)/account/OrderRow"
import Loading from "@components/Loading"
import {size} from "zod";
import {PageMetadata} from "@_types/page";

const ORDER_STATUSES = [
    { value: "", label: "All" },
    { value: "PENDING_PAYMENT", label: "Pending" },
    { value: "PAID",            label: "Paid" },
    { value: "CANCELLED",       label: "Cancelled" },
    { value: "REFUNDED",        label: "Refunded" },
    { value: "REFUND_PENDING",  label: "Refund Pending" },
]

type SortDir = "desc" | "asc"

export default function Orders() {
    const { show } = useToastStore()
    const [orders, setOrders]   = useState<Order[]>([])
    const [loading, setLoading] = useState(true)
    const [status, setStatus]   = useState("")
    const [sort, setSort]       = useState<SortDir>("desc")
    const [page, setPage]       = useState(0)
    const [meta, setMeta]       = useState<PageMetadata | null>(null)

    const fetchOrders = useCallback(async () => {
        setLoading(true)
        try {
            const params = new URLSearchParams()
            params.append("status", status)
            params.append("sort", sort)
            params.append("page", String(page))
            params.append("size", String(10))
            const res = await accountApi.getOrders(params)
            setOrders(res.content)
            setMeta(res.page)
        } catch (e: any) {
            show("Error loading orders", "error")
            logger.d("Error fetching orders", e)
        } finally {
            setLoading(false)
        }
    }, [status, sort, page])

    useEffect(() => { fetchOrders() }, [fetchOrders])

    const handleStatus = (s: string) => { setStatus(s); setPage(0) }
    const handleSort = (d: SortDir) => { setSort(d);   setPage(0) }

    return (
        <div className="fade-in">
            <Section title="Orders">
                <div className="flex flex-wrap items-center gap-2 px-5 py-3 border-b border-zinc-100 dark:border-zinc-700">
                    <div className="flex flex-wrap gap-1.5 flex-1">
                        {ORDER_STATUSES.map(s => (
                            <button
                                key={s.value}
                                onClick={() => handleStatus(s.value)}
                                className={`text-xs px-3 py-1 rounded-full border transition-colors ${
                                    status === s.value
                                        ? "bg-zinc-800 text-white border-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:border-zinc-100"
                                        : "border-zinc-200 text-zinc-500 hover:border-zinc-400 hover:text-zinc-700 dark:border-zinc-600 dark:text-zinc-400 dark:hover:border-zinc-400"
                                }`}
                            >
                                {s.label}
                            </button>
                        ))}
                    </div>

                    <button
                        onClick={() => handleSort(sort === "desc" ? "asc" : "desc")}
                        className="flex items-center gap-1 text-xs text-zinc-500 hover:text-zinc-700
                                   dark:text-zinc-400 dark:hover:text-zinc-200 transition-colors shrink-0"
                    >
                        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            {sort === "desc"
                                ? <path strokeLinecap="round" strokeLinejoin="round" d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12" />
                                : <path strokeLinecap="round" strokeLinejoin="round" d="M3 4h13M3 8h9m-9 4h6m4 0l4 4m0 0l-4 4m4-4H7" />
                            }
                        </svg>
                        {sort === "desc" ? "Newest first" : "Oldest first"}
                    </button>
                </div>

                {loading ? (
                    <Loading />
                ) : orders.length === 0 ? (
                    <div className="px-5 py-10 text-center text-sm text-zinc-400">
                        {status ? "No orders with this status." : "No orders yet."}
                    </div>
                ) : (
                    orders.map(order => <OrderRow key={order.id} order={order} />)
                )}

                {meta && meta.totalPages > 1 && (
                    <div className="flex items-center justify-between px-5 py-3 border-t border-zinc-100 dark:border-zinc-700">
                        <span className="text-xs text-zinc-400">
                            {meta.totalElements} orders &middot; page {meta.page + 1} of {meta.totalPages}
                        </span>
                        <div className="flex gap-1">
                            <button
                                disabled={meta.isFirst}
                                onClick={() => setPage(p => p - 1)}
                                className="p-1.5 rounded-md text-zinc-400 hover:text-zinc-700 hover:bg-zinc-100
                                           disabled:opacity-30 disabled:pointer-events-none
                                           dark:hover:text-zinc-200 dark:hover:bg-zinc-700 transition-colors"
                            >
                                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                                </svg>
                            </button>
                            <button
                                disabled={meta.isLast}
                                onClick={() => setPage(p => p + 1)}
                                className="p-1.5 rounded-md text-zinc-400 hover:text-zinc-700 hover:bg-zinc-100
                                           disabled:opacity-30 disabled:pointer-events-none
                                           dark:hover:text-zinc-200 dark:hover:bg-zinc-700 transition-colors"
                            >
                                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                                </svg>
                            </button>
                        </div>
                    </div>
                )}

            </Section>
        </div>
    )
}