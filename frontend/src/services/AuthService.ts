import {AuthResponse, LoginCredentials, RegisterData} from "@/src/types/auth";
import {authApi} from "@lib/api/authApi";
import {tokenManager} from "@lib/auth/tokenManager";
import {logger as log} from "@lib/logger";

class AuthService {
    async login(data: LoginCredentials) {
        const res = await authApi.login(data)
        console.log(res)
    }

    async register(data: RegisterData) {
        try {
            const res = await authApi.register(data)
            tokenManager.setAccessToken(res.data.accessToken)
        } catch (e: any) {
            log.d("Failed to register", e?.response?.data);
            return Promise.reject(e);
        }

    }

    async logout() {

    }

    async refresh() {
        try {
            const res = await authApi.refresh();
            tokenManager.setAccessToken(res.data.accessToken);
            return res;
        } catch (e) {
            tokenManager.clearAccessToken();
            return Promise.reject(e);
        }
    }


}

export const authService = new AuthService();