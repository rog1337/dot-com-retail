import http from "k6/http"
import { sleep, check, group } from "k6"
import { NEXT, API, pick, thinkTime, CATEGORY_IDS, PRODUCT_IDS, DIAMETERS, WIDTHS, HEIGHTS } from "./utils.js"

http.setResponseCallback(
    http.expectedStatuses(200, 401)
)

export const options = {
    stages: [
        { duration: "30s", target: 10 },
        { duration: "1m", target: 50 },
        { duration: "1m", target: 50 },
        { duration: "30s", target: 0  },
    ],
    thresholds: {
        "http_req_duration": ["p(90)<2000"],
        "http_req_failed": ["rate<0.02"],
        "http_req_duration{layer:ssr}": ["p(90)<2000"],
        "http_req_duration{layer:api}": ["p(90)<2000"],
        "http_req_failed{layer:ssr}": ["rate<0.02"],
        "http_req_failed{layer:api}": ["rate<0.02"],
    },
}

function logFailure(res) {
    if (res.status < 200 || res.status >= 300) {
        console.log(`FAIL [${res.status}] ${res.request.method} ${res.url} → ${res.body.substring(0, 300)}`);
    }
}

export default function () {
    const catId = pick(CATEGORY_IDS)
    const productId = pick(PRODUCT_IDS)
    const diameter = pick(DIAMETERS)
    const width = pick(WIDTHS)
    const height = pick(HEIGHTS)

    group("1_home_page", () => {
        const res = http.get(`${NEXT}/`, {tags: {layer: "ssr"}})
        check(res, {"home page 200": r => r.status === 200})
    })
    sleep(thinkTime())

    group("2_auth_refresh", () => {
        const res = http.get(`${API}/auth/refresh`, {tags: {layer: "api"}})
        check(res, {"refresh responded": r => r.status === 200 || r.status === 401})
    })
    sleep(thinkTime(0.5, 1.5))

    group("3_featured_products", () => {
        const res = http.get(`${API}/product?page=0&size=8`, {tags: {layer: "api"}})
        check(res, {"featured products 200": r => r.status === 200})
        logFailure(res)
    })
    sleep(thinkTime())

    group("4_browse_page", () => {
        const res = http.get(`${NEXT}/browse?categoryId=${catId}`, {tags: {layer: "ssr"}})
        check(res, {"browse page 200": r => r.status === 200})
        logFailure(res);
    })
    sleep(thinkTime())

    group("5_fetch_filters", () => {
        const res = http.get(`${API}/filter?categoryId=${catId}`, {tags: {layer: "api"}})
        check(res, {"filters 200": r => r.status === 200})
        logFailure(res);
    })
    sleep(thinkTime(1, 2))

    group("6_apply_filters", () => {
        let res = http.get(
            `${NEXT}/browse?page=0&size=20&categoryId=${catId}&attr_diameter=${diameter}`,
            {tags: {layer: "ssr"}}
        )
        logFailure(res);
        check(res, {"filter 1 applied 200": r => r.status === 200})
        sleep(thinkTime(1, 2))

        res = http.get(
            `${NEXT}/browse?page=0&size=20&categoryId=${catId}&attr_diameter=${diameter}&attr_width=${width}`,
            {tags: {layer: "ssr"}}
        )
        check(res, {"filter 2 applied 200": r => r.status === 200})
        logFailure(res);
        sleep(thinkTime(1, 2))

        res = http.get(
            `${NEXT}/browse?page=0&size=20&categoryId=${catId}&attr_diameter=${diameter}&attr_width=${width}&attr_height=${height}`,
            {tags: {layer: "ssr"}}
        )
        logFailure(res);
        check(res, {"filter 3 applied 200": r => r.status === 200})
    })
    sleep(thinkTime())

    group("7_product_page", () => {
        const res = http.get(`${NEXT}/products/${productId}`, {tags: {layer: "ssr"}})
        check(res, {"product page 200": r => r.status === 200})
    })
    sleep(thinkTime(1, 3))

    group("8_product_reviews", () => {
        const res = http.get(`${API}/product/${productId}/review`, {tags: {layer: "api"}})
        check(res, {"reviews 200": r => r.status === 200})
        logFailure(res);
    })
    sleep(thinkTime())
}