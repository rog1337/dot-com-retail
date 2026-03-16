import {AddressFields, Contact} from "@_types/contact"
import {type Page, PageResponse} from "@_types/page"

export interface Order {
    id: string,
    status: OrderStatus,
    paymentId: string,
    sessionId?: string,
    items: OrderItem[],
    shippingType: ShippingType,
    shippingCost: number,
    totalAmount: number,
    contact: Contact,
    date: number
}

export interface OrderItem {
    productId: string,
    productName: string,
    imageUrl: string,
    quantity: number,
    price: number,
    totalAmount: number,
}

export interface CreateOrderResponse {
    id: string
    status: string
    clientSecret: string
}

export interface SubmitOrderRequest {
    name: string
    email: string
    phone: string
    address: AddressFields,
    shippingType: ShippingType
    notes?: string
}

export interface OrderResponse {
    content: Order[]
    page: PageResponse
}

export enum OrderStatus { PENDING_PAYMENT, PAID, SHIPPED, DELIVERED, CANCELLED }
export enum ShippingType { STANDARD = "STANDARD", EXPRESS = "EXPRESS"}

export interface OrderParams extends Page {
    status: OrderStatus
    sort: "desc" | "asc"
}