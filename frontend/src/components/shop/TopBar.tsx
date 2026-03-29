import {PageMetadata} from "@_types/page"
import {ChevronLeft, ChevronRight} from "lucide-react"
import {ProductQuery, SortOrder} from "@_types/product"
import qs from "qs"
import {compactParams} from "@lib/params"
import {useRouter} from "next/navigation"

export default function TopBar({page: paging, params}: { page: PageMetadata, params: ProductQuery }) {

    const { page, elements, totalElements,size, totalPages,isLast, isFirst } = paging

    const { sort } = params

    const router = useRouter()

    const sortOptions = [
        { value: "TOP", label: "Top" },
        { value: "PRICE_ASC", label: "Price ascending" },
        { value: "PRICE_DESC", label: "Price descending" },
    ]

    const handlePageChange = (page: number) => {
        if (page < 0) return
        params.page = page
        const compactedParams = compactParams(params)
        const query = qs.stringify(compactedParams, { arrayFormat: "indices", allowDots: true })
        router.push(`browse?${query}`)
    }

    const handleSortChange = (newSort: SortOrder) => {
        params.sort = newSort
        const compactedParams = compactParams(params)
        const query = qs.stringify(compactedParams, { arrayFormat: "indices", allowDots: true })
        router.push(`browse?${query}`)
    }


    function pagingComp() {
        let currentPage
        return (
            <div className="flex items-center gap-2">
                <button
                    onClick={() => handlePageChange(page - 1)}
                    disabled={page === 0}
                    className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    aria-label="Previous page"
                >
                    <ChevronLeft className="w-4 h-4" />
                </button>

                <span className="text-sm text-gray-700 min-w-[80px] text-center">
          Page {page + 1} of {totalPages}
        </span>

                <button
                    onClick={() => handlePageChange(page + 1)}
                    disabled={page + 1 === totalPages}
                    className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    aria-label="Next page"
                >
                    <ChevronRight className="w-4 h-4" />
                </button>
            </div>
        )
    }

    function sortingComp() {
        return (
            <div className="flex items-center gap-2">
                <label htmlFor="sort" className="text-sm text-gray-700 whitespace-nowrap">
                    Sort by:
                </label>
                <select
                    id="sort"
                    value={sort}
                    onChange={(e) => handleSortChange(e.target.value as SortOrder)}
                    className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                    {sortOptions.map(option => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </div>
        )
    }

    return (
        <div className=" rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                {/* Product count */}
                <div className="text-sm text-gray-600">
                    Showing {elements} of {totalElements} products
                </div>

                {pagingComp()}

                {/*<div className="flex items-center gap-4">*/}
                {/*    /!* Sorting *!/*/}
                {/*    <SortingBar sortBy={sortBy} />*/}

                {/*    /!* Desktop Pagination - Compact inline version *!/*/}
                {/*    <div className="hidden lg:block">*/}
                {/*        <Pagination*/}
                {/*            currentPage={data.currentPage}*/}
                {/*            totalPages={data.totalPages}*/}
                {/*            compact={true}*/}
                {/*        />*/}
                {/*    </div>*/}
                {/*</div>*/}

                {sortingComp()}
            </div>
        </div>
    )
}

