"use client"

import { useToastStore } from "@store/toastStore"
import {useEffect} from "react"

export default function Toast() {
    const { message, type, visible, time, hide } = useToastStore()

    useEffect(() => {
        if (!visible) return
        const timer = setTimeout(hide, time)
        return () => clearTimeout(timer)
    }, [visible, message])

    const styles = {
        success: "bg-green-500 text-white",
        error:   "bg-red-500 text-white",
        info:    "bg-blue-500 text-white",
    }

    return (
        <div className={`
            fixed top-6 left-1/2 -translate-x-1/2 z-50
            px-5 py-3 rounded-xl shadow-lg text-lg font-medium
            transition-all duration-300 ease-in-out
            ${styles[type]}
            ${visible ? "opacity-100 translate-y-0" : "opacity-0 -translate-y-4 pointer-events-none"}
        `}>
            {message}
        </div>
    )
}