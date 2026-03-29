"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { useAuth } from "@lib/auth/authContext"
import { Moon, Sun } from "lucide-react"
import { useTheme } from "next-themes"
import { adminApi } from "@/src/lib/api/adminApi"

const NAV = [
  { href: "/admin/dashboard", label: "Dashboard", icon: "▦" },
  { href: "/admin/products", label: "Products", icon: "◈" },
  { href: "/admin/categories", label: "Categories", icon: "◈" },
  { href: "/admin/brands", label: "Brands", icon: "◈" },
  { href: "/admin/orders", label: "Orders", icon: "◎" },
  { href: "/admin/users", label: "Users", icon: "◉" },
  { href: "/admin/reviews", label: "Reviews", icon: "◆" },
]

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()
  const router = useRouter()
  const { user, setUser } = useAuth()
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const { theme, setTheme } = useTheme()

  useEffect(() => {
    const fetchAdmin = async () => {
      if (!user) {
        router.replace("/")
        return
      }
      try {
        const _user = await adminApi.getUser(user.id)
        setUser(_user)
      } catch {
        router.replace("/")
      }
    }

    fetchAdmin()
  }, [])

  const active = (href: string) => pathname.startsWith(href)

  return (
    <div
      className="flex min-h-screen bg-white text-gray-900 dark:bg-[#0a0a0a] dark:text-[#e8e0d5]"
      style={{ fontFamily: "'IBM Plex Mono', monospace" }}
    >
      {/* Sidebar */}
      <aside
        className="flex flex-shrink-0 flex-col border-r border-gray-200 bg-white transition-all duration-300 dark:border-[#1e1e1e] dark:bg-[#0a0a0a]"
        style={{ width: sidebarOpen ? "220px" : "70px" }}
      >
        <div
          className="flex h-12 items-center gap-3 border-b border-gray-200 p-4 dark:border-[#1e1e1e]"
          onClick={() => setSidebarOpen((o) => !o)}
        >
          <div className="flex h-7 w-7 flex-shrink-0 items-center justify-center bg-[#c8a96e] text-xs font-bold text-black">
            ⌘
          </div>
          {sidebarOpen && (
            <div>
              <div className="text-xs font-bold tracking-widest text-[#c8a96e] uppercase">
                Admin
              </div>
              <div className="text-[10px] tracking-wider text-gray-400 dark:text-[#555]">
                Control Panel
              </div>
            </div>
          )}
          <p className="ml-auto text-xs text-gray-400 transition-colors hover:text-[#c8a96e] dark:text-[#444]">
            {sidebarOpen ? "◂" : "▸"}
          </p>
        </div>

        <nav className="flex-1 py-4">
          {NAV.map((item) => {
            const isActive = active(item.href)
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 border-l-2 px-4 py-2.5 text-xs transition-all ${
                  isActive
                    ? "border-[#c8a96e] bg-amber-50 text-[#c8a96e] dark:bg-[#c8a96e10]"
                    : "border-transparent text-gray-500 hover:bg-gray-50 hover:text-gray-800 dark:text-[#666] dark:hover:bg-[#ffffff05] dark:hover:text-[#aaa]"
                }`}
              >
                <span className="flex-shrink-0 text-sm">{item.icon}</span>
                {sidebarOpen && <span className="tracking-wider uppercase">{item.label}</span>}
              </Link>
            )
          })}
        </nav>

        <div className="border-t border-gray-200 p-4 dark:border-[#1e1e1e]">
          <div className="flex items-center gap-2">
            <div className="flex h-6 w-6 flex-shrink-0 items-center justify-center border border-gray-300 bg-gray-100 text-[10px] text-[#c8a96e] dark:border-[#333] dark:bg-[#1e1e1e]">
              {user?.displayName?.charAt(0).toUpperCase()}
            </div>
            {sidebarOpen && (
              <div className="min-w-0">
                <div className="truncate text-[10px] text-gray-500 dark:text-[#888]">
                  {user?.displayName}
                </div>
                <div className="text-[9px] tracking-widest text-[#c8a96e] uppercase">
                  {user?.role}
                </div>
              </div>
            )}
          </div>
        </div>
      </aside>

      <main className="flex min-w-0 flex-1 flex-col">
        <div className="flex items-center justify-between border-b border-gray-200 bg-white px-8 py-4 dark:border-[#1e1e1e] dark:bg-[#0a0a0a]">
          <div className="text-base tracking-widest text-gray-400 uppercase dark:text-[#444]">
            {NAV.find((n) => active(n.href))?.label ?? "Admin"}
          </div>
          <div className="flex items-center gap-4 text-[10px] text-gray-400 dark:text-[#444]">
            <span
              className="cursor-pointer"
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            >
              {theme === "dark" ? <Sun /> : <Moon />}
            </span>
            <span>
              {new Date().toLocaleDateString("en-GB", {
                day: "2-digit",
                month: "short",
                year: "numeric",
              })}
            </span>
            <Link
              href="/"
              className="tracking-wider uppercase transition-colors hover:text-[#c8a96e]"
            >
              ← Storefront
            </Link>
          </div>
        </div>
        <div className="flex-1 overflow-auto bg-gray-50 p-8 dark:bg-[#0a0a0a]">{children}</div>
      </main>
    </div>
  )
}
