import http from "k6/http"
import {check, group, sleep} from "k6"
import {vu} from "k6/execution"
import {API, CATEGORY_IDS, DIAMETERS, NEXT, pick, PRODUCT_IDS, thinkTime} from "./utils.js"

http.setResponseCallback(
    http.expectedStatuses(200, 409)
)

export const options = {
    stages: [
        {duration: "30s", target: 5},
        {duration: "1m", target: 30},
        {duration: "1m", target: 30},
        {duration: "30s", target: 0},
    ],
    thresholds: {
        "http_req_duration": ["p(90)<2000"],
        "http_req_failed": ["rate<0.02"],
        "http_req_duration{layer:ssr}": ["p(90)<2000"],
        "http_req_duration{layer:api}": ["p(90)<2000"],
    },
}

export default function () {
    const catId = pick(CATEGORY_IDS)
    const productId = pick(PRODUCT_IDS)
    const diameter = pick(DIAMETERS)
    let headers = {"Content-Type": "application/json"}

    group("1_home_page", () => {
        const res = http.get(`${NEXT}/`, {tags: {layer: "ssr"}})
        check(res, {"home page 200": r => r.status === 200})
    })

    group("2_auth_refresh", () => {
        const res = http.post(`${API}/auth/login`, JSON.stringify({
            email: `testuser${vu.idInTest}@example.com`,
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

    group("2_1_account", () => {
        const res = http.get(`${API}/account`, {headers})
        check(res, {"account 200": r => r.status === 200})
    })

    group("3_featured_products", () => {
        const res = http.get(`${API}/product?page=0&size=8`, {tags: {layer: "api"}})
        check(res, {"featured products 200": r => r.status === 200})
    })

    group("4_browse_and_filter", () => {
        http.get(`${NEXT}/browse?categoryId=${catId}`, {tags: {layer: "ssr"}})
        sleep(thinkTime())
        http.get(`${API}/filter?categoryId=${catId}`, {tags: {layer: "api"}})
        sleep(thinkTime(1, 2))
        http.get(
            `${NEXT}/browse?page=0&size=20&categoryId=${catId}&attr_diameter=${diameter}`,
            {tags: {layer: "ssr"}}
        )
    })
    sleep(thinkTime())

    group("7_product_page", () => {
        const res = http.get(`${NEXT}/products/${productId}`, {tags: {layer: "ssr"}})
        check(res, {"product page 200": r => r.status === 200})
    })
    sleep(thinkTime(1, 3))

    group("8_reviews", () => {
        const res = http.get(`${API}/product/${productId}/review`, {tags: {layer: "api"}})
        check(res, {"reviews 200": r => r.status === 200})
    })
    sleep(thinkTime())

    group("9_add_to_cart", () => {
        const res = http.put(
            `${API}/cart`,
            JSON.stringify({items: [{productId, quantity: 1}]}),
            {headers, tags: {layer: "api"}}
        )
        check(res, {"add to cart 200/409": r => r.status === 200 || r.status === 409})
    })
    sleep(thinkTime())
}