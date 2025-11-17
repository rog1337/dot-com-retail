import axios, {InternalAxiosRequestConfig} from "axios";
import {tokenManager} from "@lib/auth/tokenManager";
import {AUTHORIZATION_HEADER_BEARER} from "@constants/auth";
import { logger as log } from "@lib/logger";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    }
});

api.interceptors.request.use(
    config => {
        log.d("[API]", {
            method: config?.method?.toUpperCase(),
            url: config.baseURL
        });

        setBearerToken(config)
        return config;
    },

    error => Promise.reject(error)
);

api.interceptors.response.use(
    response => {
        // log.api()
        return response;
    },

    error => {
        log.api(error.response.config.method, error.response.config.baseURL, error.status)
        return Promise.reject(error);
    }
)

function setBearerToken(config: InternalAxiosRequestConfig) {
    const token = tokenManager.getAccessToken();
    if (token) {
        config.headers.Authorization = `${AUTHORIZATION_HEADER_BEARER} ${token}`;
    }
    return config;
}

export default api;