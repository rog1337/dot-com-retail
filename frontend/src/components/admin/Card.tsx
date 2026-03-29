export default function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="border border-gray-200 bg-white dark:border-[#1e1e1e] dark:bg-[#0a0a0a]">
      <div className="border-b border-gray-200 px-5 py-3 dark:border-[#1e1e1e]">
        <div className="text-xs tracking-widest text-gray-500 uppercase dark:text-[#777]">
          {title}
        </div>
      </div>
      <div className="p-5">{children}</div>
    </div>
  )
}
