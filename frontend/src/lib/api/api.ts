import axios, {InternalAxiosRequestConfig} from "axios";
import {tokenManager} from "@lib/auth/tokenManager";
import {AUTHORIZATION_HEADER_BEARER} from "@constants/auth";

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    }
});

api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = tokenManager.getAccessToken();
        if (token) {
            config.headers.Authorization = `${AUTHORIZATION_HEADER_BEARER} ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);



export default api;