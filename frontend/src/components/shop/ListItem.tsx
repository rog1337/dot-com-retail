import Image from "next/image"
import Link from "next/link"
import { Product } from "@_types/product"
import StarRating from "@components/shop/StarRating"

export default function ListItem({
  product,
  onAddToCart,
  isLoading,
}: {
  product: Product
  onAddToCart: (product: Product) => void
  isLoading: boolean
}) {
  const images = product.images
  const imageUrl = images[0]?.url || "404"
  const imageAltText = images[0]?.altText || product.name

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    onAddToCart(product)
  }

  return (
    <Link
      href={`/products/${product.id}`}
      className="group flex flex-row items-center justify-between overflow-hidden rounded-xl border border-gray-200 shadow-sm transition-all hover:shadow-md"
    >
      <div className="flex items-center gap-2">
        <div className="relative aspect-square w-24 overflow-hidden ">
          <Image
            src={imageUrl}
            alt={imageAltText}
            loading="eager"
            fill
            className="object-cover transition-transform duration-300 group-hover:scale-105"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        </div>
        <h3 className="font-semibold transition-colors group-hover:text-blue-600">
          {product.name}
        </h3>
      </div>
      <div className="flex flex-row gap-5 items-center">
        <div className="">
          <StarRating
            averageRating={product.averageRating}
            reviewCount={product.reviewCount}
          ></StarRating>
        </div>
        <div className="flex flex-col items-center">
          <div className="flex items-end px-1">
          <span className="bg-foreground text-background rounded px-1 text-xl font-bold">
            ${product.price.toFixed(2)}
          </span>
          </div>
          <div className="flex flex-1 flex-col p-1">
            <button
              onClick={handleAddToCart}
              className="mt-2 w-full rounded-lg bg-black px-4 py-2.5 text-sm font-semibold text-white hover:bg-gray-800 focus:ring-2 focus:ring-black focus:ring-offset-2 focus:outline-none active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
              disabled={isLoading}
            >
              Add to Cart
            </button>
          </div>
        </div>

      </div>
    </Link>
  )
}
