import { productApi } from "@lib/api/productApi"
import ProductClient from "@/src/app/(main)/(shop)/products/[id]/ProductClient"
import { notFound } from "next/navigation"
import {Metadata} from "next"

type Props = { params: Promise<{ id: string }> }

export async function generateMetadata({ params }: Props): Promise<Metadata> {
    const { id } = await params
    const product = await productApi.getById(id)
    return {
        title: product.name.substring(0, 60),
        description: product.description?.substring(0, 160) ?? `Buy ${product.name} at our store.`,
    }
}

export default async function ProductPage({ params }: Props) {
  const { id } = await params

  if (!id) return notFound()

  let product
  try {
    product = await productApi.getById(id)
  } catch (e) {
    if (e.status === 404) return notFound()
    throw e
  }

  if (!product) return notFound()

  return (
    <div>
      <ProductClient product={product} />
    </div>
  )
}