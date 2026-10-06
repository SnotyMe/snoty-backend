<script setup lang="ts">
import AppLocaleSelector from "#layers/base/app/components/framework/AppLocaleSelector.vue"
import { useUserSettingsStore } from "~/stores/userSettings"

useHead({
	title: "Settings",
})

const { t } = useI18n({ useScope: "local" })

const settings = useUserSettingsStore()
</script>

<i18n lang="yaml">
en:
  language: Language
  dateFormat: Date Format
  colorScheme: Color Scheme
de:
  language: Sprache
  dateFormat: Datumsformat
  colorScheme: Farbschema
</i18n>

<template>
	<UForm
		id="settings"
	>
		<UPageCard variant="subtle">
			<UFormField
				name="language"
				:label="t('language')"
				required
				class="flex max-xs:flex-col justify-between items-center gap-4"
			>
				<AppLocaleSelector/>
			</UFormField>
			<USeparator/>
			<UFormField
				name="dateFormat"
				:label="t('dateFormat')"
				required
				class="flex max-xs:flex-col justify-between items-center gap-4"
			>
				<USelect
					v-model="settings.dateFormat"
					:items="[
						...Object.values(settings.dateFormatDefault),
					]"
				/>
			</UFormField>
			<USeparator/>
			<UFormField
				name="colorScheme"
				:label="t('colorScheme')"
				required
				class="flex max-xs:flex-col justify-between items-center gap-4"
			>
				<UColorModeSelect/>
			</UFormField>
		</UPageCard>
	</UForm>
</template>
