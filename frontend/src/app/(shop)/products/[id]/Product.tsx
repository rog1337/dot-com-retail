"use client"

import {type Product} from "@_types/product"
import ProductItem from "@components/shop/ProductItem"
import Field from "@/src/app/account/Field"
import {cartApi} from "@lib/api/cartApi"
import {useCartStore} from "@store/cartStore"
import {useToastStore} from "@store/toastStore"
import {useAuth} from "@lib/auth/authContext"

export default function Product({product}: { product: Product }) {
    const { sessionId, setSessionId } = useAuth()
    const { items, setCart, getItemQuantity, isLoading, setIsLoading } = useCartStore()
    const { show } = useToastStore()

    const handleAddToCart = async (product: Product) => {
        setIsLoading(true)
        try {
            let quantity = getItemQuantity(product.id)
            if (quantity) quantity += 1
            else quantity = 1

            const newItems = [
                ...items.filter((i) => i.productId !== product.id),
                {productId: product.id, quantity: quantity}
            ]

            const cart = await cartApi.updateCart(sessionId, {items: newItems})
            setCart(cart)
            setSessionId(cart.sessionId)
            show("Product added to cart")
        } catch (e: any) {
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


    console.log(product)

    let i = 0
    const attrs: any = []
    const details = Object.entries(product).map(([key, value]) => {
        if (key === "brand") value = value.name
        if (key === "category") return null
        if (key === "images") return null
        if (key === "isActive") return null
        if (key === "attributes") {
            product[key].forEach((object) => {
                attrs.push(
                    <Field
                        key={i++}
                        label={object.name}
                        children={(
                            <p className="text-base">{object.values.join(", ")}</p>
                        )}
                    />
                )
            })
            return null
        }

        return (
            <Field
                key={i++}
                label={key.toString()}
                children={(
                    <p className="text-base">{value.toString()}</p>
                )}
            />
        )
    })

    details.push(attrs)

    return (
        <div className="flex flex-col justify-center p-5">
            <div className="flex justify-center">
                <ProductItem
                    product={product}
                    onAddToCart={handleAddToCart}
                    isLoading={isLoading}
                />
            </div>

            {details}

        </div>
    )
}