import { Type } from "@components/shop/Filter"

export interface FilterQueryParams {
  categoryId: number
}

export interface FilterResponse {
  category: FilterCategory
  attributes: FilterAttribute[]
  brands: FilterBrand[]
}

export interface FilterBrand {
  id: number
  name: string
  count: number
}

export interface FilterCategory {
  id: number
  name: string
}

export interface FilterAttribute {
  attribute: string
  displayOrder: number
  id: number
  label: string
  filterType: Type
  values: AttributeValue[]
}

export interface AttributeValue {
  value: string | number | boolean
  count: number
  id: number
  isEnabled?: boolean
}

export interface FilterField {
  name: string
  label: string
  values: AttributeValue[]
  filterType: Type
}