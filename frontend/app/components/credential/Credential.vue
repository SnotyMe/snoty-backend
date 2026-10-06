<script setup lang="ts">
import type {
	CredentialDto, SchemaFieldDetailsCredentialDetails,
	PotentiallyAccessibleCredentialDto,
} from "~/api/backend/generated"
import DynamicForm from "~/components/form/DynamicForm.vue"
import type { DynamicFormValues } from "~/types/node"
import { useOnChanged } from "~/composables/utils/useOnChanged.ts"

defineProps<{
	credentialDefinition: Pick<SchemaFieldDetailsCredentialDetails, "schema">
	usage?: "CREATING" | "FLOATING"
}>()

const emit = defineEmits<{
	(e: "delete"): void
	(e: "update", data: CredentialDto): void
}>()

const credential = defineModel<
	Omit<PotentiallyAccessibleCredentialDto, "id"> & { id: null | PotentiallyAccessibleCredentialDto["id"] }
>({ required: true })

useOnChanged(() => [
	credential.value.name,
	credential.value.data,
], () => {
	const value = credential.value
	if (!value.id || !value.data) return

	// TypeScript is too stupid to smart cast it for some reason
	emit("update", value as CredentialDto)
}, { deep: true })

const accessible = computed(() => credential.value.data != null)
</script>

<template>
	<div class="h-min flex flex-col bg-muted/50 rounded border border-accented divide-y divide-accented group">
		<div class="p-3 gap-2 bg-muted flex flex-row justify-between">
			<UInput
				v-if="accessible"
				v-model="credential.name"
				:variant="usage === 'CREATING' ? 'outline' : 'ghost'"
				placeholder="Name"
				:ui="{ root: 'w-full', base: 'text-default px-2 py-1 focus-visible:ring-2 focus-visible:ring-primary' }"
				size="xl"
			/>
			<p v-else>{{ credential.name }}</p>
			<slot name="action"/>
		</div>
		<div v-if="accessible" class="p-3">
			<DynamicForm
				v-if="credentialDefinition"
				v-model="credential.data as DynamicFormValues"
				:fields="credentialDefinition?.schema ?? []"
			/>
		</div>
	</div>
</template>
