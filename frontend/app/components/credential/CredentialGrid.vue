<script setup lang="ts">
import type {
	CredentialDefinitionWithStatisticsDto, CredentialDto,
	PotentiallyAccessibleCredentialDto,
} from "~/api/backend/generated"
import { useCredentials } from "~/composables/credential/useCredentials"

const { updateCredential, deleteCredential } = useCredentials()
const updateCredentialDebounced = useDebounceFn(updateCredential, 100, { maxWait: 2500 })

const items = defineModel<PotentiallyAccessibleCredentialDto[]>({ required: true })

defineProps<{
	credentialDefinitions: CredentialDefinitionWithStatisticsDto[]
}>()

const emit = defineEmits<{
	(e: "delete", credential: PotentiallyAccessibleCredentialDto): void
}>()

async function ondelete(credential: CredentialDto) {
	await deleteCredential(credential)

	emit("delete", credential)
}
</script>

<template>
	<ul class="h-min max-h-full overflow-y-auto grid grid-cols-2 gap-3">
		<li
			v-for="(credential, idx) in items"
			:key="credential.id"
		>
			<Credential
				v-model="items[idx]!"
				:credential-definition="credentialDefinitions.find(it => it.type === credential.type)!"
				@update="updateCredentialDebounced"
			>
				<template v-if="credential.data" #action>
					<UButton
						icon="i-lucide-trash-2"
						variant="link"
						color="neutral"
						size="xs"
						class="opacity-0 group-hover:opacity-100"
						@click="ondelete(credential as CredentialDto)"
					/>
				</template>
			</Credential>
		</li>
	</ul>
</template>
