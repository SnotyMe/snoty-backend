<i18n lang="yaml">
en:
  importFlow: Import Flow
de:
  importFlow: Flow importieren
</i18n>

<script setup lang="ts">
const { t } = useI18n({ useScope: "local" })
</script>

<template>
	<UButton
		to="/flows/import"
		icon="i-lucide-upload"
		:label="t('importFlow')"
		variant="subtle"
		class="cursor-pointer"
	/>
</template>
