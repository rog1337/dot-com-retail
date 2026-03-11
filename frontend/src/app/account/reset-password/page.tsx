import {redirect} from "next/navigation";
import PasswordResetForm from "@/src/app/account/reset-password/PasswordResetForm";
import PasswordResetRequestForm from "@/src/app/account/reset-password/PasswordResetRequestForm";

export type SearchParams = {
    token: string
}

export default async function ResetPasswordPage({ searchParams }: { searchParams: SearchParams }) {
    const { token } = await searchParams

    if (!token) {
        return <PasswordResetRequestForm/>
    }

    return <PasswordResetForm token={token}/>
}