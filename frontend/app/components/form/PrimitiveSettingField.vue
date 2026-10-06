<script setup lang="ts">
import type {
	SchemaField,
	SchemaFieldDetailsCredentialDetails,
	SchemaFieldDetailsDurationDetails,
	SchemaFieldDetailsEnumDetails,
	SchemaFieldDetailsPlaintextDetails,
} from "~/api/backend/generated"
import PlaintextSettingField from "~/components/form/PlaintextSettingField.vue"
import CredentialSettingField from "~/components/form/CredentialSettingField.vue"
import type { CredentialRef } from "~/types/credential"
import CodeSettingField from "~/components/form/CodeSettingField.vue"
import DurationSettingField from "~/components/form/DurationSettingField.vue"

defineProps<{
	field: SchemaField
}>()

const modelValue = defineModel({ required: true })
</script>

<template>
	<div class="setting-container w-full">
		<UCheckbox v-if="field.type === 'Boolean'" v-model="modelValue"/>
		<USelect
			v-else-if="field.type === 'Enum'"
			v-model="modelValue as never"
			:items="(field.details as SchemaFieldDetailsEnumDetails)?.values"
			value-key="value"
			label-key="displayName"
			variant="outline"
			:ui="{ base: 'bg-transparent' }"
			class="w-full"
		/>
		<CodeSettingField
			v-else-if="field.type === 'Plaintext' && (field.details as SchemaFieldDetailsPlaintextDetails)?.language"
			v-model="modelValue as string"
			:details="field.details as SchemaFieldDetailsPlaintextDetails"
		/>
		<PlaintextSettingField
			v-else-if="field.type === 'Plaintext'"
			v-model="modelValue as string"
			:field="field"
		/>
		<UInputNumber
			v-else-if="field.type === 'Int'"
			v-model="modelValue as never"
			:format-options="{ useGrouping: false }"
			variant="outline"
			:ui="{ base: 'bg-transparent' }"
			class="w-full"
		/>
		<CredentialSettingField
			v-else-if="field.type === 'Credential'"
			v-model="modelValue as CredentialRef | null"
			:details="field.details as SchemaFieldDetailsCredentialDetails"
		/>
		<DurationSettingField
			v-else-if="field.type === 'Duration'"
			v-model="modelValue as string"
			:details="field.details as SchemaFieldDetailsDurationDetails"
		/>
		<p v-else class="text-error">
			Unknown field type: {{ field.type }}
		</p>
	</div>
</template>
