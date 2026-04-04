"use client"

import {useState} from "react";
import { type Order } from "@_types/order"

const statusStyle = (status: any) => {
    switch (status) {
        case "PAID": return { bg: "bg-emerald-50", text: "text-emerald-700", dot: "bg-emerald-500" };
        case "PENDING_PAYMENT": return { bg: "bg-amber-50", text: "text-amber-700", dot: "bg-amber-500" };
        case "PAYMENT_FAILED": return { bg: "bg-red-50", text: "text-red-600", dot: "bg-red-500" };
        default: return { bg: "bg-zinc-100", text: "text-zinc-600", dot: "bg-zinc-400" };
    }
}

const statusLabel = (s: any) => s.replace(/_/g, " ")
const fmt = (n: number) => `€${n.toFixed(2)}`;

export default function OrderRow({ order }: { order: Order }) {
    const [open, setOpen] = useState(false);
    const s = statusStyle(order.status);

    return (
        <div className="border-b border-zinc-50 last:border-0">
            <button
                onClick={() => setOpen(v => !v)}
                className="w-full flex items-center justify-between px-5 py-4 active:bg-zinc-50
                 hover:bg-zinc-50 dark:hover:bg-zinc-600 transition-colors group text-left"
            >
                <div className="min-w-0 mr-3">
                    <div className="flex flex-wrap items-center gap-x-2 gap-y-1 mb-0.5">
                        <span className="text-sm font-medium">{order.id}</span>
                        <span className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full ${s.bg} ${s.text}`}>
              <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${s.dot}`} />
                            {statusLabel(order.status)}
            </span>
                    </div>
                    <p className="text-xs text-zinc-400">
                        {new Date(order.date).toLocaleDateString()} &middot; {order.items.length} {order.items.length === 1 ? "item" : "items"}
                    </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                    <span className="text-sm font-medium">{fmt(order.totalAmount)}</span>
                    <svg
                        className={`w-4 h-4 text-zinc-300 group-hover:text-zinc-500 transition-all duration-200 ${open ? "rotate-90" : ""}`}
                        fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                    </svg>
                </div>
            </button>

            {open && (
                <div className="border-t border-zinc-100 fade-in">
                    <div className="px-5 pt-3 pb-1 space-y-3">
                        {order.items.map((item) => (
                            <div key={item.productId} className="flex items-center gap-3">
                                <img
                                    src={item.image.urls.sm}
                                    alt={item.image.altText}
                                    className="w-10 h-10 rounded-lg object-cover shrink-0 border border-zinc-100"
                                />
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm text-zinc-700 truncate">{item.productName}</p>
                                    <p className="text-xs text-zinc-400">Qty {item.quantity} &times; {fmt(item.price)}</p>
                                </div>
                                <span className="text-sm font-medium shrink-0">{fmt(item.totalAmount)}</span>
                            </div>
                        ))}
                    </div>

                    <div className="mx-5 mt-3 mb-4 pt-3 border-t border-zinc-200 space-y-1">
                        <div className="flex justify-between text-xs text-zinc-400">
                            <span>Shipping ({order.shippingType})</span>
                            <span>{order.shippingCost === 0 ? "Free" : fmt(order.shippingCost)}</span>
                        </div>
                        <div className="flex justify-between text-sm font-medium">
                            <span>Total</span>
                            <span>{fmt(order.totalAmount)}</span>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}