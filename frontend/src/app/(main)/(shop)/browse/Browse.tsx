"use client"

import FilterWindow from "@components/shop/FilterWindow"
import GridItem from "@components/shop/GridItem"
import {Product, ProductQuery} from "@_types/product"
import {PageMetadata} from "@_types/page"
import TopBar from "@components/shop/TopBar"
import {useCartStore} from "@store/cartStore"
import {cartApi} from "@lib/api/cartApi"
import { useAuth } from "@lib/auth/authContext"
import {useToastStore} from "@store/toastStore"
import {useEffect, useState} from "react"
import ListItem from "@/src/components/shop/ListItem"

export type View = "list" | "grid"

export default function Browse({params, products, page} : { params: ProductQuery, products: Product[], page: PageMetadata}) {
  const { items, setCart, getItemQuantity, isLoading, setIsLoading } = useCartStore()
  const { show } = useToastStore()
  const { sessionId, setSessionId } = useAuth()
  const [view, setView] = useState<View>("grid")

  useEffect(() => {
    const init = () => {
      const view = localStorage.getItem("view")
      if (view === "list" || view === "grid") setView(view)
    }
    init()
  }, [])

  async function handleAddToCart(product: Product) {
    setIsLoading(true)
    try {
      let quantity = getItemQuantity(product.id)
      if (quantity) quantity += 1
      else quantity = 1

      const newItems = [
        ...items.filter((i) => i.productId !== product.id),
        { productId: product.id, quantity: quantity },
      ]

      const cart = await cartApi.updateCart(sessionId, { items: newItems })
      setCart(cart)
      setSessionId(cart.sessionId)
      show("Product added to cart")
    } catch (e) {
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

  const handleSetView = (view: View) => {
    localStorage.setItem("view", view)
    setView(view)
  }

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-4 py-6">
        <div className="lg:flex sm-flex lg:gap-6">
          <aside className="hidden w-64 flex-shrink-0 lg:block">
            <FilterWindow urlParams={params} />
          </aside>

          <main className="mx-auto min-w-0 flex-1 pb-24 lg:pb-0">
            <TopBar
              pageMeta={page}
              params={params}
              view={view}
              onViewChange={handleSetView}
            />

            {view === "grid" && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 justify-items-center sm:gap-6 lg:grid-cols-3">
                {products.map((p: Product, key: number) => (
                  <GridItem
                    key={key}
                    product={p}
                    onAddToCart={(product: Product) => handleAddToCart(product)}
                    isLoading={isLoading}
                  />
                ))}
              </div>
            )}

            {view === "list" && (
              <div className="flex flex-col gap-3">
                {products.map((p: Product, key: number) => (
                  <ListItem
                    key={key}
                    product={p}
                    onAddToCart={(product: Product) => handleAddToCart(product)}
                    isLoading={isLoading}
                  />
                ))}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  )
}