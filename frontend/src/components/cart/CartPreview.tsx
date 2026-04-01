"use client"

import { useEffect, useRef } from "react"
import { X, ShoppingCart } from "lucide-react"
import Link from "next/link"
import CartItem from "@components/cart/CartItem"
import { useCartStore } from "@store/cartStore"
import { useAuth } from "@lib/auth/authContext"
import { cartApi } from "@lib/api/cartApi"
import { logger as log } from "@lib/logger"

interface CartDrawerProps {
  isOpen: boolean
  onClose: () => void
}

export default function CartPreview({ isOpen, onClose }: CartDrawerProps) {
  const { isLoggedIn, sessionId } = useAuth()
  const { items, setCart, subTotal } = useCartStore()
  const overlayRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const fetchCart = async () => {
      try {
        const cart = await cartApi.getCart(sessionId)
        setCart(cart)
      } catch (e: any) {
        const code = e.response?.data?.code
        if (code === "CART_NOT_FOUND") return
        log.d("Failed to fetch cart", e)
      }
    }

    if (isOpen && (isLoggedIn || sessionId)) {
      fetchCart()
    }
  }, [isOpen])

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden"
    } else {
      document.body.style.overflow = ""
    }
    return () => {
      document.body.style.overflow = ""
    }
  }, [isOpen])

  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose()
    }
    window.addEventListener("keydown", handleKey)
    return () => window.removeEventListener("keydown", handleKey)
  }, [onClose])

  return (
    <>
      <div
        ref={overlayRef}
        onClick={onClose}
        className={`
                    fixed inset-0 z-40 bg-black/50 backdrop-blur-sm
                    transition-opacity duration-100
                    ${isOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"}
                `}
        aria-hidden="true"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-label="Shopping cart"
        className={`
                    fixed z-50 bg-background shadow-2xl
                    flex flex-col
                    transition-transform duration-100 ease-in-out

                    /* Mobile*/
                    bottom-0 left-0 right-0 max-h-[85dvh] rounded-t-2xl
                    ${isOpen ? "translate-y-0" : "translate-y-full"}

                    /* Desktop */
                    md:top-0 md:bottom-0 md:left-auto md:right-0 md:max-h-none md:h-full
                    md:w-[420px] md:rounded-none md:rounded-l-2xl
                    ${isOpen ? "md:translate-x-0" : "md:translate-x-full"}
                    md:translate-y-0
                `}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100 dark:border-slate-700 flex-shrink-0">
          <div className="flex items-center gap-2">
            <ShoppingCart className="w-5 h-5 text-blue-500" />
            <h2 className="font-semibold text-foreground text-lg">Your Cart</h2>
            {items.length > 0 && (
              <span className="text-sm font-medium bg-blue-500 px-2 py-0.5 rounded-full leading-none">
                                {items.length}
                            </span>
            )}
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-800 transition-colors text-foreground/80"
            aria-label="Close cart"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-5 min-h-0">
          {items.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full py-16 gap-3 text-foreground/80">
              <ShoppingCart className="w-12 h-12 opacity-30" />
              <p className="text-base">Your cart is empty</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-100 dark:divide-slate-700">
              {items.map((item) => (
                <CartItem key={item.productId} item={item} />
              ))}
            </div>
          )}
        </div>

        {items.length > 0 && (
          <div className="flex-shrink-0 px-5 py-4 border-t border-gray-100 dark:border-slate-700">
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm uppercase tracking-widest text-foreground/80 font-medium">Subtotal</span>
              <span className="text-xl font-bold text-foreground">€{subTotal.toFixed(2)}</span>
            </div>
            <Link
              href="/checkout"
              onClick={onClose}
              className="block w-full text-center px-5 py-3 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-xl transition-colors"
            >
              Go to Checkout
            </Link>
            <button
              onClick={onClose}
              className="block w-full text-center mt-2 px-5 py-2.5 text-sm text-foreground/80 hover:text-gray-700 dark:hover:text-slate-200 transition-colors"
            >
              Continue Shopping
            </button>
          </div>
        )}
      </div>
    </>
  )
}