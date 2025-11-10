"use client"

import React, {createContext, useCallback, useContext, useEffect, useState} from "react";
import { AuthContextType, User } from "@/src/types/auth";
import { useRouter } from "next/router";
import axios from "axios";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    // const router = useRouter();

    useEffect(() => {
        console.log(document.cookie)


        const test = async () => {
            const url = "http://localhost:8080"
            const reg = url + "/api/v1/auth/register"

            const res = await axios.post(reg, {
                    // headers: { "Content-Type": "application/json" },
                        email: "tstemail@email.com",
                        // password: "password",
                        displayName: "test name",
                    })


            console.log(res)
        }

        const login = async () => {
            const url = "http://localhost:8080"
            const log = url + "/api/v1/auth/login"


            try {
                const res = await axios.post(log, {
                    // headers: { "Content-Type": "application/json" },
                    email: "email@email.com",
                    password: "passwor",
                    // displayName: "test name",
                })

                console.log(res)

            } catch(e) {
                // console.error(e?.response?.data)
            }


        }

        // test()
        login()

    }, [])

    const login = async () => {

}

    const register = async () => {

    }

    const logout = async () => {

    }

    const refresh = useCallback(async () => {
        try {

        } catch (e) {

        }
    }, [])

    const value: AuthContextType = {
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        refresh,
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