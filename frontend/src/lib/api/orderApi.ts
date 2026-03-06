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
    createOrder: (sessionId: string | null): Promise<CreateOrderResponse> => {
        const headers = sessionId ? { "X-Session-Id": sessionId } : {}
        return api.post(orderPaths.order, null, { headers: headers })
    },
    submitOrder: (sessionId: string | null, request: SubmitOrderRequest): Promise<Order> => {
        const headers = sessionId ? { "X-Session-Id": sessionId } : {}
        return api.post(orderPaths.submit, request, { headers: headers })
    }
}