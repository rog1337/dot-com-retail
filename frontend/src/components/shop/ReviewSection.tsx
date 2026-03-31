"use client"

import { useEffect, useState } from "react"
import { Loader2, PenLine, Star, ThumbsUp } from "lucide-react"
import { useAuth } from "@lib/auth/authContext"
import { reviewApi } from "@lib/api/reviewApi"
import StarRating from "@components/shop/StarRating"
import { Product } from "@_types/product"
import { productApi } from "@lib/api/productApi"
import { logger } from "@lib/logger"
import { useToastStore } from "@store/toastStore"
import { Review } from "@_types/review"

function StarPicker({ value, onChange }: { value: number; onChange: (n: number) => void }) {
  const [hovered, setHovered] = useState(0)
  return (
    <div className="flex gap-1">
      {Array.from({ length: 5 }, (_, i) => {
        const n = i + 1
        const active = n <= (hovered || value)
        return (
          <button
            key={n}
            type="button"
            onClick={() => onChange(n)}
            onMouseEnter={() => setHovered(n)}
            onMouseLeave={() => setHovered(0)}
            aria-label={`Rate ${n} star${n !== 1 ? "s" : ""}`}
            className="transition-transform hover:scale-110"
          >
            <Star
              className={`h-7 w-7 transition-colors ${active ? "fill-amber-400 text-amber-400" : "fill-gray-200 text-gray-200"}`}
            />
          </button>
        )
      })}
    </div>
  )
}

function RatingBar({ star, count, total }: { star: number; count: number; total: number }) {
  const pct = total > 0 ? (count / total) * 100 : 0
  return (
    <div className="flex items-center gap-2 text-sm">
      <span className="w-4 text-right text-gray-500">{star}</span>
      <Star className="h-3 w-3 flex-shrink-0 fill-amber-400 text-amber-400" />
      <div className="h-1.5 flex-1 overflow-hidden rounded-full">
        <div
          className="h-full rounded-full bg-amber-400 transition-all duration-500"
          style={{ width: `${pct}%` }}
        />
      </div>
      <span className="w-5 text-right text-gray-400">{count}</span>
    </div>
  )
}

