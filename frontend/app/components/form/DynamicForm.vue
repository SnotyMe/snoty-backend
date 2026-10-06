<script setup lang="ts">
import type { SchemaField } from "~/api/backend/generated"
import type { DynamicFormValues } from "~/types/node"
import PolySettings from "~/components/form/PolySettings.vue"

defineProps<{
	fields: SchemaField[]
}>()

const modelValue = defineModel<DynamicFormValues>({ required: true })

const hasChildren = (type: string) => {
	return [
		"Collection",
		"Map",
		"Object",
	].includes(type)
}
</script>

<template>
	<table class="border-collapse w-full">
		<tbody>
			<tr v-for="field in fields" :key="field.name" class="not-last:border-b not-last:border-accented *:p-2">
				<th :colspan="hasChildren(field.type) ? 2 : 1" class="w-min align-middle">
					<p class="text-sm font-light text-highlighted whitespace-nowrap w-min" :title="field.description ?? undefined">{{ field.displayName }}</p>

					<PolySettings v-if="hasChildren(field.type)" v-model="modelValue[field.name]" :metadata="field"/>
				</th>
				<th v-if="!hasChildren(field.type)">
					<PolySettings v-model="modelValue[field.name]" :metadata="field"/>
				</th>
			</tr>
		</tbody>
	</table>
</template>
