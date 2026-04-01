"use client"

import Image from "next/image"
import { CartItem as CartProduct} from "@_types/cart"
import {useCartStore} from "@store/cartStore"
import {cartApi} from "@lib/api/cartApi"
import {useAuth} from "@lib/auth/authContext"
import {useState} from "react"
import {logger} from "@lib/logger"
import {useToastStore} from "@store/toastStore"

export default function CartItem({item}: {item: CartProduct }) {
    const { sessionId } = useAuth()
    const { items, setQuantity, setCart, removeItem, isLoading, setIsLoading } = useCartStore()
    const { show } = useToastStore()
    const [isQuantityMaxed, setIsQuantityMaxed] = useState(false)

    const onIncrease = async (productId: number) => {
        setIsLoading(true)
        try {
            const quantity = item.quantity + 1
            setQuantity(productId, quantity)

            const cartUpdateRequest = {
                items: items.map(i => {
                    if (i.productId === productId) { i.quantity = quantity } return i
                })
            }
            const cart = await cartApi.updateCart(sessionId, cartUpdateRequest)
            setCart(cart)
        } catch(e: any) {
            const code = e.response?.data?.code
            if (code === "PRODUCT_INSUFFICIENT_STOCK") {
                setIsQuantityMaxed(true)
                show("Can't add more items", "error")
                logger.d("Not enough stock", e)
            } else {
                console.log("Error updating cart: ", e)
            }
            setQuantity(productId, item.quantity-1)
        }
        setIsLoading(false)
    }

    const onDecrease = async (productId: number) => {
        setIsLoading(true)
        if (item.quantity === 1) return onRemove(productId)

        try {
            const quantity = item.quantity - 1
            setQuantity(productId, quantity)

            const cartUpdateRequest = {
                items: items.map(i => {
                    if (i.productId === productId) { i.quantity = quantity } return i
                })
            }
            const cart = await cartApi.updateCart(sessionId, cartUpdateRequest)
            setCart(cart)
            setIsQuantityMaxed(false)
        } catch(e: any) {
            console.log("Error decrementing quantity: ", e)
        }
        setIsLoading(false)
    }

    const onRemove = async (productId: number) => {
        setIsLoading(true)
        try {
            removeItem(productId)
            const cartUpdateRequest = {
                items: items.filter(i => i.productId !== productId)
            }
            const cart = await cartApi.updateCart(sessionId, cartUpdateRequest)
            setCart(cart)
        } catch(e: any) {
            console.log("Error removing item: ", e)
        }
        setIsLoading(false)
    }


    return (
        <div className="flex items-center gap-4 py-4 border-b border-gray-100 last:border-0">
            <div className="relative w-16 h-16 rounded-lg overflow-hidden shrink-0">
                <Image
                    src={item.image.urls.sm}
                    alt={item.image.altText}
                    fill
                    className="object-cover"
                />
            </div>

            <div className="flex-1 min-w-0 text-foreground">
                <p className="font-medium">{item.productName}</p>
                <p className="mt-0.5">
                    €{(item.price * item.quantity).toFixed(2)}
                </p>
            </div>

            <div className="flex items-center gap-2 shrink-0">
                <button
                    onClick={() => onDecrease(item.productId)}
                    className="w-7 h-7 rounded-full border border-gray-200 flex items-center
                     justify-center text-foreground hover:border-foreground/60 transition-colors
                      leading-none disabled:opacity-50 disabled:cursor-not-allowed"
                    aria-label="Decrease quantity"
                    disabled={isLoading}
                >
                    −
                </button>
                <span className="w-5 text-center text-foreground font-medium">{item.quantity}</span>
                <button
                    onClick={() => onIncrease(item.productId)}
                    className="w-7 h-7 rounded-full border border-gray-200 flex items-center
                     justify-center text-foreground hover:border-foreground/60 transition-colors
                      leading-none disabled:opacity-50 disabled:cursor-not-allowed"
                    aria-label="Increase quantity"
                    disabled={isQuantityMaxed || isLoading}
                >
                    +
                </button>
            </div>

            <button
                onClick={() => onRemove(item.productId)}
                className="shrink-0 hover:red-400 transition-colors ml-1
                disabled:opacity-50 disabled:cursor-not-allowed text-foreground hover:text-red-500"
                aria-label="Remove item"
                disabled={isLoading}
            >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>
    )
}