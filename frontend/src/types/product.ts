import {createPage, Page, PageResponse} from "@_types/page"

export interface Product {
    id: number
    name: string
    description: string
    slug: string
    sku: string
    price: number
    salePrice: number
    stock: number
    brand: string
    category: string
    attributes: Attribute[]
    images: Image[]
    isActive: true
}

export interface Image {
    id: number
    url: string
    sortOrder: number
    altText: string
}

export type Attribute = {
    name: string,
    values: Array<string | number | boolean>
}

export type SortOrder = "TOP" | "PRICE_ASC" | "PRICE_DESC"


export interface ProductQuery extends Page {
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
        ...createPage(),
    }
}

export interface ProductResponse {
    content: Product[]
    page: PageResponse
}