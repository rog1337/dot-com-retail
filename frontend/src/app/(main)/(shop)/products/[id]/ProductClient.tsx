"use client"

import { useState } from "react"
import Image from "next/image"
import Link from "next/link"
import { ChevronRight, Layers, Package, ShoppingCart, Tag } from "lucide-react"
import { type Product } from "@_types/product"
import { cartApi } from "@lib/api/cartApi"
import { useCartStore } from "@store/cartStore"
import { useToastStore } from "@store/toastStore"
import { useAuth } from "@lib/auth/authContext"
import ReviewSection from "@components/shop/ReviewSection"

const INLINE_FIELDS = new Set(["price", "stock", "sku", "weight"])
const SKIP_FIELDS = new Set([
  "id",
  "name",
  "description",
  "images",
  "isActive",
  "brand",
  "category",
  "attributes",
])

export default function ProductClient({
  product,
  hasPurchased = false,
}: {
  product: Product
  hasPurchased?: boolean
}) {
  const { sessionId, setSessionId } = useAuth()
  const { items, setCart, getItemQuantity, isLoading, setIsLoading } = useCartStore()
  const { show } = useToastStore()

  const images = product.images ?? []
  const [activeImage, setActiveImage] = useState(0)

  const handleAddToCart = async () => {
    setIsLoading(true)
    try {
      let quantity = getItemQuantity(product.id)
      quantity = quantity ? quantity + 1 : 1
      const newItems = [
        ...items.filter((i) => i.productId !== product.id),
        { productId: product.id, quantity },
      ]
      const cart = await cartApi.updateCart(sessionId, { items: newItems })
      setCart(cart)
      setSessionId(cart.sessionId)
      show("Product added to cart")
    } catch (e: any) {
      const code = e.response?.data?.code
      if (code === "PRODUCT_INSUFFICIENT_STOCK") {
        show("Can't add this product to cart, not enough in stock", "error", 5000)
      } else {
        console.error("Error adding to cart:", e)
      }
    } finally {
      setIsLoading(false)
    }
  }

  const specEntries = Object.entries(product).filter(([key]) => !SKIP_FIELDS.has(key))

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-6xl px-4 py-8">
        <nav
          aria-label="Breadcrumb"
          className="text-foreground/60 mb-6 flex items-center gap-1.5 text-sm"
        >
          <Link href="/browse" className="hover:text-foreground/50 transition-colors">
            Shop
          </Link>
          {product.category && (
            <>
              <ChevronRight className="h-3.5 w-3.5" />
              <Link
                href={`/browse?categoryId=${product.category.id}`}
                className="hover:text-foreground/50 transition-colors"
              >
                {product.category.name}
              </Link>
            </>
          )}
          <ChevronRight className="h-3.5 w-3.5" />
          <span className="text-foreground max-w-[200px] truncate">{product.name}</span>
        </nav>

        <div className="grid grid-cols-1 gap-8 lg:grid-cols-2 lg:gap-12">
          <div className="flex flex-col gap-3">
            <div className="relative aspect-square w-full overflow-hidden rounded-2xl border border-gray-100 shadow-sm">
              {images.length > 0 ? (
                <Image
                  src={images[activeImage].url}
                  alt={`${product.name} — image ${activeImage + 1}`}
                  fill
                  className="object-contain p-4 transition-opacity duration-200"
                  sizes="(max-width: 1024px) 100vw, 50vw"
                  priority
                />
              ) : (
                <div className="flex h-full items-center justify-center">
                  <Package className="h-20 w-20" />
                </div>
              )}
            </div>

            {images.length > 1 && (
              <div className="flex gap-2 overflow-x-auto pb-1">
                {images.map((src, idx) => (
                  <button
                    key={idx}
                    onClick={() => setActiveImage(idx)}
                    aria-label={`View image ${idx + 1}`}
                    className={`relative h-16 w-16 flex-shrink-0 overflow-hidden rounded-lg border-2 transition-all ${
                      idx === activeImage
                        ? "border-gray-900 opacity-100"
                        : "border-transparent opacity-60 hover:opacity-90"
                    }`}
                  >
                    <Image
                      src={src.url}
                      alt={`${product.name} thumbnail ${idx + 1}`}
                      fill
                      className="object-cover"
                      sizes="64px"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="flex flex-col">
            {product.brand && (
              <p className="text-foreground/70 mb-1 text-sm font-medium tracking-widest uppercase">
                {product.brand.name}
              </p>
            )}

            <h2 className="text-2xl leading-snug font-bold sm:text-3xl">{product.name}</h2>

            {product.price != null && (
              <p className="mt-3 text-3xl font-semibold">€{Number(product.price).toFixed(2)}</p>
            )}

            {product.stock != null && (
              <div className="mt-3">
                {product.stock > 0 ? (
                  <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                    {product.stock} in stock
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 rounded-full border border-red-200 bg-red-50 px-3 py-1 text-xs font-medium text-red-600">
                    <span className="h-1.5 w-1.5 rounded-full bg-red-500" />
                    Out of stock
                  </span>
                )}
              </div>
            )}

            {product.description && (
              <div className="mt-5 border-t border-gray-100 pt-5">
                <h3 className="mb-2 text-xs font-semibold tracking-wider uppercase">Description</h3>
                <p className="text-foreground/80 text-sm leading-relaxed">{product.description}</p>
              </div>
            )}

            {product.attributes?.length > 0 && (
              <div className="mt-5 border-t border-gray-100 pt-5">
                <h3 className="mb-3 flex items-center gap-1.5 text-xs font-semibold tracking-wider uppercase">
                  <Layers className="h-3.5 w-3.5" />
                  Attributes
                </h3>
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 sm:grid-cols-3">
                  {product.attributes.map((attr, i) => (
                    <div key={i} className="flex flex-col gap-0.5">
                      <dt className="text-xs">{attr.name}</dt>
                      <dd className="text-foreground/80 text-sm font-medium">
                        {attr.values.join(", ")}
                      </dd>
                    </div>
                  ))}
                </dl>
              </div>
            )}

            <div className="mt-6 border-t border-gray-100 pt-6">
              <button
                onClick={handleAddToCart}
                disabled={isLoading || product.stock === 0}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-gray-900 px-6 py-3.5 text-sm font-semibold text-white transition-all hover:bg-gray-700 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ShoppingCart className="h-4 w-4" />
                {isLoading ? "Adding…" : "Add to cart"}
              </button>
            </div>
          </div>
        </div>

        {specEntries.length > 0 && (
          <section className="mt-10 overflow-hidden rounded-2xl border border-gray-100 shadow-sm">
            <h3 className="flex items-center gap-2 border-b border-gray-100 px-6 py-4 text-sm font-semibold tracking-wider uppercase">
              <Tag className="h-3.5 w-3.5" />
              Specifications
            </h3>
            <dl>
              {specEntries.map(([key, value], i) => (
                <div
                  key={key}
                  className={`grid grid-cols-2 px-6 py-3 text-sm ${i % 2 === 0 ? "bg-background" : "bg-black/20"}`}
                >
                  <dt className="capitalize">{key.replace(/([A-Z])/g, " $1")}</dt>
                  <dd className="font-medium">
                    {typeof value === "object" && value !== null
                      ? JSON.stringify(value)
                      : String(value)}
                  </dd>
                </div>
              ))}
            </dl>
          </section>
        )}
        <ReviewSection product={product} />
      </div>
    </div>
  )
}