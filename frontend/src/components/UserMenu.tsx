import { useState, useRef, useEffect } from "react"
import { useRouter } from "next/navigation"
import {useAuth} from "@lib/auth/authContext";
import {LogOut, User} from "lucide-react";
import {useTheme} from "next-themes";

export default function UserMenu() {
    const [open, setOpen] = useState(false)
    const ref = useRef<HTMLDivElement>(null)
    const router = useRouter()
    const { logout } = useAuth()
    const { theme } = useTheme()

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setOpen(false)
            }
        }
        document.addEventListener("mousedown", handler)
        return () => document.removeEventListener("mousedown", handler)
    }, [])

    return (
        <div ref={ref} className="relative">
            <button onClick={() => setOpen(v => !v)}>
                <User/>
            </button>

            {open && (
                <div className={`absolute right-0 mt-2 w-44 border border-zinc-100 rounded-xl shadow-lg " +
                    "overflow-hidden z-50 bg-background`}
                >
                    <button
                        onClick={() => { router.push("/account"); setOpen(false) }}
                        className="w-full flex items-center gap-2 px-4 py-3 text-base text-foreground hover:bg-zinc-400 hover:rounded-t-lg">
                        <User/>
                        My account
                    </button>
                    <div className="border-t border-zinc-50" />
                    <button
                        onClick={() => { logout(); setOpen(false) }}
                        className="w-full flex items-center gap-2 px-4 py-3 text-base text-red-500 hover:bg-red-50 hover:rounded-b-lg">
                        <LogOut/>Log out
                    </button>
                </div>
            )}
        </div>
    )
}