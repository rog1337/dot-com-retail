"use client"

import ProtectedRoute from "@components/ProtectedRoute";
import Account from "@/src/app/(main)/account/Account";

export default function AccountPage() {
    return (
        <ProtectedRoute>
            <Account/>
        </ProtectedRoute>
    )
}