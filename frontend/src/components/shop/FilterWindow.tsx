"use client"
import {Car} from "lucide-react"
import Filter, {normalizeFilters} from "@components/shop/Filter"
import {useRouter} from "next/navigation"
import {useEffect, useState} from "react"
import {FilterField} from "@_types/filter"
import {
    Attribute,
    ProductQuery,
} from "@_types/product"
import qs from "qs"
import Loading from "@components/Loading"
import {filterApi} from "@lib/api/filterApi"
import {logger as log} from "@lib/logger"
import {compactParams} from "@lib/params"

export default function FilterWindow({ urlParams } : { urlParams: ProductQuery }) {

    const router = useRouter()
    const [normalizedFilters, setNormalizedFilters] = useState<FilterField[]>([])
    const [params, setParams] = useState<ProductQuery>(urlParams)
    const [loading, setLoading] = useState<boolean>(true)

    useEffect(() => {
        const fetchFilters = async () => {
            try {
                const data = await filterApi.getFilters({categoryId: params.categoryId})
                log.d("Filters", data)

                setNormalizedFilters(normalizeFilters(data, params))
                setLoading(false)
            } catch (e: any) {
                log.debug(e)
            }
        }

        fetchFilters()
    }, [])

    const handleChange = (param: string, values: Array<string | number | boolean>) => {
        const newParams = {...params}

        if (param.startsWith("attr_")) {
            const attr = newParams.attributes.find(o => o.name == param.substring("attr_".length))
            if (!attr) {
                const newAttribute: Attribute = {name: param.substring("attr_".length), values: values}
                newParams.attributes.push(newAttribute)
            } else if (values.length < 1) {
                newParams.attributes = newParams.attributes.filter(o => o.name !== param.substring("attr_".length))
            } else {
                attr.values = values
            }
        }
        const key: keyof ProductQuery = param as keyof ProductQuery

        // @ts-ignore
        newParams[key] = values

        setParams(newParams)
        handleSearch(newParams)
    }

    const handleSearch = async (params: ProductQuery) => {
        const compactedParams = compactParams(params)
        console.log(compactedParams)
        const query = qs.stringify(compactedParams, { arrayFormat: "comma", allowDots: true, encode: false })
        router.push(`browse?${query}`)
    }

    if (loading) {
        return <Loading/>
    }

    return (
        <div className="sticky top-6 rounded-lg shadow-sm border border-gray-200 p-6 max-h-[calc(100vh-3rem)] overflow-y-auto">
            <div className="h-full">
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center justify-between">
                        <Car className="w-8 h-8 text-orange-500" />
                        <span className="ml-5 text-xl">Filters</span>
                    </div>
                </div>

                {normalizedFilters.map((filter) =>
                    <Filter
                        filter={filter}
                        key={filter.name}
                        onChange={(param: any, values: any) => handleChange(param, values)}
                    />)}
            </div>
        </div>
    )
}