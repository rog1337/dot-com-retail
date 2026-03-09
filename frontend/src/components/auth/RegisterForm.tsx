import {useAuth} from "@lib/auth/authContext"
import {useRef, useState} from "react"
import {useForm} from "react-hook-form"
import {RegisterFormData, registerSchema} from "@lib/validation/authSchemas"
import {zodResolver} from "@hookform/resolvers/zod"
import Link from "next/link"
import OAuth from "@components/auth/OAuth"
import { logger as log} from "@lib/logger"
import { Turnstile } from "@marsidev/react-turnstile"
import {useRouter} from "next/navigation";

const TURNSTILE_SITE_KEY = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY || ""

export default function RegisterForm() {
    const { register: registerUser } = useAuth()
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState("")
    const [captchaToken, setCaptchaToken] = useState("")
    const [captchaError, setCaptchaError] = useState("")
    const turnstileRef = useRef<any>(null)
    const router = useRouter()

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch,
    } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
    })

    const password = watch("password")

    const resetCaptcha = () => {
        turnstileRef.current?.reset();
        setCaptchaToken("");
    }

    const onSubmit = async (data: RegisterFormData) => {
        setIsLoading(true)
        setError("")

        if (!captchaToken) {
            log.d("No captcha token")
            setCaptchaError("Please complete the captcha")
            setIsLoading(false)
            return
        }

        try {
            await registerUser({
                ...data,
                captchaToken,
            })
            router.push("/")
        } catch (err: any) {
            console.log("err: ", err)
            const code = err?.response?.data?.code
            if (code === "CAPTCHA_FAILED") {
                setError("Captcha verification failed")
            } else if (code === "USER_ALREADY_EXISTS") {
                setError("Email is already registered")
            } else {
                setError("An error occurred")
                throw Error(err ? err : "An error occurred")
            }
            resetCaptcha()
            setIsLoading(true)
        }
    }

    return (
        <div className="p-4 md:p-0 md:w-full md:max-w-md md:mx-auto mt-10">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <h2 className="text-3xl font-bold text-center mb-8">Create Account</h2>

                {error && (
                    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                        {error}
                    </div>
                )}


                <div>
                    <label htmlFor="email" className="block text-sm font-medium mb-2">
                        Email Address
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
                    <label htmlFor="displayName" className="block text-sm font-medium mb-2">
                        Name
                    </label>
                    <input
                        {...register("displayName")}
                        id="displayName"
                        type="text"
                        autoComplete="given-name"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    {errors.displayName && (
                        <p className="mt-1 text-sm text-red-600">{errors.displayName.message}</p>
                    )}
                </div>

                <div>
                    <label htmlFor="password" className="block text-sm font-medium mb-2">
                        Password
                    </label>
                    <input
                        {...register("password")}
                        id="password"
                        type="password"
                        autoComplete="new-password"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="••••••••"
                    />
                    {errors.password && (
                        <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
                    )}

                </div>

                <div>
                    <label htmlFor="confirmPassword" className="block text-sm font-medium mb-2">
                        Confirm Password
                    </label>
                    <input
                        {...register("confirmPassword")}
                        id="confirmPassword"
                        type="password"
                        autoComplete="new-password"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="••••••••"
                    />
                    {errors.confirmPassword && (
                        <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
                    )}
                </div>

                <Turnstile
                    ref={turnstileRef}
                    siteKey={TURNSTILE_SITE_KEY}
                    onSuccess={(token) => {
                        setCaptchaToken(token)
                        setCaptchaError("")
                        setIsLoading(false)
                    }}
                    onError={(error) => {
                        setCaptchaError(error)
                        setCaptchaToken("")
                    }}
                    className="mb-0"
                />

                {captchaError && (
                    <p className="mb-0 text-sm text-red-600">{captchaError}</p>
                )}

                <button
                    type="submit"
                    disabled={isLoading}
                    className="mt-3 w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 cursor-pointer disabled:bg-blue-300 disabled:cursor-not-allowed transition"
                >
                    {isLoading ? 'Creating Account...' : 'Create Account'}
                </button>

                <p className="text-center text-sm text-gray-600 mb-2">
                    Already have an account?{' '}
                    <Link href="/login" className="text-blue-600 hover:text-blue-800 font-medium">
                        Sign in
                    </Link>
                </p>
            </form>

            <OAuth/>
        </div>
    )
}