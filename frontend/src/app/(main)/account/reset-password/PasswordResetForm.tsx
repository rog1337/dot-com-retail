"use client"

import Section from "@/src/app/(main)/account/Section"
import React, {useState} from "react";
import {authApi} from "@lib/api/authApi";
import {logger} from "@lib/logger";

type Passwords = {
    password: string
    confirmPassword: string
}

export default function PasswordResetForm({ token }: { token: string }) {
    const [passwords, setPasswords] = useState<any>({ password: "", confirmPassword: "" })
    const [error, setError] = useState<string | null>(null)
    const [success, setSuccess] = useState<boolean>(false)


    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError("")

        const { password, confirmPassword } = passwords
        password.trim()
        confirmPassword.trim()

        if (password.length < 6) {setError("Password is too short"); return}
        if (password.length > 80) {setError("Password is too long"); return}

        if (passwords.password !== passwords.confirmPassword) {
            setError("Passwords do not match")
            return
        }

        try {
            const request = {
                token: token,
                password: password,
            }
            await authApi.resetPasswordVerify(request)
            setSuccess(true)
        } catch (e: any) {
            const code = e?.response?.data?.code
            if (code === "PASSWORD_RESET_TOKEN_INVALID") {
                setError("Password reset token is invalid. Request a new password reset link.")
                return
            }
            logger.d("Error resetting password", e)
            setError("An error occurred, please try again later")
        }

    }

    if (success) {
        return (
            <div className="max-w-xl mx-auto min-h-screen mt-20 text-center">
                <span>
                    Your password has been changed.
                </span>
            </div>
        )
    }

    return (
        <div className="max-w-xl mx-auto min-h-screen mt-20">
            <div className="fade-in">
                <Section title="Reset password">
                    <div className="px-5 py-4 border-b border-zinc-50 fade-in">
                        <form onSubmit={onSubmit}>
                            <div className="space-y-2">
                                {[
                                    {key: "password", placeholder: "New password"},
                                    {key: "confirmPassword", placeholder: "Confirm new password"}
                                ].map(({key, placeholder}) => (
                                    <input
                                        key={key}
                                        type="password"
                                        autoComplete={key}
                                        placeholder={placeholder}
                                        value={passwords[key]}
                                        onChange={e => setPasswords((p: Passwords) => ({
                                            ...p,
                                            [key]: e.target.value
                                        }))}
                                        className="w-full border border-zinc-200 rounded-lg px-3 py-3
                                         outline-none focus:border-zinc-400 transition-colors
                                         placeholder-zinc-300"
                                    />
                                ))}
                                {error &&
                                    <p className="text-base text-red-500 fade-in">{error}</p>
                                }
                                <button
                                    type={"submit"}
                                    className="w-full mt-1 font-medium text-white bg-zinc-800 py-3 rounded-lg hover:bg-zinc-700 transition-colors"
                                >Update password</button>
                            </div>
                        </form>
                    </div>
                </Section>
            </div>
        </div>
    )
}