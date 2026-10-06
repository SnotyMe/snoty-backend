<script setup lang="ts">
import * as locales from "@nuxt/ui/locale"
import * as z from "zod"
import { en as zod_en, de as zod_de } from "zod/locales"

const zodLocales = {
	en: zod_en,
	de: zod_de,
}

useHead({
	titleTemplate: titleChunk => titleChunk ? `${titleChunk} | Snoty` : "Snoty",
	link: [
		{ rel: "icon", href: "/favicon.png" },
	],
})

const { locale } = useI18n()

watch(locale, async function (newLocale) {
	const zodLocale = zodLocales[newLocale]
	z.config(zodLocale())
}, { immediate: true })
</script>

<template>
	<UApp :locale="locales[locale]">
		<NuxtLayout>
			<NuxtPage/>
		</NuxtLayout>
	</UApp>
</template>
