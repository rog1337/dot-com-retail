import Image from "next/image"
import { type OrderItem } from "@_types/order"

export default function OrderItem({ item }: { item: OrderItem }) {
    return (
        <div className="flex items-center gap-3 py-3 border-b-2 border-gray-200 last:border-0">
            <div className="relative w-12 h-12 rounded-lg overflow-hidden bg-gray-100 shrink-0">
                <Image
                    src={item.imageUrl}
                    alt={item.productName}
                    fill
                    className="object-cover"
                />
            </div>

            <div className="flex-1 min-w-0">
                <p className="font-medium truncate">{item.productName}</p>
                <p className="mt-0.5">Qty: {item.quantity}</p>
            </div>

            <p className="font-medium shrink-0">
                €{(item.totalAmount).toFixed(2)}
            </p>
        </div>
    )
}