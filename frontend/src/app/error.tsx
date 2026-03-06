"use client"

import Link from "next/link"
import {useEffect, useState} from "react"

export default function Error({ error }: any) {
    const [timer, setTimer] = useState(5)
    const [cancelRedirect, setCancelRedirect] = useState(false)

    useEffect(() => {
        if (cancelRedirect) return
        if (timer === 0) {
            window.location.href = "/"
        } else {
            setTimeout(() => setTimer(timer - 1), 1000)
        }
    }, [timer])

    return (
        <div
            className="flex flex-col items-center justify-center mt-50"
        >
            <Link
                className="border-2 p-4 rounded-md bg-blue-400 mb-10"
                href="/">Go back to home page</Link>
            <span
                onClick={() => setCancelRedirect(true)}
            >Redirecting in {timer}</span>
            <span>Something went wrong</span>
            <span>{error.message}</span>
        </div>

    )
}