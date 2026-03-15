import api from "@lib/api/api"
import {Cart, CartUpdateRequest, CheckoutResponse} from "@_types/cart"

export const cartPaths = {
    base: "/cart",
    checkout: () => cartPaths.base + "/checkout",
}

export const cartApi = {
    getCart: (sessionId: string | null): Promise<Cart> => {
        const headers = sessionId ? { "X-Session-ID": sessionId } : {}
        return api.get(cartPaths.base, { headers: headers })
    },
    updateCart: (sessionId: string | null, request: CartUpdateRequest): Promise<Cart> => {
        const headers = sessionId ? { "X-Session-ID": sessionId } : {}
        return api.put(cartPaths.base, request, { headers: headers })
    },
    checkout: (sessionId: string | null): Promise<CheckoutResponse> => {
        const headers = sessionId ? { "X-Session-Id": sessionId } : {}
        return api.post(cartPaths.checkout(), null, { headers: headers })
    },
}