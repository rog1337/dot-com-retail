"use client"

import React, {useEffect, useState} from "react"
import CartItem from "@components/cart/CartItem"
import {useCartStore} from "@store/cartStore"
import {cartApi} from "@lib/api/cartApi"
import {useAuth} from "@lib/auth/authContext"
import {ShippingType} from "@_types/order"
import {useRouter} from "next/navigation"

type CheckoutWindowProps = {
    paymentFormRef: React.RefObject<HTMLFormElement | null>
}

export default function CheckoutWindow({ paymentFormRef }: CheckoutWindowProps) {
    const { sessionId } = useAuth()
    const { items, setCart, total, shippingCost, setShippingType,
        setShippingCost, shippingType, isLoading, setIsLoading } = useCartStore()
    const router = useRouter()

    useEffect(() => {
        const fetchCart = async () => {
            try {
                const response = await cartApi.getCart(sessionId)
                setCart(response)
            } catch (e: any) {
                console.log("Error fetching cart: ", e)
                throw Error("Error fetching cart: ", e)
            }
        }
        fetchCart()
    }, [])

    useEffect(() => {
        if (items.length === 0) {
            router.push("/cart")
        }
    }, [items])

    const handlePay = async () => {
        setIsLoading(true)
        paymentFormRef.current?.requestSubmit()
    }

    const handleShippingChange = async (type: ShippingType) => {
        if (type === shippingType) return
        setIsLoading(true)
        try {
            setShippingType(type)
            setShippingCost(type)
            const cartUpdateRequest = {
                items: items,
                shippingType: type
            }
            const cart = await cartApi.updateCart(sessionId, cartUpdateRequest)
            setCart(cart)
        } catch (e: any) {
            console.log("Error fetching cart: ", e)
            throw Error("Error fetching cart: ", e)
        }
        setIsLoading(false)
    }

    return (
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

            <div className="flex flex-col  px-5 py-4 border-t border-gray-100">
                <div
                    className="flex items-center justify-between font-bold"
                >
                    Shipping
                    <div
                        className="flex space-x-2 font-medium"
                    >
                        <button
                            className={`border-2 rounded-md px-1 hover:bg-gray-400
                            ${shippingType === ShippingType.STANDARD ? "bg-blue-500" : ""}
                            disabled:opacity-50 disabled:cursor-not-allowed`}
                            onClick={() => handleShippingChange(ShippingType.STANDARD)}
                            disabled={isLoading}
                        >Standard</button>
                        <button
                            className={`border-2 rounded-md px-1 hover:bg-gray-400
                            ${shippingType === ShippingType.EXPRESS ? "bg-blue-500" : ""}
                            disabled:opacity-50 disabled:cursor-not-allowed`}
                            onClick={() => handleShippingChange(ShippingType.EXPRESS)}
                            disabled={isLoading}
                        >Express</button>
                    </div>
                </div>
                <span className="" >{shippingCost && `€${shippingCost}`}</span>
            </div>


            {items.length > 0 && (
                <div className="px-5 py-4 border-t border-gray-100 flex items-center justify-between">
                    <div>
                        <p className="  uppercase tracking-wide">Total</p>
                        <p className="text-lg font-semibold ">€{total.toFixed(2)}</p>
                    </div>
                    <button
                        className="px-16 py-2 bg-gray-900 text-white text-xl
                        font-medium rounded-xl hover:bg-gray-700 transition-colors
                        disabled:opacity-50 disabled:cursor-not-allowed"
                        onClick={handlePay}
                        disabled={isLoading}
                    >Pay
                    </button>
                </div>
            )}
        </div>
    )
}