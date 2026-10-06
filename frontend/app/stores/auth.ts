import { useApi } from "~/api/backend/api-clients"
import { AuthApi, type AuthenticationMetadata, type OAuth2TokenResponse, type Role } from "~/api/backend/generated"
import { jwtDecode } from "jwt-decode"
import type { User } from "#layers/base/app/types/user.ts"
import type { AuthComposable } from "#layers/base/app/types/auth.ts"

export const useAuth = defineStore("auth", () => {
	const authApi = useApi(AuthApi)

	const thirtyDaysInSeconds = 60 * 60 * 24 * 30

	const accessTokenCookie = useCookie("access_token", { maxAge: thirtyDaysInSeconds })
	const refreshTokenCookie = useCookie("refresh_token", { maxAge: thirtyDaysInSeconds })
	const idTokenCookie = useCookie("id_token", { maxAge: thirtyDaysInSeconds })

	const getAuthMetadata = async (): Promise<AuthenticationMetadata> =>
		authApi.authMetadataGet({
			credentials: "omit",
		})

	const { data: authMetadata } = useAsyncData("authMetadata", getAuthMetadata)

	const setCookies = (data: OAuth2TokenResponse) => {
		accessTokenCookie.value = data.access_token
		refreshTokenCookie.value = data.refresh_token
		idTokenCookie.value = data.id_token
	}

	const exchangeCode = async (code: string, redirectUrl: string) =>
		authApi.authTokenPost({ code, redirectUrl })

	const login = async (data: OAuth2TokenResponse, backlink: string | undefined) => {
		setCookies(data)
		try {
			return navigateTo(backlink ?? "/")
		} catch {
			// most likely error: Nuxt's protection against open redirects => default to /
			return navigateTo("/")
		}
	}

	const refresh = async () => {
		const refreshToken = refreshTokenCookie.value
		if (!refreshToken) return

		const response = await authApi.authRefreshPost({ refreshToken })

		setCookies(response)

		return response
	}

	const logout = async () => {
		const idToken = idTokenCookie.value as string
		await authApi.authLogoutPost({ idToken })

		accessTokenCookie.value = undefined
		refreshTokenCookie.value = undefined
		idTokenCookie.value = undefined

		return navigateTo("/auth/login")
	}

	const jwt = computed(() => {
		if (!accessTokenCookie.value) return undefined

		return jwtDecode(accessTokenCookie.value)
	})

	const user = computed(() => {
		// eslint-disable-next-line
		const jwtValue = jwt.value as any | undefined
		if (!jwtValue) return undefined

		return {
			id: jwtValue.sub,
			name: jwtValue.name,
			firstName: jwtValue.given_name,
			lastName: jwtValue.family_name,
		} as User
	})

	const hasRole = async (role: string) => {
		const roles = await authApi.authRolesGet() as never as Role[]

		return roles.some(r => r.name === role)
	}

	return {
		authMetadata,
		exchangeCode,
		login,
		refresh,
		logout,
		user,
		hasRole,
		accessToken: accessTokenCookie,
		refreshToken: refreshTokenCookie,
	}
}) satisfies AuthComposable
