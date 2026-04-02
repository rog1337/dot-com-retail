import api from "@lib/api/api"
import { Product, ProductQuery, ProductResponse } from "@_types/product"
import { cache } from "react"
import { AddReviewRequest, ProductReviewResponse, Review } from "@_types/review"
import {createProductQueryParams} from "@lib/params"

export const productPaths = {
  base: "/product",
  search: () => `${productPaths.base}/search`,
}

export const productApi = {
  getById: cache((id: number | string): Promise<Product> => api.get(productPaths.base + `/${id}`)),
  getByQuery: (query: ProductQuery): Promise<ProductResponse> => {
    const params = createProductQueryParams(query)
    return api.get(productPaths.base, { params })
  },

  getReviews: cache(
    (productId: number | string): Promise<ProductReviewResponse> =>
      api.get(productPaths.base + `/${productId}/review`),
  ),

  addReview: (productId: number, body: AddReviewRequest): Promise<Review> =>
    api.post(productPaths.base + `/${productId}/review`, body),
}