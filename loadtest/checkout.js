import http from "k6/http"
import {check, group, sleep} from "k6"
import {vu} from "k6/execution"
import {
    API,
    generateStripeSignature,
    NEXT,
    orderRequestBody,
    paymentIntentSucceededPayload,
    pick,
    PRODUCT_IDS,
    thinkTime
} from "./utils.js"

http.setResponseCallback(
    http.expectedStatuses(200, 409)
)

export const options = {
    stages: [
        {duration: "15s", target: 10},
        {duration: "30s", target: 50},
        {duration: "15s", target: 10},
    ],
    thresholds: {
        "http_req_duration": ["p(90)<2000"],
        "http_req_failed": ["rate<0.05"],
        "group_duration{group:::4_checkout}": ["p(90)<3000"],
    },
}

export default async function () {
    const email = `testuser${vu.idInTest}@example.com`
    const productId = pick(PRODUCT_IDS)
    let headers = {"Content-Type": "application/json"}
    let paymentIntent
    let res
    let outOfStock = false

    group("1_login", () => {
        const res = http.post(`${API}/auth/login`, JSON.stringify({
            email: email,
            password: "password",
        }), {headers})
        if (res.status === 200) {
            try {
                const token = JSON.parse(res.body).accessToken
                headers = {...headers, Authorization: `Bearer ${token}`}
            } catch (_) {
            }
        }
        check(res, {"login 200": r => r.status === 200})
    })

    if (!headers.Authorization) return

    res = http.put(
        `${API}/cart`,
        JSON.stringify({items: [{productId, quantity: 1}]}),
        {headers, tags: {layer: "api"}}
    )
    check(res, {"item added to cart 200, 409": r => r.status === 200 || r.status === 409})
    if (res.status === 409) {
        return
    }

    sleep(thinkTime(1, 2))

    group("3_checkout_page", () => {
        let res = http.get(
            `${NEXT}/checkout`,
            {tags: {layer: "ssr"}}
        )
        check(res, {"checkout page 200": r => r.status === 200})

        res = http.post(
            `${API}/cart/checkout`,
            null,
            {headers, tags: {layer: "api"}}
        )
        check(res, {"backend cart checkout 200": r => r.status === 200 || r.status === 409})
        if (res.status === 409) {
            outOfStock = true
        }

        res = http.get(
            `${API}/account/details`,
            {headers, tags: {layer: "api"}}
        )
        check(res, {"account details 200": r => r.status === 200})

        res = http.get(
            `${API}/cart`,
            {headers, tags: {layer: "api"}}
        )
        check(res, {"get cart 200": r => r.status === 200})
    })
    if (outOfStock) {
        return
    }
    sleep(thinkTime(2, 3))

    group("4_submit_order", () => {
        const res = http.post(
            `${API}/order/submit`,
            JSON.stringify(orderRequestBody(email)),
            {headers, tags: {layer: "api"}}
        )
        check(res, {"submit order 200": r => r.status === 200})
        paymentIntent = res.json().paymentId
        if (!paymentIntent) {
            console.log("NO PAYMENT INTENT")
            console.log(res.json())
        }

    })
    if (!paymentIntent) {
        return
    }

    const payload = JSON.stringify({...paymentIntentSucceededPayload(paymentIntent)})
    const signature = generateStripeSignature(payload)

    res = http.post(
        `${API}/payment/webhook/stripe`,
        payload,
        {headers: {"Stripe-Signature": signature}, tags: {layer: "api"}}
    )
    check(res, {"call stripe webhook 200": r => r.status === 200 || r.status === 409})
    if (res.status === 409) {
        return
    }
    sleep(thinkTime(3, 5))

    group("6_load_checkout_confirmation", () => {
        const res = http.post(
            `${NEXT}/checkout/confirm?payment_intent=${paymentIntent}`,
            {tags: {layer: "ssr"}}
        )
        check(res, {"checkout confirm 200": r => r.status === 200})
    })
}