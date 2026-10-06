<i18n lang="yaml">
en:
  description: Enter your details to log in to Snoty.
  login: Login
  separator: or
  username:
    label: Username
    placeholder: Enter your username
  password:
    label: Password
    placeholder: Enter your password
de:
  description: Gib deine Daten ein, um dich in Snoty einzuloggen.
  login: Anmelden
  separator: oder
  username:
    label: Nutzername
    placeholder: Geben Sie Ihren Nutzernamen ein
  password:
    label: Passwort
    placeholder: Geben Sie Ihr Passwort ein
</i18n>

<script setup lang="ts">
import * as z from "zod"
import type { AuthFormField, FormSubmitEvent } from "@nuxt/ui"
import { definePageMeta } from "#imports"
import { useAuth } from "~/stores/auth"
import { getKeycloakProviders } from "~/utils/keycloak"
import type { KeycloakAuthenticationMetadata } from "~/api/backend/generated"

definePageMeta({
	layout: "center",
})

const { t } = useI18n({ useScope: "local" })

const fields: AuthFormField[] = [
	{
		name: "username",
		type: "username",
		label: t("username.label"),
		placeholder: t("username.placeholder"),
		required: true,
	},
	{
		name: "password",
		type: "password",
		label: t("password.label"),
		placeholder: t("password.placeholder"),
		required: true,
	},
]

const authStore = useAuth()
const { authMetadata } = storeToRefs(authStore)

const route = useRoute()
const backlink = route.query.to as string | undefined

const providers = computed(() => {
	const metadata = authMetadata.value
	if (!metadata) return

	switch (metadata?.adapter) {
		case "keycloak":
			return getKeycloakProviders(metadata as KeycloakAuthenticationMetadata, backlink)
		default:
			return []
	}
})

const schema = z.object({
	username: z.string("Name is required"),
	password: z.string("Password is required").min(8, "Must be at least 8 characters"),
})

type Schema = z.output<typeof schema>

async function onSubmit(payload: FormSubmitEvent<Schema>) {
	const authMetadataValue = authMetadata.value
	if (!authMetadataValue?.publicClientId) return

	const params = new URLSearchParams({
		client_id: authMetadataValue.publicClientId,
		username: payload.data.username,
		password: payload.data.password,
		scope: "openid",
		grant_type: "password",
	})
	const response = await fetch(
		authMetadataValue.tokenUrl,
		{
			method: "POST",
			body: params,
		},
	).then(res => res.json())
	await authStore.login(response, backlink)
}
</script>

<template>
	<UPageCard class="w-md max-w-9/10">
		<UAuthForm
			:schema="schema"
			title="Login"
			:description="t('description')"
			:separator="t('separator')"
			:fields="authMetadata?.publicClientId ? fields : undefined"
			:providers="providers"
			@submit="onSubmit"
		/>
	</UPageCard>
</template>
