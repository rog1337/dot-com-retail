import api from "@lib/api/api";
import {TwoFactorSetupResponse, TwoFactorStatus, TwoFactorVerification} from "@_types/auth";

export const twoFAPaths = {
    base: "/2fa",
    setup: () => twoFAPaths.base + "/setup",
    verify: () => twoFAPaths.base + "/verify",
    disable: () => twoFAPaths.base + "/disable",
}

export const twoFAApi = {
    status: (): Promise<TwoFactorStatus> => api.get(twoFAPaths.base),
    setup: (): Promise<TwoFactorSetupResponse> => api.post(twoFAPaths.setup()),
    verify: (data: TwoFactorVerification): Promise<any> => api.post(twoFAPaths.verify(), data),
    disable: (data: TwoFactorVerification): Promise<any> => {
        const headers = { "X-2FA-Code": data.code }
        return api.post(twoFAPaths.disable(), null, { headers })
    },
}