import Image from "next/image"
import Link from "next/link"

export default function ProductItem({product, onAddToCart}: any) {
    const images = product.images
    const imageUrl = images[0]?.url || "404"
    const imageAltText = images[0]?.altText || product.name

    const handleAddToCart = (e: any) => {
        e.preventDefault()
        e.stopPropagation()
        onAddToCart(product)
    }

    return (
        <Link
            href={`/products/${product.slug}`}
            className="group flex w-full max-w-xs flex-col overflow-hidden rounded-xl border border-gray-200 shadow-sm transition-all hover:shadow-md"
        >
            <div className="relative aspect-square w-full overflow-hidden bg-gray-50">
                <Image
                    src={imageUrl}
                    alt={imageAltText}
                    loading="eager"
                    fill
                    className="object-cover transition-transform duration-300 group-hover:scale-105"
                    sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
                />
            </div>

            <div className="flex flex-1 flex-col p-5">

                <h3 className="line-clamp-2 text-lg font-semibold transition-colors group-hover:text-blue-600">
                    {product.name}
                </h3>

                <div className="flex-1" />

                <div className="mt-3 flex items-end">
          <span className="text-xl font-bold">
            ${product.price.toFixed(2)}
          </span>
                </div>

                <button
                    onClick={handleAddToCart}
                    className="mt-5 w-full rounded-lg bg-black px-4 py-2.5 text-sm font-semibold
                     text-white hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-black
                      focus:ring-offset-2 active:scale-[0.98]"
                >
                    Add to Cart
                </button>
            </div>
        </Link>
    )
}
