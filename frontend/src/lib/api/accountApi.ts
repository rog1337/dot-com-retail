import {User, UserDetails, UserUpdate} from "@_types/auth";
import api from "@lib/api/api";
import {Order, OrderResponse} from "@_types/order";
import {Contact} from "@_types/contact";

export const accountPaths = {
    base: "/account",
    details: () => accountPaths.base + "/details",
    orders: () => accountPaths.base + "/orders",
    reset_password: () => accountPaths.base + "/reset-password",
}

export const accountApi = {
    getAccount: (): Promise<User> => (api.get(accountPaths.base)),
    getAccountDetails: (): Promise<UserDetails> => (api.get(accountPaths.details())),
    getOrders: (): Promise<OrderResponse> => (api.get(accountPaths.orders())),
    updateAccount: (update: UserUpdate): Promise<User> => (api.patch(accountPaths.base, update)),
    resetPassword: (): Promise<any> => (api.post(accountPaths.reset_password()))
}