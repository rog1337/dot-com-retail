"use client"

import Link from "next/link"
import { useEffect, useRef } from "react"
import { Search } from "lucide-react"
import { Product } from "@_types/product"

interface SearchDropdownProps {
  query: string
  results: Product[]
  isOpen: boolean
  onClose: () => void
  onSearchSubmit: () => void
}

export default function SearchDropdown({
  query,
  results,
  isOpen,
  onClose,
  onSearchSubmit,
}: SearchDropdownProps) {
  const dropdownRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        onClose()
      }
    }
    if (isOpen) document.addEventListener("click", handler)
    return () => document.removeEventListener("click", handler)
  }, [isOpen, onClose])

  if (!isOpen || query.length < 2) return null

  const resultItems =
    results.length === 0 ? (
      <p className="py-3 text-center text-sm text-gray-400 dark:text-slate-400">
        No results for &ldquo;{query}&rdquo;
      </p>
    ) : (
      <ul>
        {results.map((product) => (
          <li key={product.id}>
            <Link
              href={`/products/${product.id}`}
              className="group flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors hover:bg-gray-100 dark:hover:bg-slate-700"
            >
              <Search className="h-3.5 w-3.5 shrink-0 text-gray-400 dark:text-slate-500" />
              <span className="truncate text-sm text-gray-800 transition-colors group-hover:text-blue-600 dark:text-slate-200 dark:group-hover:text-blue-400">
                {product.name}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    )

  const seeAllButton = results.length > 0 && (
    <div className="mt-2 border-t border-gray-100 pt-2 dark:border-slate-700">
      <button
        onClick={onSearchSubmit}
        className="w-full py-1 text-center text-sm text-blue-500 transition-colors hover:text-blue-700 dark:hover:text-blue-400"
      >
        See all results for &ldquo;{query}&rdquo;
      </button>
    </div>
  )

  return (
    <div
      ref={dropdownRef}
      className="absolute top-full right-0 left-0 z-50 mt-1 overflow-hidden rounded-xl border border-gray-200 bg-white p-1 shadow-lg dark:border-slate-700 dark:bg-slate-800"
    >
      {resultItems}
      {seeAllButton}
    </div>
  )
}