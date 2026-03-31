import api from "@lib/api/api"
import { Product, ProductResponse } from "@_types/product"
import { cache } from "react"
import { AddReviewRequest, ProductReviewResponse, Review } from "@_types/review"

export const productPaths = {
  base: "/product",
  search: () => `${productPaths.base}/search`,
}

export const productApi = {
  getById: cache((id: number | string): Promise<Product> => api.get(productPaths.base + `/${id}`)),
  getByQuery: (params: URLSearchParams): Promise<ProductResponse> =>
    api.get(productPaths.base, { params }),

  getReviews: cache(
    (productId: number | string): Promise<ProductReviewResponse> =>
      api.get(productPaths.base + `/${productId}/review`),
  ),

  addReview: (productId: number, body: AddReviewRequest): Promise<Review> =>
    api.post(productPaths.base + `/${productId}/review`, body),
}