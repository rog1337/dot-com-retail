"use client"
import Link from "next/link"
import Image from "next/image"
import { ArrowRight, Package } from "lucide-react"
import { Product } from "@_types/product"
import StarRating from "@components/shop/StarRating"

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

export function FeaturedGrid({ products }: { products: Product[] }) {
  return (
    <section className="bg-gray-50 px-6 py-16 dark:bg-slate-950">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8 flex items-end justify-between">
          <div>
            <p className="mb-1 text-xs font-bold tracking-widest text-blue-500 uppercase">
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
          {products.map((p) => (
            <FeaturedCard key={p.id} product={p} />
          ))}
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
  )
}