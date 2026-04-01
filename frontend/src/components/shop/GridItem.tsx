import Image from "next/image"
import Link from "next/link"
import {Product} from "@_types/product";
import StarRating from "@components/shop/StarRating"
import {Package} from "lucide-react"

export default function GridItem({product, onAddToCart, isLoading}: {product: Product, onAddToCart: any, isLoading: boolean}) {
  const images = product.images
  const imageUrl = images[0]?.urls.lg || ""
  const imageAltText = images[0]?.altText || product.name

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    onAddToCart(product)
  }

  return (
    <Link
      href={`/products/${product.id}`}
      className="group flex w-full max-w-xs flex-col overflow-hidden rounded-xl border border-gray-200 shadow-sm transition-all hover:shadow-md"
    >
      <div className="relative aspect-square w-full overflow-hidden">
        {imageUrl ? (
          <Image
            src={imageUrl}
            alt={imageAltText}
            loading="eager"
            fill
            className="object-cover transition-transform duration-300 group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <Package className="h-20 w-20" />
          </div>
        )}
      </div>

      <div className="mt-1 flex items-end px-1">
        <span className="bg-foreground text-background font-bold text-xl rounded px-1">${product.price.toFixed(2)}</span>
      </div>

      <StarRating
        averageRating={product.averageRating}
        reviewCount={product.reviewCount}
      />

      <div className="flex flex-1 flex-col p-1">
        <h3 className="line-clamp-2 font-semibold transition-colors group-hover:text-blue-600">
          {product.name}
        </h3>

        <div className="flex-1" />

        <button
          onClick={handleAddToCart}
          className="mt-2 w-full rounded-lg bg-black px-4 py-2.5 text-sm font-semibold text-white hover:bg-gray-800 focus:ring-2 focus:ring-black focus:ring-offset-2 focus:outline-none active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
          disabled={isLoading}
        >
          Add to Cart
        </button>
      </div>
    </Link>
  )
}
