import { Page } from "@_types/page"

export interface ProductReviewResponse {
  reviews: Page<Review>
  userReviewStatus: UserReviewStatus
}

export interface UserReviewStatus {
  hasPurchased: boolean
  hasReviewed: boolean
  canReview: boolean
}

export interface Review {
  id: number
  rating: number
  body: string
  votes: number
  author: ReviewAuthor
  hasVoted: boolean
  createdAt: Date
}

export interface ReviewAuthor {
  id: string
  displayName: string
}

export interface AddReviewRequest {
  rating: number
  body?: string
}

export interface ReviewVoteResponse {
  voted: boolean
}