export default function BottomBar({
  page,
  setPage,
  totalPages,
}: {
  page: number
  setPage: (page: number) => void
  totalPages: number
}) {
  return (
    <div className="mt-4 flex items-center justify-end gap-3 text-xs">
      <button
        type="button"
        disabled={page <= 0}
        onClick={() => setPage(Math.max(0, page - 1))}
        className="rounded-lg border border-gray-200 px-3 py-2 disabled:opacity-30"
      >
        ← Prev
      </button>
      <div className="text-gray-500">
        Page {page + 1} / {totalPages}
      </div>
      <button
        type="button"
        disabled={page >= totalPages - 1}
        onClick={() => setPage(page + 1)}
        className="rounded-lg border border-gray-200 px-3 py-2 disabled:opacity-30"
      >
        Next →
      </button>
    </div>
  )
}
