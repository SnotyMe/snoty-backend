import type { KeycloakAuthenticationMetadata } from "~/api/backend/generated"

export const getKeycloakRedirectUrl = (backlink: string | undefined) => {
	const url = useRequestURL()
	return `${url.protocol}//${url.host}/auth/callback${backlink ? `?to=${encodeURIComponent(backlink)}` : ""}`
}

export const getKeycloakProviders = (
	metadata: KeycloakAuthenticationMetadata,
	backlink: string | undefined,
) => {
	const authUrl = new URL(metadata.authUrl)
	authUrl.searchParams.append("client_id", metadata.clientId)
	authUrl.searchParams.append("response_type", "code")
	authUrl.searchParams.append("scope", "openid")
	authUrl.searchParams.append("redirect_uri", getKeycloakRedirectUrl(backlink))

	return metadata.providers
		.map(provider => ({
			label: provider.displayName ?? provider.alias,
			icon: `i-simple-icons-${provider.providerId}`,
			to: `${authUrl}&kc_idp_hint=${provider.alias}`,
		}))
}
