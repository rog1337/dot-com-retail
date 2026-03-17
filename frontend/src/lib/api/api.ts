import axios, {InternalAxiosRequestConfig} from "axios"
import {tokenManager} from "@lib/auth/tokenManager"
import { logger as log } from "@lib/logger"

const API_URL = typeof window === "undefined"
    ? process.env.BACKEND_URL
    : process.env.NEXT_PUBLIC_API_URL

export const AUTHORIZATION_HEADER_BEARER = "Bearer"

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

    error => {
        log.api(error.config.method, axios.getUri(error.config), error.status ? error.status : error.code, error.response?.data ? error.response.data : null)
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

export default api