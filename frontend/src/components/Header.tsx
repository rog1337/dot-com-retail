"use client"
import {useEffect, useState} from 'react'
import {useTheme} from "next-themes"
import { Search, ShoppingCart, User, LogIn, Menu, X } from 'lucide-react'
import Link from "next/link"

export default function Header() {
    const [isLoggedIn, setIsLoggedIn] = useState(false)
    const [cartCount, setCartCount] = useState(3)
    const [searchQuery, setSearchQuery] = useState('')
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const {theme, setTheme } = useTheme()
    const [mounted, setMounted] = useState(false)

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
        <header className="bg-slate-900 text-white shadow-lg top-0 z-50">
            <div className="container mx-auto px-4">
                <div className="flex items-center justify-between h-16 md:h-20">
                    <Link href={"/"}>
                        <div className="flex items-center space-x-2 flex-shrink-0">
                            <div
                                className="w-10 h-10 md:w-12 md:h-12 bg-orange-500 rounded-full flex items-center justify-center">
                                <svg viewBox="0 0 24 24" fill="none" className="w-6 h-6 md:w-7 md:h-7">
                                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                    <circle cx="12" cy="12" r="6" stroke="currentColor" strokeWidth="2"/>
                                    <circle cx="12" cy="12" r="2" fill="currentColor"/>
                                </svg>
                            </div>
                            <span className="text-xl md:text-2xl font-bold hidden sm:block">
                                tyre-dot-com
                            </span>
                        </div>
                    </Link>


                    {/* Search Bar - Desktop */}
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
                            onClick={() => {
                                setTheme(theme === "dark" ? "light" : "dark")
                            }}
                            >
                            {theme === "dark" ? "Light Mode" : "Dark Mode"}
                        </button>


                        {isLoggedIn ? (
                            <button
                                onClick={() => setIsLoggedIn(false)}
                                className="flex items-center space-x-2 px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors"
                            >
                                <User className="w-5 h-5"/>
                                <span className="text-sm">My Account</span>
                            </button>
                        ) : (
                            <button
                                onClick={() => setIsLoggedIn(true)}
                                className="flex items-center space-x-2 px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors"
                            >
                                <LogIn className="w-5 h-5"/>
                                <span className="text-sm">Login / Register</span>
                            </button>
                        )}

                        <Link
                            href="/cart"
                        >
                            <ShoppingCart></ShoppingCart>
                        </Link>
                    </div>

                    {/* Mobile Menu Button */}
                    <div className="flex md:hidden items-center space-x-2">
                        <button className="relative p-2 hover:bg-slate-800 rounded-lg transition-colors">
                            <ShoppingCart className="w-6 h-6"/>
                            {cartCount > 0 && (
                                <span
                                    className="absolute -top-1 -right-1 bg-orange-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                                    {cartCount}
                                </span>
                            )}
                        </button>
                        <button
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                            className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
                        >
                            {mobileMenuOpen ? <X className="w-6 h-6"/> : <Menu className="w-6 h-6"/>}
                        </button>
                    </div>
                </div>

                {/* Mobile Search Bar */}
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

                {/* Mobile Menu */}
                {mobileMenuOpen && (
                    <div className="md:hidden border-t border-slate-800 py-4">
                        {isLoggedIn ? (
                            <button
                                onClick={() => {
                                    setIsLoggedIn(false)
                                    setMobileMenuOpen(false)
                                }}
                                className="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 rounded-lg transition-colors w-full"
                            >
                                <User className="w-5 h-5"/>
                                <span>My Account</span>
                            </button>
                        ) : (
                            <button
                                onClick={() => {
                                    setIsLoggedIn(true)
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