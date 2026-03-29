import {orderApi} from "@lib/api/orderApi"
import {redirect} from "next/navigation"
import OrderItem from "@components/order/OrderItem"
import ClearCart from "@/src/app/(main)/checkout/confirm/ClearCart";
import NotFound from "next/dist/client/components/builtin/not-found";

type SearchParams = {
    payment_intent?: string
    redirect_status?: string
}

const failedStatus = "failed"
const successStatus = "success"

export default async function CheckoutConfirm({ searchParams }: { searchParams: SearchParams }) {
    const { payment_intent, redirect_status } = await searchParams
    if (!payment_intent || !redirect_status) redirect("/")

    if (redirect_status === failedStatus) {
        redirect("/cart?redirect_status=failed")
    }

    const params = new URLSearchParams()
    params.append("paymentIntentId", payment_intent)

    const order = await fetchOrder()


    async function fetchOrder(retries: number = 5, delay: number = 1000) {
        try {
            return await orderApi.getOrderByPaymentIntent(params)
        } catch(e: any) {
            const code = e?.response?.data?.code
            if (code === "ORDER_NOT_FOUND") {
                if (retries === 0) {
                    return null
                }
            } else {
                if (retries === 0) throw Error("Failed to fetch order", e)
            }

            await new Promise(resolve => setTimeout(resolve, delay))
            return fetchOrder(retries-1, delay)
        }
    }

    if (!order) return NotFound()

    return (

        <div
            className="flex flex-col items-center justify-center mt-20 mb-10 max-w-3/4"
        >
            <span className="text-2xl">Order successful</span>

            <div
                className="p-4 border-2 border-gray-200 rounded-md shadow-sm mt-10"
            >
                {order.items.map((item) => (
                    <OrderItem key={item.productId} item={item} />
                ))}

                <div className="flex flex-row justify-between border-b-2 border-gray-200 py-5">
                    <span>Shipping - {order.shippingType}</span>
                    <span>€{order.shippingCost.toFixed(2)}</span>
                </div>

                <div className="flex flex-row justify-between border-b-2 border-gray-200 py-5">
                    <span>Total</span>
                    <span>€{order.totalAmount.toFixed(2)}</span>
                </div>

                <div className="flex flex-col p-4">
                    <span>Email: {order.contact.email}</span>
                    <span>Phone: {order.contact.phone}</span>
                    <span>Name: {order.contact.name}</span>
                    <span>StreetLine1: {order.contact.address.streetLine1}</span>
                    <span>StreetLine2: {order.contact.address.streetLine2}</span>
                    <span>City: {order.contact.address.city}</span>
                    <span>State or province: {order.contact.address.stateOrProvince}</span>
                    <span>Postal code: {order.contact.address.postalCode}</span>
                    <span>Country: {order.contact.address.country}</span>
                </div>

                <ClearCart/>
            </div>
        </div>
    )
}