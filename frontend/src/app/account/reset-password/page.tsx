import {redirect} from "next/navigation";
import PasswordResetForm from "@/src/app/account/reset-password/PasswordResetForm";

export type SearchParams = {
    token: string
}

export default async function ResetPasswordPage({ searchParams }: { searchParams: SearchParams }) {
    const { token } = await searchParams

    if (!token) {
        redirect("/")
    }

    return <PasswordResetForm token={token}/>
}