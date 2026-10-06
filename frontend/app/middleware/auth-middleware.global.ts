import { useAuth } from "~/stores/auth"
import { jwtDecode } from "jwt-decode"
import { validateSession } from "~/utils/auth"

export default defineNuxtRouteMiddleware(async (to, _) => {
	if (to.path.startsWith("/auth")) return

	const redirectToLogin
		= () => navigateTo(`/auth/login?to=${encodeURIComponent(to.fullPath)}`)

	const auth = useAuth()

	const accessToken = auth.accessToken
	if (accessToken == null) {
		return redirectToLogin()
	}
	const refreshToken = auth.refreshToken

	const jwt = jwtDecode(accessToken)
	const refreshJwt = refreshToken ? jwtDecode(refreshToken) : null
	const refreshType = validateSession(jwt, refreshJwt)
	if (refreshType == false) return // token is up to date :D

	switch (refreshType) {
		case "sync":
			await auth.refresh()
			break
		case "async":
			void auth.refresh()
			break
		case "unrecoverable":
			return redirectToLogin()
	}
})
