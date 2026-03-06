import api from "@lib/api/api"
import {Cart, CartUpdateRequest} from "@_types/cart"

export const cartPaths = {
    base: "/cart",
}

export const cartApi = {
    getCart: (sessionId: string | null): Promise<Cart> => {
        const headers = sessionId ? { "X-Session-ID": sessionId } : {}
        return api.get(cartPaths.base, { headers: headers })
    },
    updateCart: (sessionId: string | null, request: CartUpdateRequest): Promise<Cart> => {
        const headers = sessionId ? { "X-Session-ID": sessionId } : {}
        return api.put(cartPaths.base, request, { headers: headers })
    }
}