import {useAuth} from "@lib/auth/authContext";
import {useState} from "react";
import {useForm} from "react-hook-form";
import {RegisterFormData, registerSchema} from "@lib/validation/authSchemas";
import {zodResolver} from "@hookform/resolvers/zod";
import Link from "next/link";
import OAuth from "@components/auth/OAuth";

export default function RegisterForm() {
    const { register: registerUser } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch,
    } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
    })

    const password = watch("password")

    const onSubmit = async (data: RegisterFormData) => {
        setIsLoading(true);
        setError("");

        try {
            await registerUser({
                ...data,
            });
        } catch (err: any) {
            if (err?.response?.data?.message) {
                setError(err?.response?.data?.message);
            } else {
                setError("Registration failed. Please try again.");
            }
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="w-full max-w-md mx-auto">
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
                        {...register('email')}
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
                        {...register('displayName')}
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
                        {...register('password')}
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
                        {...register('confirmPassword')}
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

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full bg-blue-600 text-white py-3 rounded-lg font-medium hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition"
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
    );
};