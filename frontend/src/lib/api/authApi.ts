import api from "./api"
import {
    AuthResponse, LoginCredentials, PasswordResetInitiationRequest,
    PasswordResetRequest, RefreshResponse, RegisterData
} from "@/src/types/auth"

export const authPaths = {
    base: "/auth",
    login: () => authPaths.base + "/login",
    register: () => authPaths.base + "/register",
    logout: () => authPaths.base + "/logout",
    refresh: () => authPaths.base + "/refresh",
    resetPassword: () => authPaths.base + "/reset-password",
    resetPasswordVerify: () => authPaths.base + "/reset-password-verify",
}

export const authApi = {
    login: (data: LoginCredentials): Promise<AuthResponse> => api.post(authPaths.login(), data),
    register: (data: RegisterData): Promise<AuthResponse> => api.post(authPaths.register(), data),
    logout: () => api.post(authPaths.logout()),
    refresh: (): Promise<RefreshResponse> => api.get(authPaths.refresh()),
    resetPassword: (data: PasswordResetInitiationRequest): Promise<any> => api.post(authPaths.resetPassword(), data),
    resetPasswordVerify: (data: PasswordResetRequest): Promise<any> => api.post(authPaths.resetPasswordVerify(), data),
}