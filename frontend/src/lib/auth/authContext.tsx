"use client"

import React, {createContext, useCallback, useContext, useEffect, useState} from "react";
import {AuthContextType, LoginCredentials, RegisterData, User} from "@/src/types/auth";
import { useRouter } from "next/navigation";
import {authService} from "@/src/services/AuthService";
import {logger as log} from "@/src/lib/logger"

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();

    useEffect(() => {
        // try {
        //     authService.refresh()
        // } catch (e) {
        //     console.log("eee", e)
        // }
        refresh()
    }, [])

    const refresh = async () => {
        try {
            const a = await authService.refresh();
            console.log(a)
        } catch (e: any) {
            console.log("ss")
            if (e?.status === 401) {

            }
        }
    }

    const register = async (data: RegisterData) => {
        await authService.register(data)
    }

    const logout = async () => {

    }

    const login = async (data: LoginCredentials) => {
        await authService.login(data)
        router.push("/")
    }

    const dummyLogin = (): LoginCredentials => {
        const creds: LoginCredentials = {
            email: "email@email.com",
            password: "password",
        }
        return creds;
    }

    // const login = async () => {
    //     const url = "http://localhost:8080"
    //     const log = url + "/api/v1/auth/login"
    //     try {
    //         const res = await axios.post(log, {
    //             // headers: { "Content-Type": "application/json" },
    //             email: "email@email.com",
    //             password: "",
    //             // displayName: "test name",
    //         })
    //
    //         console.log(res)
    //
    //     } catch (e: any) {
    //         console.error(e.response.data)
    //     }
    // }



    // const test = async () => {
    //     const url = "http://localhost:8080"
    //     const reg = url + "/api/v1/auth/register"
    //
    //     const res = await axios.post(reg, {
    //         // headers: { "Content-Type": "application/json" },
    //         email: "tstemail@email.com",
    //         // password: "password",
    //         displayName: "test name",
    //     })
    //
    //
    //     console.log(res)
    // }

    const value: AuthContextType = {
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        // refresh,
    }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within the AuthProvider");
    }
    return context;
}