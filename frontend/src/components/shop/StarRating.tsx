interface StarRatingProps {
  averageRating: number;
  reviewCount: number;
  maxStars?: number;
  showCount?: boolean;
}

function Star({ className }: { className?: string }) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      className={className}
      aria-hidden="true"
    >
      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
    </svg>
  );
}

export default function StarRating({averageRating, reviewCount, maxStars = 5, showCount = true}: StarRatingProps) {
  const rating = reviewCount === 0 ? 0 : Math.min(averageRating, maxStars)

  return (
    <div className="flex items-center gap-2">
      <div className="flex items-center">
        {Array.from({ length: maxStars }, (_, i) => {
          const fill = Math.min(Math.max(rating - i, 0), 1) // 0–1
          const fillPercent = `${fill * 100}%`

          return (
            <div key={i} className="relative h-4 w-4">
              <Star className="absolute inset-0 h-4 w-4 fill-gray-300 text-gray-300" />
              <div className="absolute inset-0 overflow-hidden" style={{ width: fillPercent }}>
                <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
              </div>
            </div>
          )
        })}
      </div>

      {showCount && reviewCount > 0 && (
        <span className="text-sm leading-none font-semibold">
          {rating.toFixed(1)}
        </span>
      )}

      {showCount && (
        <span className="text-sm leading-none text-foreground/60">
        {reviewCount === 0
          ? "No reviews"
          : `(${reviewCount.toLocaleString()} review${reviewCount !== 1 ? "s" : ""})`}
        </span>
      )}
    </div>
  )
}