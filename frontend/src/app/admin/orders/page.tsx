"use client"

import Card from "@/src/components/admin/Card"
import { adminApi } from "@lib/api/adminApi"
import { useToastStore } from "@store/toastStore"
import { useMemo, useState } from "react"
import { AdminOrder } from "@_types/admin"
import TabButton from "@components/admin/TabButton"
import { OrderStatus, ShippingType } from "@_types/order"

export default function OrdersPage() {
  const ORDER_STATUSES: Array<{ value: OrderStatus; label: string }> = useMemo(
    () => [
      {
        value: OrderStatus.PENDING_PAYMENT,
        label: "Pending payment",
      },
      { value: OrderStatus.PAID, label: "Paid" },
      { value: OrderStatus.FAILED, label: "Failed" },
      { value: OrderStatus.SHIPPED, label: "Shipped" },
      { value: OrderStatus.DELIVERED, label: "Delivered" },
      { value: OrderStatus.CANCELLED, label: "Cancelled" },
      { value: OrderStatus.REFUND_PENDING, label: "Refund pending" },
      { value: OrderStatus.REFUNDED, label: "Refunded" },
      { value: OrderStatus.REFUND_FAILED, label: "Refund failed" },
    ],
    [],
  )

  const SHIPPING_TYPES: Array<{
    value: "STANDARD" | "EXPRESS"
    label: string
  }> = useMemo(
    () => [
      { value: "STANDARD", label: "Standard" },
      { value: "EXPRESS", label: "Express" },
    ],
    [],
  )

  const [activeTab, setActiveTab] = useState<"orders" | "refunds">("orders")
  const [orderIdInput, setOrderIdInput] = useState<string>("")
  const [order, setOrder] = useState<AdminOrder | null>(null)
  const [orderLoading, setOrderLoading] = useState(false)

  const [orderNewStatus, setOrderNewStatus] = useState<OrderStatus>(OrderStatus.PENDING_PAYMENT)
  const [orderNewShippingType, setOrderNewShippingType] = useState<ShippingType>(
    ShippingType.STANDARD,
  )
  const [orderCancelReason, setOrderCancelReason] = useState<string>("")
  const [refundCause, setRefundCause] = useState<string>("")
  const { show } = useToastStore()

  const loadOrder = async () => {
    const id = orderIdInput.trim()
    if (!id) {
      show("Order id is required", "error")
      return
    }
    setOrderLoading(true)
    try {
      const res = await adminApi.getOrder(id)
      setOrder(res)
      setOrderNewStatus(res.status)
      setOrderNewShippingType(res.shippingType ?? ShippingType.STANDARD)
      show("Order loaded", "success")
    } catch {
      show("Failed loading order", "error")
    } finally {
      setOrderLoading(false)
    }
  }

  const updateOrder = async () => {
    if (!orderIdInput.trim()) {
      show("Order id is required", "error")
      return
    }
    try {
      await adminApi.updateOrder(orderIdInput.trim(), {
        shippingType: orderNewShippingType,
        status: OrderStatus[orderNewStatus],
      })
      show("Order updated", "success")
      await loadOrder()
    } catch {
      show("Failed updating order", "error")
    }
  }

  const cancelOrder = async () => {
    if (!orderIdInput.trim()) {
      show("Order id is required", "error")
      return
    }
    const reason = orderCancelReason.trim()
    if (!reason) {
      show("Cancellation reason is required", "error")
      return
    }
    try {
      await adminApi.cancelOrder(orderIdInput.trim(), reason)
      show("Order cancellation requested")
      setOrderCancelReason("")
      await loadOrder()
      show("Order cancelled")
    } catch {
      show("Failed cancelling order", "error")
    }
  }

  const refundOrder = async () => {
    const id = orderIdInput.trim()
    if (!id) {
      show("Order id is required", "error")
      return
    }
    try {
      await adminApi.refundOrder(id, refundCause.trim() || undefined)
      show("Refund initiated")
    } catch (e) {
      if (e?.response?.data?.code === "REFUND_ORDER_ILLEGAL_STATE") {
        show(`${e?.response?.data?.detail}`, "error", 10000)
        return
      }
      show("Refund failed", "error")
    }
  }

  return (
    <div>
      <TabButton active={activeTab === "orders"} onClick={() => setActiveTab("orders")}>
        Orders
      </TabButton>
      <TabButton active={activeTab === "refunds"} onClick={() => setActiveTab("refunds")}>
        Refunds
      </TabButton>

      {activeTab === "orders" && (
        <Card title="Orders & Shipping">
          <div className="grid grid-cols-2 items-end gap-3">
            <label className="space-y-1">
              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                Order ID (UUID)
              </div>
              <input
                className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                value={orderIdInput}
                onChange={(e) => setOrderIdInput(e.target.value)}
                placeholder="e.g. 3d9b1b8c-...."
              />
            </label>

            <button
              type="button"
              onClick={loadOrder}
              className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
            >
              {orderLoading ? "Loading..." : "Load Order"}
            </button>
          </div>

          {order && (
            <div className="mt-5 space-y-4">
              <div className="grid grid-cols-3 gap-3">
                <div className="rounded-lg border border-gray-200 p-3 text-xs">
                  <div className="tracking-widest text-gray-400 uppercase">Status</div>
                  <div className="mt-1 font-semibold">{order.status}</div>
                </div>
                <div className="rounded-lg border border-gray-200 p-3 text-xs">
                  <div className="tracking-widest text-gray-400 uppercase">Shipping</div>
                  <div className="mt-1 font-semibold">{order.shippingType ?? "—"}</div>
                </div>
                <div className="rounded-lg border border-gray-200 p-3 text-xs">
                  <div className="tracking-widest text-gray-400 uppercase">Total</div>
                  <div className="mt-1 font-semibold">€{order.totalAmount}</div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <label className="space-y-1">
                  <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                    New status
                  </div>
                  <select
                    className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                    value={orderNewStatus}
                    onChange={(e) => setOrderNewStatus(e.target.value as unknown as OrderStatus)}
                  >
                    {ORDER_STATUSES.map((s) => (
                      <option key={s.value} value={s.value}>
                        {s.label}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="space-y-1">
                  <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                    Delivery option
                  </div>
                  <select
                    className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                    value={orderNewShippingType}
                    onChange={(e) => setOrderNewShippingType(e.target.value as ShippingType)}
                  >
                    {SHIPPING_TYPES.map((s) => (
                      <option key={s.value} value={s.value}>
                        {s.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={updateOrder}
                  className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
                >
                  Update order
                </button>

                <div className="flex-1" />

                <label className="w-full space-y-1 md:w-1/2">
                  <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                    Cancel reason
                  </div>
                  <input
                    className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                    value={orderCancelReason}
                    onChange={(e) => setOrderCancelReason(e.target.value)}
                    placeholder="Reason (required for cancel)"
                  />
                </label>

                <button
                  type="button"
                  onClick={cancelOrder}
                  className="rounded-lg border border-red-200 px-4 py-2 text-xs text-red-600 hover:bg-red-50"
                >
                  Cancel order
                </button>
              </div>
              <p className="text-xs">The user will be notified via email on order update/cancel</p>

              <div className="border-t border-gray-200 pt-4">
                <div className="mb-2 text-xs tracking-widest text-gray-500 uppercase">Items</div>
                <div className="space-y-2">
                  {order.items.map((it, idx) => (
                    <div
                      key={idx}
                      className="flex justify-between rounded-lg border border-gray-100 px-3 py-2 text-xs"
                    >
                      <div className="truncate pr-3">{it.productName}</div>
                      <div className="text-gray-500">
                        x{it.quantity} @ €{it.price}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </Card>
      )}

      {activeTab === "refunds" && (
        <Card title="Refunds (Payment refund endpoint)">
          <div className="grid grid-cols-2 items-end gap-3">
            <label className="col-span-2 space-y-1">
              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                Order ID (UUID)
              </div>
              <input
                className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                value={orderIdInput}
                onChange={(e) => setOrderIdInput(e.target.value)}
                placeholder="Paste an order UUID"
              />
            </label>

            <label className="col-span-2 space-y-1">
              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                Reason (optional)
              </div>
              <input
                className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                value={refundCause}
                onChange={(e) => setRefundCause(e.target.value)}
                placeholder="Refund reason"
              />
            </label>
          </div>

          <div className="mt-4 flex justify-end">
            <button
              type="button"
              onClick={refundOrder}
              className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
            >
              Request refund
            </button>
          </div>
        </Card>
      )}
    </div>
  )
}
