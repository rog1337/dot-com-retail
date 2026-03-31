import { OrderStatus, ShippingType } from "@_types/order"

export type Role = "ADMIN" | "USER"

export type ReviewStatus = "PENDING" | "APPROVED" | "REJECTED"

export interface AdminProduct {
  id: number
  name: string
  sku: string
  description: string
  price: number
  salePrice: number
  stock: number
  isActive: boolean
  category: { id: string; name: string }
  brand: { id: string; name: string }
  images: AdminProductImage[]
  attributes: AdminProductAttribute[]
  createdAt: string
  updatedAt: string
}

export interface AdminProductAttribute {
  name: string
  values: Array<string | number | boolean>
}

export interface AdminUser {
  id: string
  displayName: string
  email: string
  role: Role
  twoFactorEnabled: boolean
  createdAt: string
}

export interface AdminReview {
  id: string
  product: { id: string; name: string }
  author: AdminUser
  rating: number
  body: string
  status: ReviewStatus
  votes: number
  createdAt: string
}

export interface CreateProductRequest {
  name: string
  sku: string
  description?: string | null
  price: number
  salePrice: number
  stock: number
  categoryId: number | null
  brandId: number | null
  isActive: boolean
  images: ImageMetadata[]
  attributes: AdminProductAttribute[]
}

export interface ImageMetadata {
  fileName: string
  sortOrder?: number | null
  altText: string | null
}

export interface EditProductRequest {
  id: number
  name?: string
  sku?: string
  description?: string
  price?: number
  salePrice?: number
  stock?: number
  isActive?: boolean
  categoryId?: number | null
  brandId?: number | null
  attributes: AdminProductAttribute[]
  images?: AdminEditImage[] | null
}

export interface AdminCategory {
  id: number
  name: string
  parentId: number | null
  childrenIds: number[] | null
  attributes: AdminCategoryAttribute[] | null
}

export interface AdminCategoryAttribute {
  id: number
  attribute: string
  label: string
  unit: string | null
  dataType: AttributeDataType
  filterType: FilterType
  displayOrder: number
  isPublic: boolean
  categories: AdminCategory[]
}

export interface CreateCategoryAttribute {
  attribute: string
  label: string
  unit?: string | null
  dataType: AttributeDataType
  filterType: FilterType
  displayOrder: number
}

export interface EditCategoryAttribute {
  id: string
  attribute: string
  label: string
  unit?: string | null
  dataType: AttributeDataType
  filterType: FilterType
  displayOrder: number
}

export type AttributeDataType = "TEXT" | "NUMBER" | "BOOLEAN"
export type FilterType = "CHECKBOX" | "SLIDER" | "DROPDOWN"

export interface AdminBrand {
  id: number
  name: string
  isActive: boolean
  image?: unknown | null
}

export interface AdminOrderItem {
  productId: number
  productName: string
  imageUrl?: string | null
  price: number
  quantity: number
  totalAmount: number
}
export interface AdminOrder {
  id: string
  status: OrderStatus
  paymentId: string
  sessionId?: string | null
  items: AdminOrderItem[]
  shippingType?: ShippingType | null
  shippingCost?: number | null
  totalAmount: number
  contact?: unknown | null
  date: number
  notes?: string | null
}

export interface CreateCategoryRequest {
  name: string
  attributeIds?: number[] | null
}
export interface EditCategoryRequest extends CreateCategoryRequest {
  id: number
}

export interface CreateBrandRequest {
  name: string
  image?: number | null
}

export interface EditBrandRequest extends CreateBrandRequest {
  id: number
}

export interface AdminProductImage {
  id: number
  url: string
  fileName: string
  sortOrder: number
  altText: string | null
}

export interface AdminEditImage {
  id: number
  sortOrder: number
  altText: string | null
}