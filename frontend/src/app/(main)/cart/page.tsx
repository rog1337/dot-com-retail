"use client"

import React, { useEffect } from "react"
import { useCartStore } from "@store/cartStore"
import {cartApi} from "@lib/api/cartApi"
import {useAuth} from "@lib/auth/authContext"
import {logger as log} from "@lib/logger"
import CartItem from "@components/cart/CartItem"
import Link from "next/link"
import {useToastStore} from "@store/toastStore"

type SearchParams = {
    redirect_status?: string
}

export default function Cart({ searchParams }: { searchParams: SearchParams }) {
    const { isLoggedIn, sessionId } = useAuth()
    const { items, setCart, subTotal, total } = useCartStore()
    const { show } = useToastStore()

    useEffect(() => {

        const showPaymentFailedToast = async () => {
            const { redirect_status } = await searchParams
            if (redirect_status === "failed") {
                show("Payment failed", "error")
            }
        }

        const fetchCart = async() => {
            try {
                const cart = await cartApi.getCart(sessionId)
                setCart(cart)
            } catch(e: any) {
                const code = e.response?.data?.code
                if (code === "CART_NOT_FOUND") {
                    return
                }
                log.d("Failed to fetch cart", e)
                throw Error("Failed to fetch cart", e)
            }
        }

        showPaymentFailedToast()

        if (isLoggedIn || sessionId) {
            fetchCart()
        }

    }, [])

    if (!isLoggedIn && !sessionId || items.length === 0) {

        return emptyCart()
    }

    function emptyCart() {
        return <div className="mt-20 p-10 text-center">Cart is empty</div>
    }

    return (
        <div
            className="flex flex-col max-w-4/5 align-center justify-center mx-auto mt-20 gap-20 md:flex-row"
        >
            <div className="w-full max-w-md rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
                    <h2 className="font-semibold">Your Cart</h2>
                    {items.length > 0 && (
                        <span className="text-base font-medium bg-gray-400 px-2 py-0.5 rounded-full">
            {items.length} {items.length === 1 ? "item" : "items"}
          </span>
                    )}
                </div>

                <div className="px-5 overflow-y-auto max-h-[420px]">
                    {items.length === 0 ? (
                        <div className="py-12 text-center ">
                            Your cart is empty
                        </div>
                    ) : (
                        items.map((item) => (
                            <CartItem
                                key={item.productId}
                                item={item}
                            />
                        ))
                    )}
                </div>


                {items.length > 0 && (
                    <div className="px-5 py-4 border-t border-gray-100 flex items-center justify-between">
                        <div>
                            <p className="uppercase tracking-wide">Total</p>
                            <p className="text-lg font-semibold ">€{subTotal.toFixed(2)}</p>
                        </div>

                        <Link
                            href="/checkout"
                            className="px-5 py-2 bg-gray-900 text-white text-xl font-medium rounded-xl hover:bg-gray-700 transition-colors"
                        >To Checkout
                        </Link>
                    </div>
                )}
            </div>

        </div>
    )
}