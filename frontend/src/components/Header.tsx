"use client"
import {useEffect, useState} from 'react'
import {useTheme} from "next-themes"
import {Search, ShoppingCart, User, LogIn, Menu, X, LogOut, LoaderPinwheel, Moon, Sun} from 'lucide-react'
import Link from "next/link"
import {useRouter} from "next/navigation";
import { useAuth } from "@lib/auth/authContext"
import UserMenu from "@components/UserMenu";

export default function Header() {
    const { isLoggedIn, logout } = useAuth()
    const [cartCount, setCartCount] = useState(3)
    const [searchQuery, setSearchQuery] = useState('')
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const {theme, setTheme } = useTheme()
    const [mounted, setMounted] = useState(false)
    const router = useRouter()

    useEffect(() => setMounted(true), [])

    if (!mounted) return null

    const handleSearch = () => {
        console.log('Searching for:', searchQuery)
    }

    const handleKeyPress = (e: any) => {
        if (e.key === 'Enter') {
            handleSearch()
        }
    }

    return (
        <header className="bg-slate-900 text-white shadow-lg w-full top-0 z-50">
            <div className="container mx-auto px-4">
                <div className="flex items-center justify-between h-16 md:h-20">
                    <Link href={"/"}>
                        <div className="flex items-center space-x-2 flex-shrink-0">
                            <LoaderPinwheel/>
                            <span className="text-xl md:text-2xl font-bold sm:block">
                                tyre-dot-com
                            </span>
                        </div>
                    </Link>

                    <div className="hidden md:flex flex-1 max-w-2xl mx-8">
                        <div className="relative w-full">
                            <input
                                type="text"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyDown={handleKeyPress}
                                placeholder="Search for tyres by size, brand, or vehicle..."
                                className="w-full px-4 py-2.5 pr-12 rounded-lg bg-slate-800 border border-slate-700 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-white placeholder-slate-400"
                            />
                            <button
                                onClick={handleSearch}
                                className="absolute right-2 top-1/2 -translate-y-1/2 p-2 hover:bg-slate-700 rounded-md transition-colors"
                            >
                                <Search className="w-5 h-5 text-slate-400"/>
                            </button>
                        </div>
                    </div>

                    <div className="hidden md:flex items-center space-x-4">

                        <button
                            className="cursor-pointer"
                            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
                            >
                            {theme === "dark" ? <Sun/> : <Moon/>}
                        </button>


                        {isLoggedIn ? (
                                <div className="mt-2">
                                    <UserMenu/>
                                </div>
                        ) : (
                            <button
                                onClick={() => router.push("/login")}
                                className="flex items-center space-x-2 px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors"
                            >
                                <LogIn className="w-5 h-5"/>
                                <span className="text-sm">Login / Register</span>
                            </button>
                        )}

                        <Link
                            href="/cart"
                        >
                            <ShoppingCart/>
                        </Link>
                    </div>

                    <div className="flex md:hidden items-center space-x-2">
                        <Link href="/cart">
                            <ShoppingCart/>
                        </Link>
                        <button
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
                        >
                            {mobileMenuOpen ? <X className="w-6 h-6"/> : <Menu className="w-6 h-6"/>}
                        </button>
                    </div>
                </div>

                <div className="md:hidden pb-4">
                    <div className="relative">
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyDown={handleKeyPress}
                            placeholder="Search tyres..."
                            className="w-full px-4 py-2.5 pr-12 rounded-lg bg-slate-800 border border-slate-700 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 text-white placeholder-slate-400"
                        />
                        <button
                            onClick={handleSearch}
                            className="absolute right-2 top-1/2 -translate-y-1/2 p-2 hover:bg-slate-700 rounded-md transition-colors"
                        >
                            <Search className="w-5 h-5 text-slate-400"/>
                        </button>
                    </div>
                </div>

                {mobileMenuOpen && (
                    <div className="md:hidden border-t border-slate-800 py-4">
                        {isLoggedIn ? (
                            <div>
                                <button
                                    onClick={() => {
                                        router.push("/account")
                                        setMobileMenuOpen(false)
                                    }}
                                    className="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-lg transition-colors w-full"
                                >
                                    <User/>
                                    <span>My account</span>
                                </button>
                                <button
                                    onClick={() => {
                                        logout()
                                        setMobileMenuOpen(false)
                                    }}
                                    className="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-lg transition-colors w-full"
                                >
                                    <LogOut/>
                                    <span>Log out</span>
                                </button>
                            </div>

                        ) : (
                            <button
                                onClick={() => {
                                    router.push("/login")
                                    setMobileMenuOpen(false)
                                }}
                                className="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-lg transition-colors w-full"
                            >
                                <LogIn className="w-5 h-5"/>
                                <span>Login / Register</span>
                            </button>
                        )}
                    </div>
                )}
            </div>
        </header>
    )
}