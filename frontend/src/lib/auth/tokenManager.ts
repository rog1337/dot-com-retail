class TokenManager {
    private accessToken: String | null = null
    private refreshTokenExpiry: String | null = null

    getAccessToken() {
        return this.accessToken
    }

    setAccessToken(token: string) {
        this.accessToken = token
    }

    clearAccessToken(): void {
        this.accessToken = null
    }

    hasValidAccessToken(): boolean {
        return this.accessToken !== null
    }

    clearAll() {
        this.accessToken = null
    }
}

export const tokenManager = new TokenManager()