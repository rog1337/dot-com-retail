class TokenManager {
    private accessToken: string | null = null
    private refreshTokenExpiry: string | null = null
    private refreshHandler: (() => Promise<boolean>) | null = null

    getAccessToken() {
        return this.accessToken
    }

    setAccessToken(token: string) {
        this.accessToken = token
    }

    setRefreshHandler(fn: () => Promise<boolean>) {
      this.refreshHandler = fn
    }

    getRefreshHandler() {
      return this.refreshHandler
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