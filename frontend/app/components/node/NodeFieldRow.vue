<i18n lang="yaml">
en:
  dynamic: Dynamic
de:
  dynamic: Dynamisch
</i18n>

<script setup lang="ts">
import type { SchemaField } from "~/api/backend/generated"

const { t } = useI18n({ useScope: "local" })

defineProps<{
	name: string
	fields: SchemaField[]
}>()
</script>

<template>
	<span>{{ name }}</span>
	<div
		class="flex items-center gap-1 overflow-x-auto py-2"
		style="mask-image: linear-gradient(to right, rgba(0,0,0,1) 90%, rgba(0,0,0,0) 100%)"
	>
		<UBadge v-if="fields.length === 0">
			{{ t('dynamic') }}
		</UBadge>
		<UBadge
			v-for="field of fields"
			:key="field.name"
			:label="field.displayName"
			color="neutral"
		/>
	</div>
</template>
