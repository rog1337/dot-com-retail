"use client"

import {Elements} from "@stripe/react-stripe-js"
import {loadStripe} from "@stripe/stripe-js"
import React, {useEffect, useRef, useState} from "react"
import {orderApi} from "@lib/api/orderApi"
import {useAuth} from "@lib/auth/authContext"
import {useTheme} from "next-themes"
import CheckoutForm from "@/src/app/checkout/CheckoutForm"
import {logger} from "@lib/logger"
import CheckoutWindow from "@/src/app/checkout/CheckoutWindow"
import {useCartStore} from "@store/cartStore"
import {useRouter} from "next/navigation"
import Loading from "@/src/components/Loading"

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY!)

export default function Checkout() {
    const { items } = useCartStore()
    const { sessionId } = useAuth()
    const { theme } = useTheme()
    const router = useRouter()
    const [clientSecret, setClientSecret] = useState<string>()
    const paymentFormRef = useRef<HTMLFormElement>(null)

    useEffect(() => {
        const createIntent = async () => {
            try {
                const res = await orderApi.createOrder(sessionId)
                setClientSecret(res.clientSecret)
            } catch (e: any) {
                const code = e.response?.data?.code
                if (code === "CART_NOT_FOUND") {
                    router.push("/")
                } else if (code === "CART_EMPTY") {
                    router.push("/cart")
                } else if (code === "CART_IDENTIFIER_REQUIRED") {
                    router.push("/")
                }
                logger.d("Checkout load error", e)
            }
        }

        createIntent()
    }, [])

    if (!clientSecret) return <Loading/>

    return (
        <div
            className="flex flex-col max-w-4/5 align-center justify-center mx-auto mt-20 mb-20 gap-20 md:flex-row"
        >

            <div
                className="w-150 max-w-full"
            >
                <Elements
                    stripe={stripePromise}
                    options={{
                        appearance: {
                            theme: theme === "dark" ? "night" : "stripe",
                        },
                        clientSecret
                    }
                    }
                >
                    <CheckoutForm paymentFormRef={paymentFormRef}></CheckoutForm>
                </Elements>
            </div>

            <div className="sticky top-6 h-fit">
                <CheckoutWindow paymentFormRef={paymentFormRef}/>
            </div>

        </div>
    )
}