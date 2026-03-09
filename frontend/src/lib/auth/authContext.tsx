"use client"

import React, {createContext, useCallback, useContext, useEffect, useState} from "react"
import {AuthResponse, LoginCredentials, RegisterData, User} from "@/src/types/auth"
import { useRouter } from "next/navigation"
import {authApi} from "@lib/api/authApi";
import {tokenManager} from "@lib/auth/tokenManager";
import {accountApi} from "@lib/api/accountApi";
import Loading from "@components/Loading";
import {logger} from "@lib/logger";

const sessId = typeof window !== "undefined"
    ? localStorage.getItem("sessionId") : null

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const router = useRouter()
    const [sessionId, _setSessionId] = useState<string | null>(sessId)

    useEffect(() => {
        _setSessionId(localStorage.getItem("sessionId"))
    }, [sessId])

    useEffect(() => {
        const init = async () => {
            try {
                if (!await refresh()) return
                const user = await accountApi.getAccount()
                setUser(user)
                setSessionId(null)
            } catch(e: any) {
                const code = e?.response?.data?.code
                if (code === "JWT_REFRESH_REVOKED") {
                    // router.push("/")
                    return
                }

                logger.d("Error fetching user", e)
            } finally {
                setIsLoading(false)
            }
        }

        init()
    }, [])

    const refresh = async () => {
        try {
            const response = await authApi.refresh()
            tokenManager.setAccessToken(response.accessToken)
            return true
        } catch (e: any) {
            const code = e?.response?.data?.code
            if (code === "JWT_REFRESH_REVOKED") {
                return Promise.reject(e)
            }
            if (e?.status === 401) {
                return false
            }
            return Promise.reject(e)
        }
    }

    const register = async (data: RegisterData) => {
        try {
            const res = await authApi.register(data)
            tokenManager.setAccessToken(res.accessToken)
            setUser(res.user)
            setSessionId(null)
            setIsLoading(false)
            return res
        } catch (e: any) {
            return Promise.reject(e)
        }
    }

    const logout = async () => {
        try {
            await authApi.logout()
            setUser(null)
        } catch (e: any) {
            logger.d("Error on logout", e)
        }
    }

    const login = async (data: LoginCredentials) => {
        try {
            const res = await authApi.login(data)
            tokenManager.setAccessToken(res.accessToken)
            setUser(res.user)
            setSessionId(null)
            setIsLoading(false)
            return res
        } catch (e) {
            return Promise.reject(e)
        }
    }

    function setSessionId(id: string | null) {
        if (id == null) {
            localStorage.removeItem("sessionId")
        } else {
            localStorage.setItem("sessionId", id)
        }
        _setSessionId(id)
    }

    const value: AuthContextType = {
        user,
        setUser,
        isLoggedIn: !!user,
        isLoading,
        login,
        register,
        logout,
        sessionId,
        setSessionId,
    }

    if (isLoading) return <Loading />

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const context = useContext(AuthContext)
    if (context === undefined) {
        throw new Error("useAuth must be used within the AuthProvider")
    }
    return context
}

export interface AuthContextType {
    user: User | null
    setUser: (user: User) => void
    isLoggedIn: boolean
    isLoading: boolean
    login: (data: LoginCredentials) => Promise<AuthResponse>
    register: (data: RegisterData) => Promise<AuthResponse>
    logout: () => void
    // refresh: () => Promise<void>
    sessionId: string | null
    setSessionId: (sessionId: string | null) => void
}