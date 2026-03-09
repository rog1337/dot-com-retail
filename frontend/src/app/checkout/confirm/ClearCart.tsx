"use client"

import {useEffect} from "react";

export default function ClearCart() {
    useEffect(() => {
        localStorage.removeItem("cart-storage");
    }, [])

    return null
}