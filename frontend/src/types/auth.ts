import {Contact} from "@_types/contact";

export interface User {
    id: string
    displayName: string
}

export interface LoginCredentials {
    email: string
    password: string
}

export interface RegisterData {
    email: string
    password: string
    confirmPassword: string
    displayName: string
    captchaToken: string
}

export interface AuthResponse {
    accessToken: string
    user: User
}

export interface RefreshResponse {
    accessToken: string
}

export interface UserUpdate {
    displayName: string
}

export interface UserDetails {
    displayName: string
    email: string
    contact?: Contact
}

export interface PasswordResetInitiationRequest {
    email: string
}

export interface PasswordResetRequest {
    token: string
    password: string
}

export interface TwoFactorStatus {
    isEnabled: boolean
}

export interface TwoFactorSetupResponse {
    secret: string
    qrCode: string
}

export interface TwoFactorVerification {
    code: string
}