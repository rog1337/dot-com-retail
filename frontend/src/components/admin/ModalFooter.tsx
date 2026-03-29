export default function ModalFooter({
  mode,
  onCancel,
  onSubmit,
}: {
  mode: "create" | "edit"
  onCancel: () => void
  onSubmit: () => void
}) {
  return (
    <div className="flex justify-end gap-2 border-t border-gray-200 pt-2">
      <button
        type="button"
        className="rounded-lg border border-gray-200 px-4 py-2 text-xs hover:bg-gray-50"
        onClick={onCancel}
      >
        Cancel
      </button>
      <button
        type="button"
        className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e]"
        onClick={onSubmit}
      >
        {mode === "edit" ? "Save changes" : "Create"}
      </button>
    </div>
  )
}
