import { useAuth } from "~/stores/auth"

export function injectAuth(options: RequestInit = {}) {
	const auth = useAuth()
	return {
		...options,
		headers: {
			Authorization: `Bearer ${auth.accessToken}`,
			...options.headers,
		},
	}
}
