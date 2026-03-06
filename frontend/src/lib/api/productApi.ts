import api from "@lib/api/api"
import {Product, ProductQuery, ProductResponse} from "@_types/product"

export const productPaths = {
    base: "/product",
    byId: (id: number) => `${productPaths.base}/${id}`,
    search: () => `${productPaths.base}/search`,
}

export const productApi = {
    getById: (id: number): Promise<Product> => api.get(productPaths.byId(id)),
    getByQuery: (params: URLSearchParams): Promise<ProductResponse> => api.get(productPaths.base, { params }),
}