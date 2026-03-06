"use client"

import {
    AddressElement,
    LinkAuthenticationElement,
    PaymentElement,
    useElements,
    useStripe
} from "@stripe/react-stripe-js"
import React, {useRef, useState} from "react"
import {
    StripeAddressElementChangeEvent,
    StripeLinkAuthenticationElementChangeEvent
} from "@stripe/stripe-js"
import {orderApi} from "@lib/api/orderApi"
import {useAuth} from "@lib/auth/authContext"
import {Contact} from "@_types/contact"
import {logger as log} from "@lib/logger"
import {ShippingType} from "@_types/order"

type CheckoutFormProps = {
    paymentFormRef: React.RefObject<HTMLFormElement | null>
}

export default function CheckoutForm({ paymentFormRef }: CheckoutFormProps) {
    const stripe = useStripe()
    const elements = useElements()
    const [clientSecret, setClientSecret] = React.useState<string>("")
    const { sessionId } = useAuth()
    const [email, setEmail] = useState<string>("")
    const [contact, setContact] = useState<Contact>()
    const [isPayEnabled, setIsPayEnabled] = useState<boolean>(false)
    const [shippingType, setShippingType] = useState<ShippingType>(ShippingType.STANDARD)
    const [elementsReady, setElementsReady] = useState<boolean>(false)
    let emailRef = useRef<string>(email)

    const handleEmailChange = (e: StripeLinkAuthenticationElementChangeEvent) => {
        emailRef.current = e.value.email
        setEmail(e.value.email)
    }

    const handleContactChange = (e: StripeAddressElementChangeEvent) => {
        const contact = {
            name: e.value.name,
            email: emailRef.current,
            phone: e.value.phone ?? "",
            address: {
                streetLine1: e.value.address.line1,
                streetLine2: e.value.address.line2 ?? null,
                postalCode: e.value.address.postal_code,
                city: e.value.address.city,
                country: e.value.address.country,
                stateOrProvince: e.value.address.state
            }
        }
        setContact(contact)
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        if (stripe == null) {setIsPayEnabled(false); log.d("Stripe is null"); return}
        if (elements == null) {setIsPayEnabled(false); log.d("Stripe Elements is null"); return}
        if (contact == null) {setIsPayEnabled(false); log.d("Contact info is null"); return}

        const { error: submitError } = await elements.submit()

        if (submitError) {
            log.d("Stripe elements submit error", submitError)
            setIsPayEnabled(false)
            return
        }

        try {
            const requestBody = {
                name: contact.name,
                email: email,
                phone: contact.phone,
                address: contact.address,
                shippingType: shippingType,
            }
            const submitOrderResponse = await orderApi.submitOrder(sessionId, requestBody)
        } catch (e: any) {
            log.d("Failed to submit order", e)
            setIsPayEnabled(false)
            return
        }

        const result = await stripe.confirmPayment({
            elements,
            confirmParams: {
                return_url: window.location.origin + "/checkout/confirm",
                payment_method_data: {
                    billing_details: {
                        email,
                    },
                }
            },
        })
    }

    return (
        <form ref={paymentFormRef} onSubmit={handleSubmit}>
            <LinkAuthenticationElement
                onChange={(event) => handleEmailChange(event)}
            />
            <AddressElement
                options={{
                    mode: "shipping",
                    autocomplete: { mode: "automatic"},
                    fields: { phone: "always" },
                    validation: { phone: { required: "always" } }
                }}
                onChange={(event) => handleContactChange(event)}
                onReady={() => setElementsReady(true)}
            />

            {elementsReady && <span>Payment</span>}
            <PaymentElement/>
        </form>
    )
}