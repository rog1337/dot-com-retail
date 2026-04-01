"use client"
import Link from "next/link"

export default function Home() {
    return (
        <div className="mt-50 flex flex-col items-center justify-center text-xl">
            <h1>Welcome!</h1>
            <Link
                href="/browse?categoryId=1"
                className="mt-10 p-5 border-2 text-xl bg-gray- text-blue-500 hover:bg-gray-200"
            >Browse Tyres</Link>
        </div>
        )
}
