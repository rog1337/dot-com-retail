import {AuthResponse, LoginCredentials, RegisterData} from "@/src/types/auth";
import {authApi} from "@lib/api/authApi";
import {tokenManager} from "@lib/auth/tokenManager";
import {logger as log} from "@lib/logger";

class AuthService {
    async login(data: LoginCredentials) {
        try {
            const res = await authApi.login(data);
            return res;
        } catch (e) {
            return Promise.reject(e);
        }
    }

    async register(data: RegisterData) {
        try {
            const res = await authApi.register(data);
            tokenManager.setAccessToken(res.data.accessToken);
            return res;
        } catch (e: any) {
            return Promise.reject(e);
        }
    }

    async logout() {

    }

    async refresh() {
        try {
            const res = await authApi.refresh();
            tokenManager.setAccessToken(res?.data?.accessToken);
            return res;
        } catch (e) {
            tokenManager.clearAccessToken();
            return Promise.reject(e);
        }
    }


}

export const authService = new AuthService();