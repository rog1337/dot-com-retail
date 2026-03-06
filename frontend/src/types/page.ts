export interface Page {
    page: number
    pageSize: number
}

export interface PageResponse {
    page: number
    size: number
    elements: number
    totalElements: number
    totalPages: number
    isLast: boolean
    isFirst: boolean
}

export function createPage(): Page {
    return { page: PAGE_DEFAULT, pageSize: PAGE_SIZE_DEFAULT }
}

export const PAGE_DEFAULT = 0
export const PAGE_SIZE_DEFAULT = 20