import api from "@lib/api/api"
import {FilterQueryParams, FilterResponse} from "@_types/filter"

export const filterPaths = {
    base: "/filter",
}

export const filterApi = {
    getFilters: (params: FilterQueryParams): Promise<FilterResponse> => api.get(filterPaths.base, { params })
}