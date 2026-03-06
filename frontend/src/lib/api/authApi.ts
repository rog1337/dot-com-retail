import api from "./api"
import {LoginCredentials, RegisterData} from "@/src/types/auth"

export const authPaths = {
    login: "/auth/login",
    register: "/auth/register",
    logout: "/auth/logout",
    refresh: "/auth/refresh",
}

export const authApi = {
    login: (data: LoginCredentials) => api.post(authPaths.login, data),
    register: (data: RegisterData) => api.post(authPaths.register, data),
    logout: () => api.post(authPaths.logout),
    refresh: () => api.post(authPaths.refresh),
}