export interface Page<T> {
  content: T[]
  page: PageMetadata
}

export interface PageMetadata {
    page: number
    size: number
    elements: number
    totalElements: number
    totalPages: number
    isLast: boolean
    isFirst: boolean
}

export const defaultPageMetadata = {
    page: 0,
    size: 0,
    elements: 0,
    totalElements: 0,
    totalPages: 0,
    isFirst: false,
    isLast: false,
}

export interface PageParams {
    page: number
    size: number
}

export function createPageParams(): PageParams {
    return { page: PAGE_DEFAULT, size: PAGE_SIZE_DEFAULT }
}

export const PAGE_DEFAULT = 0
export const PAGE_SIZE_DEFAULT = 20