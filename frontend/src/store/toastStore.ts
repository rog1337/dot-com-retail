import {create} from "zustand"

type ToastType = "success" | "error" | "info"

export const useToastStore = create<ToastState>((set) => ({
    message: "",
    type: "success",
    visible: false,
    time: 3000,
    show: (message, type = "success", time = 3000) => set({
        message, type, visible: true, time
    }),
    hide: () => set({ visible: false }),
}))

type ToastState = {
    message: string
    type: ToastType
    visible: boolean
    time: number
    show: (message: string, type?: ToastType, time?: number) => void
    hide: () => void
}