export default function ModalHeader({ title, onClose }: { title: string; onClose: () => void }) {
  return (
    <div className="flex items-center justify-between border-b border-gray-200 pb-3">
      <div className="text-xs tracking-widest text-[#c8a96e] uppercase">{title}</div>
      <button onClick={onClose} className="text-xs text-gray-400 hover:text-gray-700" type="button">
        ✕
      </button>
    </div>
  )
}
