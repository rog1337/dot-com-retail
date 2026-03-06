import {ProductQuery} from "@_types/product"

export function createProductQueryParams(queryObject: ProductQuery): URLSearchParams {
    const formattedQuery = compactParams(queryObject)
    const params = new URLSearchParams()
    for (const [key, value] of Object.entries(formattedQuery)) {
        if (Array.isArray(value)) {
            params.append(key, value.join(","))
        } else {
            params.append(key, String(value))
        }
    }
    return params
}

export function compactParams(params: ProductQuery): Record<string, any> {
    let attributes: Record<string, (string | number | boolean)[]> = {}
    params.attributes.forEach(attribute => {
        const key = `attr_${attribute.name}`
        attributes[key] = attribute.values
    })
    const { attributes: _, ...rest } = params
    return {...rest, ...attributes}
}