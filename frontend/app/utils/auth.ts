export function validateSession(
	token: { exp?: number | undefined, iat?: number | undefined },
	refreshToken: { exp?: number | undefined, iat?: number | undefined } | null,
) {
	// if there is no expiration, assume it is broken and don't refresh anything
	if (token.exp === undefined || token.iat === undefined) {
		return false
	}

	// if the expiration is in the past, the token is invalid and should be refreshed synchronously
	if (Date.now() > token.exp * 1000) {
		// refresh token is expired too, tough luck
		if (refreshToken?.exp == null || Date.now() > refreshToken.exp * 1000) return "unrecoverable"

		return "sync"
	}

	// if the token lifetime is 66% over, refresh it
	const tokenLifetime = token.exp - token.iat
	const tokenElapsed = Date.now() / 1000 - token.iat
	return tokenElapsed > 0.66 * tokenLifetime ? "async" : false
}

export const ROLE_ADMIN = "admin"
