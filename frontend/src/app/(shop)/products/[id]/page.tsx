import {productApi} from "@lib/api/productApi"
import Image from "next/image"
import {SquareX} from "lucide-react"
import axios, {AxiosError} from "axios"
import NotFound from "next/dist/client/components/builtin/not-found"
import Product from "src/app/(shop)/products/[id]/Product"


export default async function ProductPage({ params }: any) {
    const { id } = await params

    if (!id) return NotFound()

    let product
    try {
        product = await productApi.getById(id)

    } catch (e: unknown) {
        if (axios.isAxiosError(e)) {
            if (e.status === 404) return NotFound()
        }

        throw e
    }

    if (!product) return NotFound()

    return (
        <div>
            <Product product={product}/>
        </div>
    )
}