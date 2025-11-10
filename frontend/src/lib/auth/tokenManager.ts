class TokenManager {
    private accessToken: String | null = null;
    private refreshTokenExpiry: String | null = null;

    getAccessToken() {
        return this.accessToken;
    }

    setAccessToken(token: string) {
        this.accessToken = token;
    }

    clearAccessToken(): void {
        this.accessToken = null;
    }

}