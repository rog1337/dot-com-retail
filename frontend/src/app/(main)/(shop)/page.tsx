"use client"
import Link from "next/link"
import Image from "next/image"
import { useEffect, useState } from "react"
import { productApi } from "@lib/api/productApi"
import { Product } from "@_types/product"
import StarRating from "@components/shop/StarRating"
import { ArrowRight, Package, ShieldCheck, Truck } from "lucide-react"

function FeaturedCard({ product }: { product: Product }) {
  const image = product.images?.[0]
  return (
    <Link
      href={`/products/${product.id}`}
      className="group relative flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition-shadow duration-300 hover:shadow-lg dark:border-slate-700 dark:bg-slate-800"
    >
      <div className="relative aspect-square overflow-hidden bg-gray-50 dark:bg-slate-700">
        {image ? (
          <Image
            src={image.urls.lg}
            alt={image.altText || product.name}
            fill
            className="object-cover transition-transform duration-500 group-hover:scale-105"
            sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <Package className="h-12 w-12 text-gray-300 dark:text-slate-500" />
          </div>
        )}
      </div>

      <div className="flex flex-col gap-1.5 p-4">
        <h3 className="line-clamp-2 text-sm leading-snug font-semibold text-gray-900 transition-colors group-hover:text-blue-500 dark:text-white">
          {product.name}
        </h3>
        <StarRating averageRating={product.averageRating} reviewCount={product.reviewCount} />
        <span className="mt-1 text-lg font-bold text-gray-900 dark:text-white">
          €{product.price.toFixed(2)}
        </span>
      </div>
    </Link>
  )
}

function SkeletonCard() {
  return (
    <div className="flex animate-pulse flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-slate-700 dark:bg-slate-800">
      <div className="aspect-square bg-gray-100 dark:bg-slate-700" />
      <div className="flex flex-col gap-2 p-4">
        <div className="h-4 w-3/4 rounded bg-gray-100 dark:bg-slate-700" />
        <div className="h-3 w-1/2 rounded bg-gray-100 dark:bg-slate-700" />
        <div className="h-5 w-1/3 rounded bg-gray-100 dark:bg-slate-700" />
      </div>
    </div>
  )
}

const CATEGORIES = [
  { label: "All-Season", params: "?categoryId=1&attr_type=All-Season" },
  { label: "Tyre Repair Kits", params: "?categoryId=2" },
  { label: "Air Fresheners", params: "?categoryId=3" },
]

export default function Home() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await productApi.getByQuery({ page: 0, size: 8 })
        setProducts(res.content ?? res)
      } catch {
      } finally {
        setLoading(false)
      }
    }
    fetch()
  }, [])

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
          {CATEGORIES.map((cat, key) => (
            <Link
              key={key}
              href={`/browse${cat.params}`}
              className="rounded-full border border-gray-200 px-4 py-1.5 text-sm font-medium text-gray-700 transition-colors hover:border-blue-400 hover:text-blue-500 dark:border-slate-700 dark:text-slate-300 dark:hover:border-blue-400 dark:hover:text-blue-400"
            >
              {cat.label}
            </Link>
          ))}
        </div>
      </section>

      <section className="bg-gray-50 px-6 py-16 dark:bg-slate-950">
        <div className="mx-auto max-w-6xl">
          <div className="mb-8 flex items-end justify-between">
            <div>
              <p className="mb-1 text-xs font-bold tracking-widest text-blue-500 uppercase">
                {/* Update this label once you have real sorting: "Top Rated" / "Best Sellers" / "On Sale" */}
                Featured
              </p>
              <h2 className="text-2xl font-black text-gray-900 md:text-3xl dark:text-white">
                Popular Picks
              </h2>
            </div>
            <Link
              href="/browse"
              className="hidden items-center gap-1.5 text-sm font-semibold text-blue-500 transition-colors hover:text-blue-400 sm:inline-flex"
            >
              View all <ArrowRight className="h-4 w-4" />
            </Link>
          </div>

          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {loading
              ? Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)
              : products.map((p) => <FeaturedCard key={p.id} product={p} />)}
          </div>

          <div className="mt-8 text-center sm:hidden">
            <Link
              href="/browse"
              className="inline-flex items-center gap-1.5 text-sm font-semibold text-blue-500 transition-colors hover:text-blue-400"
            >
              View all tyres <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </section>

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