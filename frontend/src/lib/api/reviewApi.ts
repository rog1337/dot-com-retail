import api from "@lib/api/api"
import { ReviewVoteResponse } from "@_types/review"

export const paths = {
  base: "/review",
}

export const reviewApi = {
  deleteReview: (reviewId: number): Promise<ReviewVoteResponse> =>
    api.delete(paths.base + `/${reviewId}`),
  toggleVote: (reviewId: number): Promise<ReviewVoteResponse> =>
    api.post(paths.base + `/${reviewId}/vote`),
}