import Link from "next/link"
import { ArrowRight, ShieldCheck, Truck } from "lucide-react"
import { FeaturedGrid } from "src/app/(main)/(shop)/FeatureGrid"
import { productApi } from "@lib/api/productApi"

export const revalidate = 60

const CATEGORIES = [
  { label: "All-Season", params: "?categoryId=1&attr_type=All-Season" },
  { label: "Tyre Repair Kits", params: "?categoryId=3" },
  { label: "Air Fresheners", params: "?categoryId=2" },
]

async function getFeaturedProducts() {
  try {
    const res = await productApi.getByQuery({ page: 0, size: 8 })
    return res.content ?? res
  } catch {
    return []
  }
}

export default async function Home() {
  const products = await getFeaturedProducts()

  return (
    <div className="flex flex-col">
      <section className="relative overflow-hidden bg-slate-900 px-6 py-24 text-white md:py-36">
        <div
          className="pointer-events-none absolute inset-0 opacity-5"
          style={{
            backgroundImage:
              "repeating-linear-gradient(45deg, #fff 0, #fff 1px, transparent 0, transparent 50%)",
            backgroundSize: "12px 12px",
          }}
        />
        <div className="pointer-events-none absolute -top-32 -right-32 h-96 w-96 rounded-full bg-blue-500/20 blur-3xl" />
        <div className="relative mx-auto max-w-4xl text-center">
          <p className="mb-4 text-xs font-bold tracking-[0.3em] text-blue-400 uppercase">
            Estonia's Tyre Specialists
          </p>
          <h1 className="text-4xl leading-tight font-black tracking-tight md:text-6xl lg:text-7xl">
            The Right Tyre.
            <br />
            <span className="text-blue-400">Every Road.</span>
          </h1>
          <p className="mx-auto mt-6 max-w-xl text-lg text-slate-300">
            Summer, winter, all-season — find the perfect set for your vehicle with fast delivery
            straight to your door.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            <Link
              href="/browse?categoryId=1&sort=TOP&page=0&size=20&attr_type=Summer"
              className="inline-flex items-center gap-2 rounded-xl bg-blue-500 px-7 py-3.5 text-base font-bold text-white transition-colors hover:bg-blue-400"
            >
              Browse Summer Tyres <ArrowRight className="h-4 w-4" />
            </Link>
            <Link
              href="/browse?categoryId=1"
              className="inline-flex items-center gap-2 rounded-xl border border-slate-600 px-7 py-3.5 text-base font-semibold text-slate-200 transition-colors hover:bg-slate-800"
            >
              All Tyres
            </Link>
          </div>
        </div>
      </section>

      <section className="border-b border-gray-100 bg-white px-6 py-5 dark:border-slate-800 dark:bg-slate-900">
        <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-center gap-2">
          {CATEGORIES.map((cat) => (
            <Link
              key={cat.label}
              href={`/browse${cat.params}`}
              className="rounded-full border border-gray-200 px-4 py-1.5 text-sm font-medium text-gray-700 transition-colors hover:border-blue-400 hover:text-blue-500 dark:border-slate-700 dark:text-slate-300 dark:hover:border-blue-400 dark:hover:text-blue-400"
            >
              {cat.label}
            </Link>
          ))}
        </div>
      </section>

      {/* FeaturedGrid receives already-fetched products — no client-side fetch needed */}
      <FeaturedGrid products={products} />

      <section className="border-t border-gray-100 bg-white px-6 py-12 dark:border-slate-800 dark:bg-slate-900">
        <div className="mx-auto grid max-w-4xl grid-cols-2 gap-8 md:grid-cols-2">
          {[
            { icon: Truck, title: "Fast Delivery", body: "To your door or fitting centre" },
            { icon: ShieldCheck, title: "Quality Assured", body: "Top brands, genuine products" },
          ].map(({ icon: Icon, title, body }) => (
            <div key={title} className="flex flex-col items-center gap-2 text-center">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-950">
                <Icon className="h-5 w-5 text-blue-500" />
              </div>
              <p className="text-sm font-bold text-gray-900 dark:text-white">{title}</p>
              <p className="text-xs text-gray-500 dark:text-slate-400">{body}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}