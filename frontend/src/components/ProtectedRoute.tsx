"use client"

import {useAuth} from "@lib/auth/authContext";
import {useRouter} from "next/navigation";
import React, {useEffect} from "react";
import Loading from "@components/Loading";

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const { user, isLoading } = useAuth()
    const router = useRouter()

    useEffect(() => {
        if (!isLoading && !user) {
            router.replace("/login")
        }
    }, [user, isLoading])

    if (isLoading) return <Loading />
    if (!user) return null

    return children
}