export default function ReviewSection({ product }: { product: Product }) {
  const { isLoggedIn, user } = useAuth()
  const { show, hide } = useToastStore()

  const [reviews, setReviews] = useState<Review[]>([])
  const [hasPurchased, setHasPurchased] = useState(false)
  const [hasReviewed, setHasReviewed] = useState(false)
  const [canReview, setCanReview] = useState(false)
  const [loading, setLoading] = useState(true)

  const [formOpen, setFormOpen] = useState(false)
  const [formRating, setFormRating] = useState(0)
  const [formText, setFormText] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState("")

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      await getReviews()
      setLoading(false)
    }
    load()
  }, [product.id, isLoggedIn])

  const getReviews = async () => {
    try {
      const { reviews, userReviewStatus } = await productApi.getReviews(product.id)
      setReviews(reviews.content)
      setHasPurchased(userReviewStatus.hasPurchased)
      setHasReviewed(userReviewStatus.hasReviewed)
      setCanReview(userReviewStatus.canReview)
    } catch (e) {
      logger.d("Failed to fetch reviews", e)
    }
  }

  const breakdown = [5, 4, 3, 2, 1].map((star) => ({
    star,
    count: reviews.filter((r) => r.rating === star).length,
  }))

  const toggleVote = async (reviewId: number) => {
    console.log("dick")
    if (!isLoggedIn) {
      show("You must be logged in to vote reviews", "error")
      return
    }
    setReviews((prev) =>
      prev
        .map((r) =>
          r.id === reviewId
            ? { ...r, votes: r.hasVoted ? r.votes - 1 : r.votes + 1, hasVoted: !r.hasVoted }
            : r,
        )
        .sort((a, b) => b.votes - a.votes),
    )
    try {
      await reviewApi.toggleVote(reviewId)
    } catch (e) {
      if (e?.response?.data?.code === "CANNOT_VOTE_ON_OWN_REVIEW") {
        show("Cannot vote on own review", "error")
        return
      }
      show("There was an error voting the review", "error")
      logger.d("Failed to vote on review", e)
      await getReviews()
    }
  }

  const handleSubmit = async () => {
    console.log("handleSubmit")
    if (formRating === 0) {
      setFormError("Please select a star rating.")
      return
    }
    if (formText.trim().length < 5) {
      setFormError("Review must be at least 5 characters.")
      return
    }
    setFormError("")
    setSubmitting(true)
    try {
      await productApi.addReview(product.id, {
        rating: formRating,
        body: formText.trim(),
      })
      setHasReviewed(true)
      setFormOpen(false)
      setFormRating(0)
      setFormText("")
    } catch (e) {
      setFormError(e.response?.data?.message ?? "Failed to submit review.")
    } finally {
      setSubmitting(false)
    }
    await getReviews()
  }

  const deleteReview = async (reviewId: number) => {
    setLoading(true)
    try {
      await reviewApi.deleteReview(reviewId)
    } catch (e) {
      show("There was an error while deleting the review", "error")
      logger.d(`Failed to delete review`, e)
    }
    await getReviews()
    setLoading(false)
  }

  return (
    <section className="mt-10" aria-labelledby="reviews-heading">
      <h3 id="reviews-heading" className="mb-6 text-lg font-semibold">
        Customer Reviews
      </h3>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="h-6 w-6 animate-spin" />
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
          <div className="flex flex-col gap-4 lg:col-span-1">
            <div className="flex flex-col gap-4 rounded-2xl border border-gray-100 p-6 shadow-sm">
              {product.reviewCount === 0 ? (
                <p className="py-2 text-center text-sm">No reviews yet.</p>
              ) : (
                <>
                  <div className="flex flex-col items-center gap-2">
                    <span className="text-5xl font-bold">{product.averageRating.toFixed(1)}</span>
                    <StarRating
                      averageRating={product.averageRating}
                      reviewCount={product.reviewCount}
                    />
                  </div>
                  <div className="flex flex-col gap-1.5">
                    {breakdown.map(({ star, count }) => (
                      <RatingBar key={star} star={star} count={count} total={product.reviewCount} />
                    ))}
                  </div>
                </>
              )}

              {formOpen && hasPurchased && !hasReviewed && (
                <div className="flex flex-col gap-4">
                  <h4 className="text-sm font-semibold">Your review</h4>
                  <StarPicker value={formRating} onChange={setFormRating} />
                  <textarea
                    value={formText}
                    onChange={(e) => setFormText(e.target.value)}
                    placeholder="Share your experience with this product…"
                    rows={4}
                    className="w-full resize-none rounded-xl border border-gray-200 px-3 py-2.5 text-sm placeholder:text-gray-300 focus:ring-2 focus:ring-gray-900/10 focus:outline-none"
                  />
                  {formError && <p className="text-xs text-red-500">{formError}</p>}
                  <button
                    onClick={handleSubmit}
                    disabled={submitting}
                    className="hover:bg-foreground/30 flex items-center justify-center gap-2 rounded-xl border border-gray-200 px-4 py-2.5 text-sm font-semibold transition-colors disabled:opacity-50"
                  >
                    {submitting && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                    {submitting ? "Submitting…" : "Submit review"}
                  </button>
                </div>
              )}

              {hasPurchased && !hasReviewed && (
                <button
                  onClick={() => setFormOpen((o) => !o)}
                  className={`hover:bg-foreground/30 flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-medium transition-colors ${formOpen ? "border-red-300 hover:bg-red-300/50" : "border-gray-200"}`}
                >
                  <PenLine className="h-4 w-4" />

                  {formOpen ? "Cancel" : "Write a review"}
                </button>
              )}
              {hasPurchased && hasReviewed && (
                <p className="text-center text-xs">You&#39;ve already reviewed this product.</p>
              )}
              {!hasPurchased && isLoggedIn && (
                <p className="text-center text-xs">Only verified purchasers can leave a review.</p>
              )}
              {!isLoggedIn && (
                <p className="text-center text-xs">
                  <a href="/login" className="underline hover:text-gray-600">
                    Sign in
                  </a>{" "}
                  to leave a review.
                </p>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-4 lg:col-span-2">
            {reviews.length === 0 ? (
              <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-gray-200 py-16 text-center">
                <Star className="mb-3 h-8 w-8 text-gray-200" />
                <p className="text-sm font-medium">Be the first to review this product</p>
              </div>
            ) : (
              reviews.map((review) => {
                const canVote = isLoggedIn && review.author.id !== user?.id
                return (
                  <article
                    key={review.id}
                    className="flex flex-col gap-3 rounded-2xl border border-gray-100 p-5 shadow-sm"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex flex-col gap-1.5">
                        <div className="flex items-center gap-2">
                          <div className="flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-full bg-gray-900 text-xs font-bold text-white">
                            {review.author.displayName.charAt(0).toUpperCase()}
                          </div>
                          <span className="text-sm font-medium">{review.author.displayName}</span>
                        </div>
                        <StarRating
                          averageRating={review.rating}
                          reviewCount={1}
                          showCount={false}
                        />
                      </div>
                      <time
                        className="flex-shrink-0 text-xs text-gray-400"
                        dateTime={review.createdAt.toString()}
                      >
                        {new Date(review.createdAt).toLocaleDateString("en-GB", {
                          day: "numeric",
                          month: "short",
                          year: "numeric",
                        })}
                      </time>
                    </div>

                    <p className="text-sm leading-relaxed">{review.body}</p>

                    <div className="flex items-center justify-between gap-2 border-t border-gray-50 pt-1">
                      <button
                        disabled={!canVote}
                        onClick={() => toggleVote(review.id)}
                        aria-label={review.hasVoted ? "Remove upvote" : "Upvote this review"}
                        aria-pressed={review.hasVoted}
                        className={`flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium transition-colors`}
                      >
                        <ThumbsUp
                          className={`h-3.5 w-3.5 ${!canVote ? "opacity-50" : ""} ${review.hasVoted ? "fill-blue-500" : ""}`}
                        />
                        <p className={!canVote ? "opacity-50" : ""}>Helpful</p>
                        {review.votes > 0 && (
                          <span
                            className={`text-foreground bg-foreground/30 rounded-full px-1.5 py-0.5 text-xs leading-none`}
                          >
                            {review.votes}
                          </span>
                        )}
                      </button>

                      {!isLoggedIn && (
                        <span className="text-xs text-gray-300">Sign in to vote</span>
                      )}

                      {review.author.id === user?.id && (
                        <button
                          disabled={loading}
                          className="bg-foreground/10 rounded border-2 border-red-500 px-2 hover:bg-red-300/30"
                          onClick={() => deleteReview(review.id)}
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  </article>
                )
              })
            )}
          </div>
        </div>
      )}
    </section>
  )
}