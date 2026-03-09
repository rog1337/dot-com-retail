"use client"

import Section from "src/app/account/Section"
import {useEffect, useState} from "react";
import {logger} from "@lib/logger";
import {useToastStore} from "@store/toastStore";
import {accountApi} from "@lib/api/accountApi";
import { type Order } from "@_types/order"
import OrderRow from "@/src/app/account/OrderRow";
import Loading from "@components/Loading";

export default function Orders() {
    const { show } = useToastStore()
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const orders = await accountApi.getOrders()
                setOrders(orders.content)
            } catch(e: any) {
                show("Error fetching orders", "error")
                logger.d("Error fetching orders", e);
            }
            setLoading(false);
        }

        fetchOrders()
    }, []);

    return (
        <div className="fade-in">
            <Section title="Orders">
                {loading &&
                    <Loading/>
                }
                {orders.length === 0
                    ? <div className="px-5 py-10 text-center text-sm">No orders yet.</div>
                    : orders.map(order => <OrderRow key={order.id} order={order} />)
                }
            </Section>
        </div>
    )
}