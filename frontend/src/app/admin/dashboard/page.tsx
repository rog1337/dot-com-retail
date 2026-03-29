"use client"

import { useAuth } from "@lib/auth/authContext"

export default function DashboardPage() {
  const { user } = useAuth()

  return (
    <div className="max-w-6xl space-y-8">
      <div>
        <h1 className="text-xl font-bold text-gray-900 dark:text-[#e8e0d5]">
          Admin app management panel
        </h1>
      </div>
      <h1>Hello {user?.displayName}</h1>
    </div>
  )
}
