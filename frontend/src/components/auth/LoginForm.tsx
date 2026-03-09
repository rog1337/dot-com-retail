"use client"

import {LoginFormData, loginSchema} from "@lib/validation/authSchemas"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import {useState} from "react"
import { useAuth } from "@/src/lib/auth/authContext"
import OAuth from "@components/auth/OAuth"
import Link from "next/link"
import {useRouter} from "next/navigation";
import {logger} from "@lib/logger";

export default function LoginForm() {
    const { login } = useAuth()
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState("")
    const router = useRouter()
    const [twoFARequired, setTwoFARequired] = useState<boolean>(false)

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormData> ({
        resolver: zodResolver(loginSchema)
    })

    const onSubmit = async (data: LoginFormData) => {
        setIsLoading(true)
        setError("")

        console.log("errors", errors)
        try {
            const { user } = await login({...data})

            if (!user) {
                logger.d("2FA required")
                setTwoFARequired(true)
                return
            }

            router.push("/")
        } catch (err: any) {
            const data = err?.response?.data
            const code = data?.code

            if (code === "USER_NOT_FOUND") {
                setError("Email is not registered")
            } else if (code === "INVALID_CREDENTIALS") {
                setError("Invalid credentials")
            } else {
                setError("An error occurred")
                throw Error(err)
            }
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className="px-4 pb-4 md:px-0 md:w-full md:max-w-md md:mx-auto mt-10">
            <form onSubmit={handleSubmit(onSubmit)} className="mb-2 space-y-6">
                <h2 className="text-3xl font-bold text-center mb-8">Sign in</h2>

                { error && (
                    <div className="bg-reg-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}

                <div>
                    <label htmlFor="email" className="block text-sm font-memdium mb-2">
                        Email
                    </label>
                    <input
                        {...register("email")}
                        id="email"
                        type="email"
                        autoComplete="email"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="you@example.com"
                    />
                    {errors.email && (
                        <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="password" className="block text-sm font-medium mb-2">
                        Password
                    </label>
                    <input
                        {...register('password')}
                        id="password"
                        type="password"
                        autoComplete="current-password"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    {errors.password && (
                        <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
                    )}
                </div>

                {twoFARequired &&
                    <div>
                        <label htmlFor="twoFactorCode" className="block text-sm font-medium mb-2">
                            Two-factor authentication
                        </label>
                        <input
                            {...register("twoFactorCode")}
                            id="twoFactorCode"
                            autoComplete="twoFactorCode"
                            className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                        {errors.twoFactorCode && (
                            <p className="mt-1 text-sm text-red-600">{errors.twoFactorCode.message}</p>
                        )}
                    </div>
                }

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition"
                >
                    {isLoading ? 'Signing in...' : 'Sign In'}
                </button>
            </form>
            <OAuth/>
            <p className="mt-2 text-center text-sm text-gray-600">
                Don't have an account?{' '}
                <Link href="/register" className="text-blue-600 hover:text-blue-800 font-medium">
                    Sign up
                </Link>
            </p>
        </div>
    )
}