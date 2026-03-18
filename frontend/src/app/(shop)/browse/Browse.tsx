"use client"

import FilterWindow from "@components/shop/FilterWindow"
import ProductItem from "@components/shop/ProductItem"
import {Product, ProductQuery} from "@_types/product"
import {PageResponse} from "@_types/page"
import TopBar from "@components/shop/TopBar"
import {useCartStore} from "@store/cartStore"
import {cartApi} from "@lib/api/cartApi"
import { useAuth } from "@/src/lib/auth/authContext"
import {useToastStore} from "@store/toastStore"

export default function Browse({params, products, page} : { params: ProductQuery, products: Product[], page: PageResponse}) {

    const {items, setCart, getItemQuantity, isLoading, setIsLoading } = useCartStore()
    const { show } = useToastStore()
    const {sessionId, setSessionId} = useAuth()

    async function handleAddToCart(product: Product) {
        setIsLoading(true)
        try {
            let quantity = getItemQuantity(product.id)
            if (quantity) quantity += 1
            else quantity = 1

            const newItems = [
                ...items.filter((i) => i.productId !== product.id),
                { productId: product.id, quantity: quantity }
            ]

            const cart = await cartApi.updateCart(sessionId, {items: newItems})
            setCart(cart)
            setSessionId(cart.sessionId)
            show("Product added to cart")
        } catch(e: any) {
            const code = e.response?.data?.code
            if (code === "PRODUCT_INSUFFICIENT_STOCK") {
                show("Can't add this product to cart, not enough in stock", "error", 5000)
                setIsLoading(false)
                return
            }
            console.log("Error adding to cart: ", e)
        }
        setIsLoading(false)
    }

    return (
        <div className="min-h-screen">
            <div className="max-w-7xl mx-auto px-4 py-6">
                <div className="lg:flex lg:gap-6">
                    <aside className="hidden lg:block w-64 flex-shrink-0">
                        <FilterWindow urlParams={params}/>
                    </aside>

                    <main className="flex-1 min-w-0 pb-24 lg:pb-0">

                        <TopBar page={page} params={params}/>

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                            {products.map((p: any) => (
                                <ProductItem
                                    product={p}
                                    key={p.id}
                                    onAddToCart={(product: Product) => handleAddToCart(product)}
                                    isLoading={isLoading}
                                />
                            ))}
                        </div>
                    </main>

                </div>
            </div>
        </div>
    )
}