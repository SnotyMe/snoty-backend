<script setup lang="ts">
import type { SchemaField, SchemaFieldDetailsMapDetails } from "~/api/backend/generated"
import type { DynamicFormValues } from "~/types/node"
import PolySettings from "~/components/form/PolySettings.vue"

const props = defineProps<{
	metadata: SchemaField & { details: SchemaFieldDetailsMapDetails }
}>()

const keyDetails = props.metadata.details.keyDetails!
const keyField: SchemaField = {
	...props.metadata,
	type: keyDetails.type,
	details: keyDetails,
}

const valueDetails = props.metadata.details.valueDetails!
const valueField: SchemaField = {
	...props.metadata,
	type: valueDetails.type,
	details: valueDetails,
}

const modelValue = defineModel<DynamicFormValues>({ required: true })

const renameKey = (oldKey: string, newKey: string) => {
	if (oldKey === newKey) return
	modelValue.value[newKey] = modelValue.value[oldKey] as never
	// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
	delete modelValue.value[oldKey]
}
</script>

<template>
	<table class="table border-collapse">
		<tbody>
			<tr v-for="field of Object.keys(modelValue)" :key="field" class="not-last:border-b not-last:border-accented *:p-2">
				<th class="w-min">
					<UInput
						:default-value="field"
						variant="outline"
						:ui="{ base: 'bg-transparent ring-muted' }"
						class="block appearance-none"
						@change="renameKey(field, ($event.target as HTMLInputElement).value)"
					/>
					<div class="mx-1 invisible h-0">
						'{{ field }}'
					</div>
				</th>
				<th class="w-full">
					<PolySettings v-model="modelValue[field]" :metadata="valueField"/>
				</th>
			</tr>
		</tbody>
	</table>
	<UButton
		icon="i-lucide-plus"
		variant="ghost"
		@click="modelValue[getDefaultValue(keyField) as string || 'newkey'] = getDefaultValue(valueField) as never"
	/>
</template>
