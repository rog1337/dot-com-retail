import Link from "next/link";

export default function Footer() {
  return (
    <footer className="mt-12 bg-slate-900 py-3 text-white">
      <div className="mx-auto flex max-w-4xl flex-col items-center justify-between space-y-4 px-4 md:flex-row md:space-y-0">
        <div className="text-center md:text-left">
          <h2 className="text-xl font-bold">tyre-dot-com</h2>
          <p className="text-sm text-gray-400">E-commerce project selling tyres</p>
          <div className="mt-3 text-sm text-gray-500">
            &copy; {new Date().getFullYear()} tyre-dot-com
          </div>
        </div>

        <div className="flex space-x-6">
          <Link href="/about" className="text-gray-200 hover:underline">
            About
          </Link>
          <Link href="/contact" className="text-gray-200 hover:underline">
            Contact
          </Link>
        </div>
      </div>
    </footer>
  )
}