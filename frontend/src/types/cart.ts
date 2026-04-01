import { ShippingType } from "@_types/order"
import {Image} from "@_types/product"

export interface CartItem {
  productId: number
  productName: string
  image: Image
  price: number
  quantity: number
}

export type Cart = {
  id: string
  sessionId: string
  items: CartItem[]
  subTotalPrice: number
  totalPrice: number
  shippingType: ShippingType | null
  shippingCost: number
  totalQuantity: number
}

export interface CartUpdateRequest {
  items: CartUpdateItem[] | null
  shippingType?: ShippingType | null
}

export interface CartUpdateItem {
  productId: number
  quantity: number
}

export interface CheckoutResponse {
  clientSecret: string
}