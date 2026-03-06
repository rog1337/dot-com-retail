import {productApi} from "@lib/api/productApi"
import type {Product} from "@_types/product"
import Image from "next/image"
import {SquareX} from "lucide-react"
import axios, {AxiosError} from "axios"
import NotFound from "next/dist/client/components/builtin/not-found"

export default async function Product({ params }: any) {
    const { id } = await params

    let product: Product

    try {
        const data = await productApi.getById(1)
        product = data

    } catch (e: unknown) {
        if (axios.isAxiosError(e)) {
            if (e.status === 404) return NotFound()
        }

        throw e
    }

    if (!product) return NotFound()

    return (
        <div>
            {product.images[0]?.url ?
                <Image
                    src={product.images[0].url}
                    alt={"product"}
                    width={100}
                    height={100}
                />
                :
                <SquareX size={75} width={75} />
            }

            <span>{JSON.stringify(product)}</span>
        </div>
    )
}