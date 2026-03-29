"use client"

import { FormEvent, useState } from "react"
import { adminApi } from "@lib/api/adminApi"
import { useToastStore } from "@store/toastStore"
import Card from "@/src/components/admin/Card"
import { AdminReview } from "@_types/admin"
import { parseIntOrNull } from "@lib/util"

export default function AdminReviewPage() {
  const [reviewIdInput, setReviewIdInput] = useState<string>("")
  const [review, setReview] = useState<AdminReview | null>(null)
  const [loading, setLoading] = useState(false)
  const [reviewDeleteCause, setReviewDeleteCause] = useState<string>("")
  const { show } = useToastStore()

  const loadReview = async () => {
    const id = parseIntOrNull(reviewIdInput)
    if (!id) {
      show("Review id must be a number", "error")
      return
    }
    setLoading(true)
    try {
      const res = await adminApi.getReview(id)
      setReview(res)
      show("Review loaded", "success")
    } catch (e) {
      if (e?.response?.status === 404) {
        show(`Review not found: ${id}`, "error")
        return
      }
      show("Failed loading review", "error")
    } finally {
      setLoading(false)
    }
  }

  const deleteReview = async () => {
    const id = parseIntOrNull(reviewIdInput)
    if (!id) {
      show("Review id must be a number", "error")
      return
    }
    try {
      await adminApi.deleteReview(id, reviewDeleteCause.trim() || undefined)
      show("Review deleted", "success")
      setReview(null)
      setReviewDeleteCause("")
    } catch {
      show("Failed deleting review", "error")
    }
  }

  const handleSearch = async (e: FormEvent) => {
    e.preventDefault()
    await loadReview()
  }

  return (
    <Card title="Review Moderation (delete)">
      <div className="grid grid-cols-2 items-end gap-3">
        <label className="space-y-1">
          <div className="text-[10px] tracking-widest text-gray-400 uppercase">Review ID</div>
          <form onSubmit={handleSearch}>
            <input
              className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
              value={reviewIdInput}
              onChange={(e) => setReviewIdInput(e.target.value)}
              placeholder="e.g. 12"
            />
            <button
              type="submit"
              onSubmit={handleSearch}
              className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
            >
              {loading ? "Loading..." : "Load Review"}
            </button>
          </form>
        </label>
      </div>

      {review && (
        <div className="mt-5 space-y-4">
          <div className="rounded-lg border border-gray-200 p-3 text-xs">
            <div className="tracking-widest text-gray-400 uppercase">Rating</div>
            <div className="mt-1 font-semibold">{review.rating} / 5</div>
            <div className="mt-1 text-gray-500">Votes: {review.votes}</div>
            <div className="mt-1 text-gray-500">
              Author:
              <p className="pl-2">ID: {review.author.id}</p>
              <p className="pl-2">email: {review.author.email}</p>
              <p className="pl-2">name: {review.author.displayName}</p>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 p-3 text-xs">
            <div className="tracking-widest text-gray-400 uppercase">Body</div>
            <div className="mt-2 whitespace-pre-wrap text-gray-800">{review.body ?? "—"}</div>
          </div>

          <label className="space-y-1">
            <div className="text-[10px] tracking-widest text-gray-400 uppercase">
              Delete cause (optional)
            </div>
            <input
              className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
              value={reviewDeleteCause}
              onChange={(e) => setReviewDeleteCause(e.target.value)}
              placeholder="e.g. Inappropriate content"
            />
          </label>

          <div className="mt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={deleteReview}
              className="rounded-lg border border-red-200 px-4 py-2 text-xs text-red-600 hover:bg-red-50"
            >
              Delete review
            </button>
          </div>
        </div>
      )}
    </Card>
  )
}
