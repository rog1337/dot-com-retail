import { z } from "zod"

const emailSchema = z
    .string()
    .min(5, "Must be a valid email address")
    .max(255, "Email is too long")
    .toLowerCase()

const passwordSchema = z
    .string()
    .min(5, "Password must be at least 5 characters")
    .max(128, "Password is too long")

const displayNameSchema = z
    .string()
    .min(2, "Name is required")
    .max(50, "Name must not exceed 50 characters")
    .regex(/^[a-zA-Z\s-']+$/, "Name contains illegal characters")

const twoFactorSchema = z
    .string()
    .min(6, "Code is too short")
    .max(6, "Code is too long")
    .optional()

export const loginSchema = z.object({
    email: emailSchema,
    password: passwordSchema,
    twoFactorCode: twoFactorSchema,
})

export const registerSchema = z.object({
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, "Confirm your password"),
    displayName: displayNameSchema,
})
    .refine((data) => data.password === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"]
    })


export type LoginFormData = z.infer<typeof loginSchema>
export type RegisterFormData = z.infer<typeof registerSchema>