<i18n lang="yaml">
en:
  description: The login is in progress...
de:
  description: Der Login läuft...
</i18n>

<script lang="ts" setup>
import { definePageMeta } from "#imports"
import ShinyText from "~/components/vue-bits/ShinyText.vue"
import { useAuth } from "~/stores/auth"
import { getKeycloakRedirectUrl } from "~/utils/keycloak"
import { useAsyncData } from "#app"
import LoadingTexts from "~/components/ui/LoadingTexts.vue"

const { t } = useI18n({ useScope: "local" })

definePageMeta({
	layout: "center",
})

const route = useRoute()
const code = route.query.code as string
const backlink = route.query.to as string | undefined

const { exchangeCode, login } = useAuth()
const { error } = useAsyncData("login", async () => {
	const oauthTokenResponse = await exchangeCode(code, getKeycloakRedirectUrl(backlink))
	await login(oauthTokenResponse, backlink)
}, { server: false })
</script>

<template>
	<div class="flex flex-col items-center text-center space-y-2">
		<ShinyText
			:text="t('description')"
			:disabled="false"
			:speed="3"
			class-name="text-xl"
		/>
		<p class="absolute mt-8">
			<span v-if="error" class="text-error">{{ error }}</span>
			<LoadingTexts v-show="!error"/>
		</p>
	</div>
</template>
