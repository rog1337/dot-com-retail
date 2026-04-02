import axios, {InternalAxiosRequestConfig} from "axios"
import {tokenManager} from "@lib/auth/tokenManager"
import { logger as log } from "@lib/logger"
import {useToastStore} from "@store/toastStore"

const API_URL = typeof window === "undefined" && process.env.NODE_ENV !== "development"
    ? process.env.BACKEND_URL
    : process.env.NEXT_PUBLIC_API_URL

export const AUTHORIZATION_HEADER_BEARER = "Bearer"
let isRefreshing = false
let failedQueue: { resolve: (value: unknown) => void; reject: (reason?: unknown) => void }[] = []

const processQueue = (error: unknown) => {
    failedQueue.forEach(p => error ? p.reject(error) : p.resolve(null))
    failedQueue = []
}

export const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    proxy: false,
    headers: {
        "Content-Type": "application/json",
    },
})

api.interceptors.request.use(
    config => {
        log.d("[API] [REQUEST]", {
            method: config?.method?.toUpperCase(),
            url: axios.getUri(config),
        })

        setBearerToken(config)
        return config
    },

    error => Promise.reject(error)
)

api.interceptors.response.use(
    response => {
        log.api(response?.config?.method ? response.config.method : "", axios.getUri(response.config), response.status, response.data)
        return response.data
    },

    async error => {
        log.api(error.config.method, axios.getUri(error.config), error.status ? error.status : error.code, error.response?.data ? error.response.data : null)

        if (error?.response?.data?.code === "JWT_ACCESS_REVOKED" || error?.response?.data?.code === "JWT_EXPIRED") {
            const originalRequest = error.config
            if (originalRequest._retry) {
                return Promise.reject(error)
            }

            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({resolve, reject})
                }).then(() => api(originalRequest))
                    .catch(e => Promise.reject(e))
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                const refreshHandler = tokenManager.getRefreshHandler()
                if (!refreshHandler) return Promise.reject(error)

                const success = await refreshHandler()
                if (!success) return Promise.reject(error)

                processQueue(null)
                return api(originalRequest)
            } catch(refreshError) {
                processQueue(refreshError)
                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        } else if (error?.status === 429 ) {
            const toast = useToastStore.getState()
            toast.show("Too many requests", "error")
        }
        return Promise.reject(error)
    }
)

function setBearerToken(config: InternalAxiosRequestConfig) {
    const token = tokenManager.getAccessToken()
    if (token) {
        config.headers.Authorization = `${AUTHORIZATION_HEADER_BEARER} ${token}`
    }
    return config
}

export function buildMultipartJsonPart(obj: unknown) {
    return new Blob([JSON.stringify(obj)], { type: "application/json" })
}

export default api