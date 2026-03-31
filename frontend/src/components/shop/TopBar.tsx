import {PageMetadata} from "@_types/page"
import { ChevronLeft, ChevronRight, LayoutGrid, List } from "lucide-react"
import {ProductQuery, SortOrder} from "@_types/product"
import qs from "qs"
import {compactParams} from "@lib/params"
import {useRouter} from "next/navigation"
import {View} from "@/src/app/(main)/(shop)/browse/Browse"

type TopBarProps = {
  pageMeta: PageMetadata
  params: ProductQuery
  view: View,
  onViewChange: (view: View) => void
}

export default function TopBar({ pageMeta, params, view, onViewChange }: TopBarProps) {
  const { page, elements, totalElements, totalPages } = pageMeta

  const router = useRouter()

  const sortOptions = [
    { value: "TOP", label: "Top" },
    { value: "PRICE_ASC", label: "Price ascending" },
    { value: "PRICE_DESC", label: "Price descending" },
  ]

  const handlePageChange = (page: number) => {
    if (page < 0) return
    const compactedParams = compactParams({...params, page})
    const query = qs.stringify(compactedParams, { arrayFormat: "indices", allowDots: true })
    router.push(`browse?${query}`)
  }

  const handleSortChange = (newSort: SortOrder) => {
    const compactedParams = compactParams({...params, sort: newSort})
    const query = qs.stringify(compactedParams, { arrayFormat: "indices", allowDots: true })
    router.push(`browse?${query}`)
  }

  function pagingComp() {
    return (
      <div className="flex items-center gap-2">
        <button
          onClick={() => handlePageChange(page - 1)}
          disabled={page === 0}
          className="rounded-lg border border-gray-300 p-2 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          aria-label="Previous page"
        >
          <ChevronLeft className="h-4 w-4" />
        </button>

        <span className="min-w-[80px] text-center text-sm text-gray-700">
          Page {page + 1} of {totalPages}
        </span>

        <button
          onClick={() => handlePageChange(page + 1)}
          disabled={page + 1 === totalPages}
          className="rounded-lg border border-gray-300 p-2 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          aria-label="Next page"
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    )
  }

  function sortingComp() {
    return (
      <div className="flex items-center gap-2">
        <label htmlFor="sort" className="text-sm whitespace-nowrap text-gray-700">
          Sort by:
        </label>
        <select
          id="sort"
          value={params.sort}
          onChange={(e) => handleSortChange(e.target.value as SortOrder)}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none"
        >
          {sortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
    )
  }

  function gridAndListViewButtons() {
    return (
      <div className="flex items-center gap-2">
        <button
          onClick={() => onViewChange("list")}
          className={view === "grid" ? "opacity-50" : ""}>
          <List/>
        </button>
        <button
          onClick={() => onViewChange("grid")}
          className={view === "list" ? "opacity-50" : ""}>
          <LayoutGrid/>
        </button>
      </div>
    )
  }

  return (
    <div className="mb-6 rounded-lg border border-gray-200 p-4 shadow-sm">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">

        <div className="text-sm text-gray-600">
          Showing {elements} of {totalElements} products
        </div>

        {pagingComp()}
        <div className="flex gap-5">
          {gridAndListViewButtons()}
          {sortingComp()}
        </div>

      </div>
    </div>
  )
}