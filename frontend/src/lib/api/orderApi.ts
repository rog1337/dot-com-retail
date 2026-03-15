import api from "@lib/api/api"
import {SubmitOrderRequest, CreateOrderResponse, Order} from "@_types/order"

export const orderPaths = {
    order: "/order",
    submit: "/order/submit"
}

export const orderApi = {
    getOrderByPaymentIntent: (params: URLSearchParams): Promise<Order> => {
        return api.get(orderPaths.order, { params })
    },
    submitOrder: (sessionId: string | null, request: SubmitOrderRequest): Promise<Order> => {
        const headers = sessionId ? { "X-Session-Id": sessionId } : {}
        return api.post(orderPaths.submit, request, { headers: headers })
    }
}