export default function TabButton({
  active,
  onClick,
  children,
}: {
  active: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-lg border px-3 py-2 text-xs transition-colors ${
        active
          ? "border-[#c8a96e] bg-amber-50 text-[#7a5a1f]"
          : "border-gray-200 bg-white text-gray-500 hover:bg-gray-50 hover:text-gray-700"
      }`}
    >
      {children}
    </button>
  )
}
