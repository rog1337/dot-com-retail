export interface User {
    id: string
    email: string
    displayName: string
    role?: string
    twoFactorEnabled?: boolean
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

export interface AuthContextType {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
    login: (data: LoginCredentials) => Promise<void>
    register: (data: RegisterData) => Promise<void>
    logout: () => Promise<void>
    // refresh: () => Promise<void>
    sessionId: string | null
    setSessionId: (sessionId: string | null) => void
}

export interface AuthResponse {
    accessToken: string
    user: User
}