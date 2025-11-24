export const AUTHORIZATION_HEADER_BEARER = "Bearer";
export const OAUTH_URL_GOOGLE = process.env.NEXT_PUBLIC_API_URL + "/oauth2/authorize/google";
export const TURNSTILE_SITE_KEY = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY || ""