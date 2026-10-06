<template>
	<Credential
		v-model="credential"
		:credential-definition="details"
		usage="FLOATING"
		@update="updateCredentialDebounced"
	>
		<template #action>
			<UButton
				icon="i-lucide-x"
				variant="link"
				color="neutral"
				size="xl"
				@click="emit('close')"
			/>
		</template>
	</Credential>
</template>

<script setup lang="ts">
import type { SchemaFieldDetailsCredentialDetails, PotentiallyAccessibleCredentialDto } from "~/api/backend/generated"
import { useCredentials } from "~/composables/credential/useCredentials"

const { updateCredential } = useCredentials()
const updateCredentialDebounced = useDebounceFn(updateCredential, 500, { maxWait: 1000 })

const credential = defineModel<PotentiallyAccessibleCredentialDto>({ required: true })

defineProps<{
	details: SchemaFieldDetailsCredentialDetails
}>()

const emit = defineEmits<{
	(e: "close"): void
}>()
</script>
