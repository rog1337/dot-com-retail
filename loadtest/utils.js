import crypto from "k6/crypto"

export const NEXT = "http://localhost:3000"
export const API  = "http://localhost:8080/api/v1"

export const CATEGORY_IDS = [1]
export const PRODUCT_IDS = Array.from({ length: 20 }, (_, i) => 1000 + i)
export const DIAMETERS = [15, 16, 17, 18, 19]
export const WIDTHS = [195, 205, 215, 225, 235, 245]
export const HEIGHTS = [40, 45, 50, 55, 60, 65]

export function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)]
}

export function thinkTime(min = 1, max = 3) {
    return Math.random() * (max - min) + min
}

export function generateStripeSignature(payload, secret = "whsec_516e79e1c9e2db074a3fb1d2844ddc4d5a223da58a1d1bd6d1f7650898d2387c") {
    const timestamp = Math.floor(Date.now() / 1000).toString()
    const signedPayload = `${timestamp}.${payload}`
    const signature = crypto.hmac("sha256", secret, signedPayload, "hex")
    return `t=${timestamp},v1=${signature}`
}

export function orderRequestBody(email = "test@example.com") {
    return {
        name: "Test Name",
        phone: 5555555,
        email: email,
        address: {
            streetLine1: "Test Street",
            streetLine2: "Test Street2",
            city: "Test City",
            stateOrProvince: "Test State",
            postalCode: 11111,
            country: "Test Country",
        },
        shippingType: "STANDARD",
    }
}

export function paymentIntentSucceededPayload(intentId) {
    return {
        "id": "evt_3TIpEtPkO5Lq9JNa1QNDlBhZ",
        "object": "event",
        "api_version": "2026-01-28.clover",
        "created": 1775390005,
        "data": {
            "object": {
                "id": intentId,
                "object": "payment_intent",
                "amount": 33028,
                "amount_capturable": 0,
                "amount_details": {
                    "tip": {}
                },
                "amount_received": 33028,
                "application": null,
                "application_fee_amount": null,
                "automatic_payment_methods": {
                    "allow_redirects": "always",
                    "enabled": true
                },
                "canceled_at": null,
                "cancellation_reason": null,
                "capture_method": "automatic_async",
                "client_secret": intentId + "_secret_MpPQyN6LMJjTHQTKAkZP1GZFV",
                "confirmation_method": "automatic",
                "created": 1775389995,
                "currency": "eur",
                "customer": null,
                "customer_account": null,
                "description": null,
                "excluded_payment_method_types": null,
                "last_payment_error": null,
                "latest_charge": "ch_3TIpEtPkO5Lq9JNa1UZaDZq8",
                "livemode": false,
                "metadata": {
                    "updated_at": "1775390004502"
                },
                "next_action": null,
                "on_behalf_of": null,
                "payment_method": "pm_1TIpF3PkO5Lq9JNahJbmCQci",
                "payment_method_configuration_details": {
                    "id": "pmc_1Syg1xPkO5Lq9JNagqVb8RRc",
                    "parent": null
                },
                "payment_method_options": {
                    "bancontact": {
                        "preferred_language": "en"
                    },
                    "card": {
                        "installments": null,
                        "mandate_options": null,
                        "network": null,
                        "request_three_d_secure": "automatic"
                    },
                    "eps": {},
                    "klarna": {
                        "preferred_locale": null
                    },
                    "link": {
                        "persistent_token": null
                    },
                    "paypal": {
                        "preferred_locale": null,
                        "reference": null
                    }
                },
                "payment_method_types": [
                    "card",
                    "bancontact",
                    "eps",
                    "klarna",
                    "link",
                    "paypal"
                ],
                "processing": null,
                "receipt_email": null,
                "review": null,
                "setup_future_usage": null,
                "shipping": {
                    "address": {
                        "city": "Tallinn",
                        "country": "EE",
                        "line1": "Valge 1",
                        "line2": null,
                        "postal_code": "11413",
                        "state": ""
                    },
                    "carrier": null,
                    "name": "Jenny Rosen",
                    "phone": "+37256558402",
                    "tracking_number": null
                },
                "source": null,
                "statement_descriptor": null,
                "statement_descriptor_suffix": null,
                "status": "succeeded",
                "transfer_data": null,
                "transfer_group": null
            }
        },
        "livemode": false,
        "pending_webhooks": 2,
        "request": {
            "id": "req_JMlauY5Juhc2RT",
            "idempotency_key": "7ffaf577-43fc-401a-9a30-2e6dae3fefd4"
        },
        "type": "payment_intent.succeeded"
    }
}