import {AuthResponse, LoginCredentials, RegisterCredentials} from "@/src/types/auth";
import {authApi} from "@lib/api/authApi";
import {tokenManager} from "@lib/auth/tokenManager";

class AuthService {
    async login(data: LoginCredentials) {
        const res = await authApi.login(data)
        console.log(res)
    }

    async register(data: RegisterCredentials) {
        const res = await authApi.register(data)
        tokenManager.setAccessToken(res.data.accessToken)
    }

    async logout() {}

    async refresh() {
        const res = await authApi.refresh();
        tokenManager.setAccessToken(res.data.accessToken);
    }


}

export const authService = new AuthService();