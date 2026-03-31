import api, { buildMultipartJsonPart } from "@lib/api/api"

import {
  AdminBrand,
  AdminCategory,
  AdminCategoryAttribute,
  AdminOrder,
  AdminProduct,
  AdminReview,
  AdminUser,
  CreateBrandRequest,
  CreateCategoryAttribute,
  CreateCategoryRequest,
  CreateProductRequest,
  EditBrandRequest,
  EditCategoryAttribute,
  EditCategoryRequest,
  EditProductRequest,
  ImageMetadata,
  Role,
} from "@_types/admin"
import { Page } from "@_types/page"

const paths = {
  base: "/admin",
  product: () => paths.base + "/product",
  category: () => paths.base + "/category",
  brand: () => paths.base + "/brand",
  order: () => paths.base + "/order",
  user: () => paths.base + "/user",
  review: () => paths.base + "/review",
}

export const adminApi = {
  getProducts: (page = 0, size = 20): Promise<Page<AdminProduct>> =>
    api.get(paths.product() + `?page=${page}&size=${size}`),

  getProductsByText: (query: string, page = 0, size = 20): Promise<Page<AdminProduct>> =>
    api.get(paths.product() + `/search?query=${query}&page=${page}&size=${size}`),

  getProduct: (id: number | string): Promise<AdminProduct> => api.get(paths.product() + `/${id}`),

  createProduct: (data: CreateProductRequest, imageFiles: File[] = []): Promise<AdminProduct> => {
    const form = new FormData()
    form.append(
      "product",
      buildMultipartJsonPart({
        ...data,
        attributes: data.attributes ?? [],
      }),
    )
    imageFiles.forEach((f) => form.append("images", f, f.name))
    return api.post(paths.product(), form, {
      headers: { "Content-Type": "multipart/form-data" },
    })
  },
  updateProduct: (
    id: number,
    data: EditProductRequest,
    newImageFiles: File[] = [],
    newImageMetadata: ImageMetadata[] = [],
  ): Promise<AdminProduct> => {
    const form = new FormData()
    form.append(
      "product",
      buildMultipartJsonPart({
        ...data,
        id,
      }),
    )
    if (newImageFiles.length > 0) {
      newImageFiles.forEach((f) => form.append("images", f, f.name))
      form.append("image_metadata", buildMultipartJsonPart(newImageMetadata))
    }
    return api.patch(paths.product() + `/${id}`, form, {
      headers: { "Content-Type": "multipart/form-data" },
    })
  },
  deleteProduct: (id: number): Promise<void> => api.delete(paths.product() + `/${id}`),

  // ---------------- Categories ----------------
  createCategory: (data: CreateCategoryRequest): Promise<AdminCategory> =>
    api.post(paths.category(), data),
  getCategory: (id: number | string): Promise<AdminCategory> =>
    api.get(paths.category() + `/${id}`),
  getCategoriesByText: (query: string, page = 0, size = 20): Promise<Page<AdminCategory>> =>
    api.get(paths.category() + `/search?query=${query}&page=${page}&size=${size}`),
  editCategory: (data: EditCategoryRequest): Promise<AdminCategory> =>
    api.put(paths.category() + `/${data.id}`, data),
  deleteCategory: (id: number): Promise<void> => api.delete(paths.category() + `/${id}`),

  createCategoryAttribute: (data: CreateCategoryAttribute): Promise<AdminCategoryAttribute> =>
    api.post(paths.category() + "/attribute", data),
  getCategoryAttributeById: (id: string): Promise<AdminCategoryAttribute> =>
    api.get(paths.category() + `/attribute/${id}`),
  getCategoryAttributesByText: (
    query: string,
    page = 0,
    size = 20,
  ): Promise<Page<AdminCategoryAttribute>> =>
    api.get(paths.category() + `/attribute/search?query=${query}&page=${page}&size=${size}`),
  editCategoryAttribute: (data: EditCategoryAttribute): Promise<AdminCategoryAttribute> =>
    api.put(paths.category() + `/attribute/${data.id}`, data),
  deleteCategoryAttribute: (id: number): Promise<AdminCategoryAttribute> =>
    api.post(paths.category() + `/attribute/${id}`),

  // ---------------- Brands ----------------
  createBrand: (data: CreateBrandRequest): Promise<AdminBrand> => api.post(paths.brand(), data),
  getBrand: (id: string | number): Promise<AdminBrand> => api.get(paths.brand() + `/${id}`),
  getBrandsByText: (query: string, page = 0, size = 20): Promise<Page<AdminBrand>> =>
    api.get(paths.brand() + `/search?query=${query}&page=${page}&size=${size}`),
  editBrand: (data: EditBrandRequest): Promise<AdminBrand> =>
    api.put(paths.brand() + `/${data.id}`, data),
  deleteBrand: (id: number): Promise<void> => api.delete(paths.brand() + `/${id}`),

  // ---------------- Orders ----------------
  getOrder: (orderId: string): Promise<AdminOrder> => api.get(paths.order() + `/${orderId}`),
  updateOrder: (
    orderId: string,
    request: { shippingType?: "STANDARD" | "EXPRESS"; status?: string },
  ): Promise<AdminOrder> =>
    api.patch(paths.order() + `/${orderId}`, {
      orderId,
      shippingType: request.shippingType,
      status: request.status,
    }),
  cancelOrder: (orderId: string, reason: string): Promise<void> =>
    api.post(paths.order() + `/${orderId}/cancel`, { reason }),
  refundOrder: (orderId: string, reason?: string): Promise<void> =>
    api.post(paths.order() + `/${orderId}/refund`, {
      orderId,
      reason: reason ?? undefined,
    }),

  // ---------------- Users ----------------
  getUser: (userId: string): Promise<AdminUser> => api.get(paths.user() + `/${userId}`),
  updateUser: (userId: string, role: Role): Promise<AdminUser> =>
    api.patch(paths.user() + `/${userId}`, { role }),

  // ---------------- Reviews ----------------
  getReview: (reviewId: number): Promise<AdminReview> => api.get(paths.review() + `/${reviewId}`),
  deleteReview: (reviewId: number, cause?: string): Promise<void> =>
    api.delete(paths.review() + `/${reviewId}`, { data: { cause: cause ?? "" } }),
}
