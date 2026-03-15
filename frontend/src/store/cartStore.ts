import { create } from "zustand"
import { persist } from "zustand/middleware"
import {Cart, CartItem} from "@_types/cart"
import {ShippingType} from "@_types/order"

export const useCartStore = create<CartStore>()(
    persist(
        (set, get) => ({
            items: [],
            shippingType: ShippingType.STANDARD,
            shippingCost: 0,
            subTotal: 0,
            total: 0,
            totalQuantity: 0,
            isLoading: false,

            setItems: (items: CartItem[]) => set({ items }),
            setShippingCost: (type: ShippingType) => set((state) => {
                const shippingCost = type === ShippingType.STANDARD ? 5 : 15
                const total = state.total - state.shippingCost + shippingCost
                return { shippingCost, total }
            }),
            setShippingType: (type: ShippingType) => set({ shippingType: type }),
            setSubTotal: (subTotal: number) => set({ subTotal: subTotal }),
            setTotal: (total: number) => set({ total: total }),
            setTotalQuantity: (totalQuantity: number) => set({ totalQuantity: totalQuantity }),

            setQuantity: (id: number, quantity: number) => set((state) => ({
                items: state.items.map((i) => i.productId === id ? { ...i, quantity } : i),
            })),

            getItemQuantity: (id: number) => get().items.find(i => i.productId === id)?.quantity || null,

            removeItem: (id) => set((state) => ({
                items: state.items.filter((i) => i.productId !== id),
            })),

            updateQuantity: (id, quantity) => set((state) => ({
                items: quantity <= 0
                    ? state.items.filter((i) => i.productId !== id)
                    : state.items.map((i) => i.productId === id ? { ...i, quantity } : i),
            })),

            clearCart: () => set({ items: [] }),
            itemCount: () => get().items.reduce((sum, i) => sum + i.quantity, 0),

            setCart: (cart: Cart) => set((state) => {
                const orderMap = new Map<number, number>(
                    state.items.map((item, index) => [item.productId, index])
                )

                const sorted = [...cart.items].sort((a, b) => {
                    const posA = orderMap.get(a.productId) ?? Infinity
                    const posB = orderMap.get(b.productId) ?? Infinity
                    return posA - posB
                })

                return {
                    items: sorted,
                    shippingType: cart.shippingType ?? ShippingType.STANDARD,
                    shippingCost: cart.shippingCost,
                    subTotal: cart.subTotalPrice,
                    total: cart.totalPrice,
                    totalQuantity: cart.totalQuantity
                }
            }),
            setIsLoading: (isLoading: boolean) => set({ isLoading }),
        }),
        {
            name: "cart-storage",
            partialize: (state) => ({ isLoading: false }),
        }
    ),
)

export interface CartStore {
    items: CartItem[]
    setItems: (items: CartItem[]) => void
    shippingType: ShippingType
    setShippingType: (type: ShippingType) => void
    setShippingCost: (type: ShippingType) => void
    shippingCost: number
    subTotal: number
    setSubTotal: (subTotal: number) => void
    total: number
    setTotal: (total: number) => void
    totalQuantity: number
    setTotalQuantity: (totalQuantity: number) => void
    setCart: (cart: Cart) => void
    setQuantity: (id: number, quantity: number) => void
    getItemQuantity: (id: number) => number | null
    removeItem: (id: number) => void
    updateQuantity: (id: number, quantity: number) => void
    clearCart: () => void
    itemCount: () => number
    isLoading: boolean
    setIsLoading: (isLoading: boolean) => void
}