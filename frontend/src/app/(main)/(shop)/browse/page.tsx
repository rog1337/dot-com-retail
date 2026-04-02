import qs from "qs"
import {productApi} from "@lib/api/productApi"
import {createProductQuery, ProductQuery, ProductResponse} from "@_types/product"
import Browse from "@/src/app/(main)/(shop)/browse/Browse"
import {createProductQueryParams} from "@lib/params"

export default async function BrowsePage({ searchParams }: any) {
    const parsedParams = qs.parse(await searchParams, { allowDots: true, ignoreQueryPrefix: true })
    let query: ProductQuery = {
        ...createProductQuery(),
        ...parsedParams,
        attributes: formatAttributes()
    }

    query = Object.fromEntries(
        Object.entries(query).filter(([key]) => !key.startsWith("attr_"))
    ) as ProductQuery

    if (!Number(query.categoryId) || query.categoryId < 1) {
        query.categoryId = 1
    }

    const { content: products, page }: ProductResponse = await productApi.getByQuery(query)

    function formatAttributes() {
        return Object.entries(parsedParams)
            .filter(([key]) => key.startsWith("attr_"))
            .map(([key, value]) => {
                let values: string[]

                if (typeof value === "string") {
                    values = value.split(",")
                } else if (Array.isArray(value)) {
                    values = value.map(v => (typeof v === "string" ? v : JSON.stringify(v)))
                } else {
                    values = [JSON.stringify(value)]
                }

                return { name: key.substring("attr_".length), values }
            })
    }

    return <Browse params={query} products={products} page={page} ></Browse>
}

