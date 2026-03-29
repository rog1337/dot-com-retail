import { PageMetadata } from "@_types/page"
import { useRef } from "react"

export type Button = {
  name: string
  onClick: () => void
}

export default function TopBar({
  page,
  buttons,
  onSetPageSize,
  onSearch,
}: {
  page: PageMetadata
  buttons: Button[]
  onSetPageSize: (value: number) => void
  onSearch: (value: string) => void
}) {
  const inputRef = useRef<HTMLInputElement>(null)

  const clearInput = () => {
    if (inputRef.current) {
      inputRef.current.value = ""
    }
    onSearch("")
  }

  return (
    <div className="mb-4 flex items-center justify-between gap-3">
      <div className="text-xs text-gray-500">{page ? `${page.totalElements} total` : "—"} </div>
      <div className="flex items-center gap-2">
        <p className="text-sm">Search</p>
        <input
          ref={inputRef}
          className="rounded-md border border-gray-200 p-1 text-sm"
          onChange={(e) => onSearch(e.target.value)}
        />
        <button
          onClick={clearInput}
          className="text-xs text-gray-400 hover:text-gray-700"
          type="button"
        >
          ✕
        </button>
      </div>
      <div className="flex items-center gap-2">
        <select
          className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
          value={String(page.size)}
          onChange={(e) => onSetPageSize(Number(e.target.value))}
        >
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>

        {buttons.map((button) => (
          <button
            key={button.name}
            type="button"
            onClick={button.onClick}
            className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
          >
            {button.name}
          </button>
        ))}
      </div>
    </div>
  )
}
