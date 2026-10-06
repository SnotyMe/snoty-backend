<script setup lang="ts" generic="T">
import type { SchemaField, SchemaFieldDetailsCollectionDetails } from "~/api/backend/generated"
import PolySettings from "~/components/form/PolySettings.vue"
import { getDefaultValue } from "~/utils/node"

const props = defineProps<{
	metadata: SchemaField & { details: SchemaFieldDetailsCollectionDetails }
}>()

const modelValue = defineModel<T[]>({ required: true })

const elementDetails = props.metadata.details.elementDetails!
const fieldDetails = {
	...props.metadata,
	type: elementDetails.type,
	details: elementDetails,
}
</script>

<template>
	<table class="border-collapse w-full">
		<tbody>
			<tr v-for="(_, index) in modelValue" :key="index" class="*:pl-2 *:py-1">
				<th class="flex gap-1 flex-row items-center">
					<PolySettings v-model="modelValue[index]" :metadata="fieldDetails"/>
					<UButton icon="i-lucide-minus" variant="ghost" @click="modelValue.splice(index, 1)"/>
				</th>
			</tr>
		</tbody>
	</table>
	<UButton icon="i-lucide-plus" variant="ghost" @click="modelValue.push(getDefaultValue(fieldDetails) as T)"/>
</template>
