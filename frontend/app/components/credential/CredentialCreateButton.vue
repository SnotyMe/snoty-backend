<i18n lang="yaml">
en:
  title: Create Credential
  form:
    scope: Scope
    role:
      label: Role
      placeholder: Enter required role
  create: Create
de:
  title: Zugangsdaten erstellen
  form:
    scope: Geltungsbereich
    role:
      label: Rolle
      placeholder: Benötigte Rolle eingeben
  create: Erstellen
</i18n>

<script setup lang="ts">
import {
	type CredentialCreateDto,
	type CredentialDefinitionWithStatisticsDto,
	CredentialDtoScopeEnum as CredentialScope,
} from "~/api/backend/generated"
import { defaultRecordFromSchema } from "~/utils/node"
import { useAuth } from "~/stores/auth"
import { ROLE_ADMIN } from "~/utils/auth"
import { useCredentials } from "~/composables/credential/useCredentials"

const { t } = useI18n({ useScope: "local" })

const open = ref<boolean>(false)

defineExpose({
	open: () => open.value = true,
})

const { hasRole } = useAuth()
const { data: isAdmin } = useAsyncData("role-admin", () => hasRole(ROLE_ADMIN))

const { createCredential } = useCredentials()

const props = defineProps<{
	credentialDefinition: CredentialDefinitionWithStatisticsDto
}>()

const emit = defineEmits<{
	(e: "create"): void
}>()

export type CredentialCreateFormData = Omit<CredentialCreateDto, "role">
	& { scope: CredentialScope, role: string | undefined }

const initialFormData = () => ({
	id: null,
	type: props.credentialDefinition.type,
	name: "",
	scope: CredentialScope.User,
	role: undefined,
	data: defaultRecordFromSchema(props.credentialDefinition.schema),
})

const form = ref<CredentialCreateFormData & { id: null }>(initialFormData())

async function oncreate(close: () => void) {
	const data = form.value
	await createCredential({
		...data,
		role: data.role
			? {
					name: data.role,
				}
			: undefined,
	})

	form.value = initialFormData()
	close()
	emit("create")
}
</script>

<template>
	<UPopover v-model:open="open" :content="{ align: 'end', side: 'bottom' }">
		<UButton
			icon="i-lucide-plus"
			color="primary"
			variant="subtle"
			class="cursor-pointer"
		/>

		<template #content="{ close }">
			<UForm v-if="isAdmin" class="p-2 space-y-2">
				<UFormField
					name="scope"
					:label="t('form.scope')"
					orientation="horizontal"
				>
					<USelect
						v-model="form.scope"
						:items="[
							{ label: 'User', value: CredentialScope.User },
							{ label: 'Role', value: CredentialScope.Role },
							{ label: 'Global', value: CredentialScope.Global },
						]"
						class="w-32"
					/>
				</UFormField>
				<UFormField
					v-if="form.scope === CredentialScope.Role"
					name="role"
					:label="t('form.role.label')"
					orientation="horizontal"
				>
					<UInput
						v-model="form.role"
						:placeholder="t('form.role.placeholder')"
					/>
				</UFormField>
			</UForm>
			<Credential
				v-model="form"
				:credential-definition="credentialDefinition"
				usage="CREATING"
				class="border-none"
			/>
			<div class="p-2 flex justify-end">
				<UButton class="r-0" @click="oncreate(close)">
					{{ t('create') }}
				</UButton>
			</div>
		</template>
	</UPopover>
</template>
