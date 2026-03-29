"use client"

import Card from "@/src/components/admin/Card"
import { adminApi } from "@lib/api/adminApi"
import { FormEvent, useState } from "react"
import { useToastStore } from "@store/toastStore"
import { AdminUser, Role } from "@_types/admin"

export default function AdminUsersPage() {
  const [userIdInput, setUserIdInput] = useState<string>("")
  const [user, setUser] = useState<AdminUser | null>(null)
  const [loading, setLoading] = useState(false)
  const [userRole, setUserRole] = useState<"ADMIN" | "USER">("ADMIN")
  const [error, setError] = useState<string | null>(null)
  const { show } = useToastStore()

  const loadUser = async () => {
    const id = userIdInput.trim()
    if (!id) {
      show("User id is required", "error")
      return
    }
    setLoading(true)
    try {
      const res = await adminApi.getUser(id)
      setUser(res)
      setUserRole(res.role)
      show("User loaded", "success")
    } catch (e) {
      if (e?.response?.status === 404) {
        show(`User not found: ${id}`, "error")
        return
      }
      show("Failed loading user", "error")
    } finally {
      setLoading(false)
    }
  }

  const saveUserRole = async () => {
    const id = userIdInput.trim()
    if (!id) {
      show("User id is required", "error")
      return
    }
    setError(null)
    try {
      await adminApi.updateUser(id, userRole)
      show("Role updated", "success")
      await loadUser()
    } catch (e) {
      if (e?.response?.data?.code === "TWO_FACTOR_REQUIRED") {
        setError("User does not have 2FA enabled")
        return
      }
      show("Failed updating role", "error")
    }
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    await loadUser()
  }

  return (
    <Card title="User Management">
      <div>
        <div className="grid grid-cols-2 items-end gap-3">
          <label className="space-y-1">
            <div className="text-[10px] tracking-widest text-gray-400 uppercase">
              User ID (UUID)
            </div>
            <form onSubmit={handleSubmit}>
              <input
                className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                value={userIdInput}
                onChange={(e) => setUserIdInput(e.target.value)}
              />
              <button
                type="submit"
                onSubmit={handleSubmit}
                className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
                disabled={loading}
              >
                {loading ? "Loading..." : "Load User"}
              </button>
            </form>
          </label>
        </div>

        {user && (
          <div className="mt-5 grid grid-cols-2 gap-3">
            <div className="rounded-lg border border-gray-200 p-3 text-xs">
              <div className="tracking-widest text-gray-400 uppercase">Name</div>
              <div className="mt-1 font-semibold">{user.displayName}</div>
              <div className="mt-1 text-gray-500">{user.email}</div>
            </div>
            <div className="rounded-lg border border-gray-200 p-3 text-xs">
              <div className="tracking-widest text-gray-400 uppercase">2FA</div>
              <div className="mt-1 font-semibold">
                {user.twoFactorEnabled ? "Enabled" : "Disabled"}
              </div>
              <div className="mt-1 text-gray-500">Role: {user.role}</div>
            </div>

            <label className="col-span-2 space-y-1">
              <div className="text-[10px] tracking-widest text-gray-400 uppercase">Role</div>
              <select
                className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
                value={userRole}
                onChange={(e) => setUserRole(e.target.value as Role)}
              >
                <option value="ADMIN">ADMIN</option>
                <option value="USER">USER</option>
              </select>
            </label>

            <div className="col-span-2 flex justify-end">
              <button
                type="button"
                onClick={saveUserRole}
                disabled={loading}
                className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
              >
                Save role
              </button>
            </div>
          </div>
        )}
        {error && <p className="mt-2 justify-self-end text-sm text-red-600">{error}</p>}
      </div>
    </Card>
  )
}
