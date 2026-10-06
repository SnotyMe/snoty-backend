<script setup lang="ts">
import type {
	SchemaField, SchemaFieldDetailsCollectionDetails, SchemaFieldDetailsMapDetails, SchemaFieldDetailsObjectDetails,
} from "~/api/backend/generated"
import DynamicForm from "~/components/form/DynamicForm.vue"
import CollectionSettings from "~/components/form/CollectionSettings.vue"
import MapSettings from "~/components/form/MapSettings.vue"
import PrimitiveSettingField from "~/components/form/PrimitiveSettingField.vue"
import type { DynamicFormValues } from "~/types/node"

defineProps<{
	metadata: SchemaField
}>()

const modelValue = defineModel({ required: true })
</script>

<template>
	<UTheme :ui="{ button: { base: 'p-0' } }">
		<CollectionSettings
			v-if="metadata.type === 'Collection'"
			v-model="modelValue as never[]"
			:metadata="metadata as SchemaField & { details: SchemaFieldDetailsCollectionDetails }"
		/>
		<MapSettings
			v-else-if="metadata.type === 'Map'"
			v-model="modelValue as DynamicFormValues"
			:metadata="metadata as SchemaField & { details: SchemaFieldDetailsMapDetails }"
		/>
		<DynamicForm
			v-else-if="metadata.type === 'Object'"
			v-model="modelValue as DynamicFormValues"
			:fields="(metadata.details as SchemaFieldDetailsObjectDetails)?.schema"
		/>
		<PrimitiveSettingField
			v-else
			v-model="modelValue"
			:field="metadata"
		/>
	</UTheme>
</template>
