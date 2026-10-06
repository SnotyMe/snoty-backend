<i18n lang="yaml">
en:
  title: No Flows found.
  description: It looks like you haven't created any Flows yet. Create one to get started.
de:
  title: Keine Flows gefunden.
  description: Es sieht so aus, als ob du noch keine Flows hast. Erstelle einen um zu starten.
</i18n>

<script setup lang="ts">
const { t } = useI18n({ useScope: "local" })

const emit = defineEmits<{
	(e: "openCreate"): void
}>()

const actions = [
	{
		icon: "i-lucide-plus",
		label: "Create Flow",
		onClick: () => emit("openCreate"),
	},
]
</script>

<template>
	<UEmpty
		icon="i-lucide-ghost"
		:title="t('title')"
		:description="t('description')"
		:actions="actions"
		variant="naked"
	/>
</template>
