"use client"

import Section from "src/app/account/Section"
import React, { useState } from "react";
import {emailSchema, LoginFormData, } from "@lib/validation/authSchemas";
import {logger} from "@lib/logger";
import {authApi} from "@lib/api/authApi";
/*
    This page is meant for unauthenticated password reset requests,
    otherwise a simple reset button is available in the account page
 */
export default function PasswordResetRequestForm() {
    const [email, setEmail] = useState("")
    const [error, setError] = useState("")
    const [submitDisabled, setSubmitDisabled] = useState(false)
    const [loading, setLoading] = useState(false)
    const [submitted, setSubmitted] = useState(false)

    const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(e.target.value)
        if (submitted && validateEmail(email)) {
            setError("")
        }
    }

    const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (submitDisabled) return
        setSubmitted(true)

        if (!validateEmail(email)) return
        setError("")

        try {
            setSubmitDisabled(true)
            setLoading(true)
            const request = {
                email: email,
            }
            await authApi.resetPassword(request)
            setError("Password reset link sent to your email")
        } catch (e: any) {
            const code = e?.response?.data?.code
            if (code === "USER_NOT_FOUND") {
                setError("This email is not registered")
                setSubmitDisabled(false)
                return
            }
            logger.d("Error resetting password", e)
            setError("Error resetting password")
            setSubmitDisabled(false)
        } finally {
            setLoading(false)
        }
    }

    function validateEmail(email: string) {
        const result = emailSchema.safeParse(email)
        if (!result.success) {
            setError(result.error.issues[0].message)
            return false
        }
        return true
    }

    return (
        <div className="max-w-xl mx-auto min-h-screen mt-20">
            <div className="fade-in">
                <Section title="Reset password">
                    <div className="px-5 py-4 border-b border-zinc-50 fade-in">
                        <form onSubmit={onSubmit}>
                            <div className="space-y-2">
                                <input
                                    placeholder={"Email address"}
                                    value={email}
                                    onChange={e => handleEmailChange(e)}
                                    className="w-full border border-zinc-200 rounded-lg px-3 py-3
                                             outline-none focus:border-zinc-400 transition-colors
                                             placeholder-zinc-300"
                                />
                                {error &&
                                    <p className="text-base text-red-500 fade-in">{error}</p>
                                }
                                <button
                                    disabled={loading || submitDisabled}
                                    type={"submit"}
                                    className="w-full mt-1 font-medium text-white bg-zinc-800 py-3 rounded-lg
                                     hover:bg-zinc-700 transition-colors disabled:text-zinc-400 disabled:hover:bg-zinc-800"
                                >Request password reset
                                </button>
                            </div>
                        </form>
                    </div>
                </Section>
            </div>
        </div>
    )
}