"use client"

import {
    AddressElement,
    LinkAuthenticationElement,
    PaymentElement,
    useElements,
    useStripe
} from "@stripe/react-stripe-js"
import React, {useCallback, useEffect, useRef, useState} from "react"
import {
    StripeAddressElementChangeEvent,
    StripeLinkAuthenticationElementChangeEvent
} from "@stripe/stripe-js"
import {orderApi} from "@lib/api/orderApi"
import {useAuth} from "@lib/auth/authContext"
import {Contact} from "@_types/contact"
import {logger, logger as log} from "@lib/logger"
import {ShippingType} from "@_types/order"
import {accountApi} from "@lib/api/accountApi";
import Loading from "@components/Loading";
import {useToastStore} from "@store/toastStore";
import {useCheckout} from "@stripe/react-stripe-js/checkout";
import {useCartStore} from "@store/cartStore";

type CheckoutFormProps = {
    paymentFormRef: React.RefObject<HTMLFormElement | null>
}

export default function CheckoutForm({ paymentFormRef }: CheckoutFormProps) {
    const stripe = useStripe()
    const elements = useElements()
    const { user, sessionId } = useAuth()
    const { setIsLoading } = useCartStore()
    const { show } = useToastStore()
    const [email, setEmail] = useState<string>("")
    const [contact, setContact] = useState<Contact>()
    const [shippingType] = useState<ShippingType>(ShippingType.STANDARD)
    const [elementsReady, setElementsReady] = useState<boolean>(false)
    const [loading, setLoading] = useState(true)
    let emailRef = useRef<string>(email)

    useEffect(() => {
        const fetchContact = async () => {
            try {
                const { contact } = await accountApi.getAccountDetails()
                setContact(contact)
            } catch(e: any) {
                logger.d("Error fetching contact", e)
            } finally {
                setLoading(false)
            }
        }
        if (user) fetchContact()
    }, [])

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
        if (stripe == null) {
            show("Stripe error", "error", 5000)
            log.d("Stripe is null")
            setIsLoading(false)
            return
        }
        if (elements == null) {
            show("Stripe error", "error", 5000)
            log.d("Stripe Elements is null")
            setIsLoading(false)
            return
        }
        if (contact == null) {
            log.d("Contact info is null")
            setIsLoading(false)
            return
        }

        const { error: submitError } = await elements.submit()

        if (submitError) {
            log.d("Stripe elements submit error", submitError)
            setIsLoading(false)
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
            show("An error occurred processing your order", "error", 5000)
            log.d("Failed to submit order", e)
            return
        }

        try {
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
        } catch (e: any) {
            show("An error occurred with Stripe payment", "error", 5000)
            logger.d("Stripe payment confirm failed", e)
        }
        setIsLoading(false)
    }

    if (loading) return <Loading/>

    return (
        <form ref={paymentFormRef} onSubmit={handleSubmit}>
            <LinkAuthenticationElement
                options={{
                    defaultValues: {
                        email: contact?.email ?? ""
                    }
                }}
                onChange={(event) => handleEmailChange(event)}
            />
            <AddressElement
                options={{
                    mode: "shipping",
                    autocomplete: {mode: "automatic"},
                    fields: {phone: "always"},
                    validation: {phone: {required: "always"}},
                    defaultValues: {
                        name: contact?.name,
                        phone: contact?.phone,
                        address: {
                            line1: contact?.address?.streetLine1,
                            line2: contact?.address?.streetLine2,
                            city: contact?.address?.city,
                            state: contact?.address?.stateOrProvince,
                            postal_code: contact?.address?.postalCode,
                            country: contact?.address?.country ?? ""
                        }
                    }
                }}
                onChange={(event) => handleContactChange(event)}
                onReady={() => setElementsReady(true)}
            />

            {elementsReady && <span>Payment</span>}
            <PaymentElement/>
        </form>
    )
}