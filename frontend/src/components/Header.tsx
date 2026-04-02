"use client"
import { useEffect, useRef, useState } from "react"
import { useTheme } from "next-themes"
import {
  LoaderPinwheel,
  LogIn,
  LogOut,
  Menu,
  Moon,
  Search,
  ShoppingCart,
  Sun,
  User,
  X,
} from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useAuth } from "@lib/auth/authContext"
import UserMenu from "@components/UserMenu"
import { useCartStore } from "@store/cartStore"
import CartPreview from "@components/cart/CartPreview"
import { productApi } from "@lib/api/productApi"
import { logger } from "@lib/logger"
import { Product } from "@_types/product"
import SearchDropdown from "@components/shop/SearchDropdown"

export default function Header() {
  const { isLoggedIn, logout } = useAuth()
  const [searchQuery, setSearchQuery] = useState("")
  const [searchResults, setSearchResults] = useState<Product[]>([])
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [cartOpen, setCartOpen] = useState(false)
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = useState(false)
  const router = useRouter()
  const { items } = useCartStore()
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    const init = () => {
      setMounted(true)
    }
    init()
  }, [])

  if (!mounted) return null

  const fetchProducts = async (search: string) => {
    try {
      const query = { search: search.trim(), page: 0, size: 10 }
      return await productApi.getByQuery(query)
    } catch (e) {
      logger.d("Error fetching products by text search", e)
    }
  }

  const handleChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault()
    const value = e.target.value
    setSearchQuery(value)

    if (debounceRef.current) clearTimeout(debounceRef.current)

    if (value.length < 2) {
      setDropdownOpen(false)
      setSearchResults([])
      return
    }

    debounceRef.current = setTimeout(async () => {
      const products = await fetchProducts(value)
      if (products) {
        setSearchResults(products.content ?? products)
        setDropdownOpen(true)
      }
    }, 300)
  }

  const handleSearchButton = async (e: React.MouseEvent) => {
    e.preventDefault()
    submitSearch()
  }

  const submitSearch = () => {
    if (!searchQuery.trim()) return
    setDropdownOpen(false)
    router.push(`/browse?search=${encodeURIComponent(searchQuery.trim())}`)
  }

  const handleKeyPress = async (e: React.KeyboardEvent) => {
    if (e.key === "Enter") submitSearch()
  }

  const searchBox = (placeholder: string) => (
    <div className="relative w-full">
      <input
        type="text"
        value={searchQuery}
        onChange={handleChange}
        onKeyDown={handleKeyPress}
        placeholder={placeholder}
        className="w-full rounded-lg border border-slate-700 bg-slate-800 px-4 py-2.5 pr-12 text-white placeholder-slate-400 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 focus:outline-none"
      />
      <button
        onClick={handleSearchButton}
        className="absolute top-1/2 right-2 -translate-y-1/2 rounded-md p-2 transition-colors hover:bg-slate-700"
      >
        <Search className="h-5 w-5 text-slate-400" />
      </button>
      <SearchDropdown
        query={searchQuery}
        results={searchResults}
        isOpen={dropdownOpen}
        onClose={() => setDropdownOpen(false)}
        onSearchSubmit={submitSearch}
      />
    </div>
  )

  return (
    <header className="top-0 z-50 w-full bg-slate-900 text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between md:h-20">
          <Link href={"/"}>
            <div className="flex flex-shrink-0 items-center space-x-2">
              <LoaderPinwheel />
              <span className="text-xl font-bold sm:block md:text-2xl">tyre-dot-com</span>
            </div>
          </Link>

          <div className="mx-8 hidden max-w-2xl flex-1 md:flex">{searchBox("Search products")}</div>

          <div className="hidden items-center space-x-4 md:flex">
            <button
              className="cursor-pointer"
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            >
              {theme === "dark" ? <Sun /> : <Moon />}
            </button>

            {isLoggedIn ? (
              <div className="mt-2">
                <UserMenu />
              </div>
            ) : (
              <button
                onClick={() => router.push("/login")}
                className="flex items-center space-x-2 rounded-lg px-4 py-2 transition-colors hover:bg-slate-800"
              >
                <LogIn className="h-5 w-5" />
                <span className="text-sm">Login / Register</span>
              </button>
            )}

            <CartButton itemCount={items.length} onClick={() => setCartOpen(true)} />
          </div>

          <div className="flex items-center space-x-2 md:hidden">
            <CartButton itemCount={items.length} onClick={() => setCartOpen(true)} />

            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="rounded-lg p-2 transition-colors hover:bg-slate-800"
            >
              {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        <div className="pb-4 md:hidden">{searchBox("Search products")}</div>

        {mobileMenuOpen && (
          <div className="border-t border-slate-800 py-4 md:hidden">
            {isLoggedIn ? (
              <div>
                <button
                  onClick={() => {
                    router.push("/account")
                    setMobileMenuOpen(false)
                  }}
                  className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 transition-colors hover:bg-slate-800"
                >
                  <User />
                  <span>My account</span>
                </button>
                <button
                  onClick={() => {
                    logout()
                    setMobileMenuOpen(false)
                  }}
                  className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 transition-colors hover:bg-slate-800"
                >
                  <LogOut />
                  <span>Log out</span>
                </button>
              </div>
            ) : (
              <button
                onClick={() => {
                  router.push("/login")
                  setMobileMenuOpen(false)
                }}
                className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 transition-colors"
              >
                <LogIn className="h-5 w-5" />
                <span>Login / Register</span>
              </button>
            )}

            <button
              className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 transition-colors"
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            >
              <>
                {theme === "dark" ? <Sun /> : <Moon />}
                <p>{theme === "dark" ? "Light mode" : "Dark mode"}</p>
              </>
            </button>

          </div>
        )}
      </div>

      <CartPreview isOpen={cartOpen} onClose={() => setCartOpen(false)} />
    </header>
  )
}

function CartButton({ itemCount, onClick }: { itemCount: number; onClick: () => void }) {
  return (
    <button onClick={onClick} className="relative" aria-label="Open cart">
      <ShoppingCart />
      {itemCount > 0 && (
        <span className="absolute -top-1.5 -right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-blue-500 text-[10px] leading-none font-bold text-white">
          {itemCount > 9 ? "9+" : itemCount}
        </span>
      )}
    </button>
  )
}