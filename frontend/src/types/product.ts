import { createPageParams, PageMetadata, PageParams } from "@_types/page"

export interface Product {
  id: number
  name: string
  description: string
  slug: string
  sku: string
  price: number
  salePrice: number
  stock: number
  brand: Brand
  category: Category
  attributes: Attribute[]
  images: Image[]
  averageRating: number
  reviewCount: number
  isActive: true
}

export interface Brand {
  id: number
  name: string
}

export interface Category {
  id: number
  name: string
}

export interface Image {
  id: number
  url: string
  sortOrder: number
  altText: string
}

export type Attribute = {
  name: string
  values: Array<string | number | boolean>
}

export type SortOrder = "TOP" | "PRICE_ASC" | "PRICE_DESC"

export interface ProductQuery extends PageParams {
  categoryId: number
  brands: number[]
  attributes: Attribute[]
  sort: SortOrder
}

export function createProductQuery(): ProductQuery {
  return {
    categoryId: 0,
    brands: [],
    attributes: [],
    sort: "TOP",
    ...createPageParams(),
  }
}

export interface ProductResponse {
  content: Product[]
  page: PageMetadata
}