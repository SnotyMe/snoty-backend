<template>
	<UPopover
		v-model:open="editModalOpen"
		:portal="false"
		:dismissible="false"
		:modal="false"
		:ui="{ content: 'w-(&#45;&#45;reka-popper-anchor-width) shadow-lg-center' }"
		class="z-10"
	>
		<template #anchor>
			<USelectMenu
				v-model="credentialId"
				v-model:open="selectModalOpen"
				:items="groupedCandidates"
				:loading="pending"
				value-key="id"
				label-key="name"
				variant="outline"
				:ui="{ base: 'bg-transparent', trailing: 'pe-1' }"
				icon="i-lucide-shield-check"
				class="w-full"
			>
				<template #default>
					<span>{{ credentialData?.name ?? ' ' }}</span>
				</template>

				<template #trailing>
					<div class="flex gap-0.5">
						<UTheme :ui="{ button: { base: 'rounded-sm' } }">
							<UButton
								v-if="modelValue && credentialData?.data"
								variant="subtle"
								color="neutral"
								size="xs"
								:icon="modelValue ? 'i-lucide-square-asterisk' : 'i-lucide-icon-grid-2x2-plus'"
								@click.stop="editModalOpen = !editModalOpen"
							/>
							<UButton
								v-if="modelValue"
								variant="subtle"
								color="neutral"
								size="xs"
								icon="i-lucide-minus"
								@click.stop="modelValue = null"
							/>
							<UButton
								v-if="!modelValue"
								variant="link"
								color="neutral"
								size="xs"
								icon="i-lucide-chevron-down"
							/>
						</UTheme>
					</div>
				</template>
			</USelectMenu>
		</template>

		<template #content="{ close }">
			<StandaloneCredentialManager
				v-if="credentialData"
				v-model="credentialData"
				:details="details"
				@close="close"
			/>
		</template>
	</UPopover>
</template>

<script setup lang="ts">
import type { CredentialRef } from "~/types/credential"
import { CredentialDtoScopeEnum, type SchemaFieldDetailsCredentialDetails } from "~/api/backend/generated"
import { useCredentials } from "~/composables/credential/useCredentials"
import type { SelectMenuItem } from "#ui/components/SelectMenu.vue"
import StandaloneCredentialManager from "~/components/credential/StandaloneCredentialManager.vue"

const { getCredential, enumerateCredentials } = useCredentials()

const modelValue = defineModel<CredentialRef | null>({ required: true })
const credentialId = computed({
	get: () => modelValue.value?.credentialId,
	set: (value: string | null) => modelValue.value = value ? { credentialId: value } : null,
})
const { data: credentialData } = useAsyncData(refDefault(credentialId, "none"), async () => {
	const value = credentialId?.value
	if (!value) return null

	return await getCredential(value)
}, { deep: true })

const props = defineProps<{
	details: SchemaFieldDetailsCredentialDetails
}>()

const { data: candidates, pending } = useAsyncData(
	props.details.credentialType,
	() => enumerateCredentials(props.details.credentialType),
	{ lazy: true },
)
const groupedCandidates = computed<SelectMenuItem[]>(() => {
	const value = candidates.value
	if (!value) return []

	const grouped = Object.groupBy(value, it => it.scope)
	return [
		CredentialDtoScopeEnum.User,
		CredentialDtoScopeEnum.Role,
		CredentialDtoScopeEnum.Global,
	].flatMap((scope) => {
		const credentials = grouped[scope]
		if (!credentials) return []
		return [
			{ type: "label", name: scope },
			...credentials.map(cred => ({ id: cred.id, name: cred.name })),
		]
	})
})

const open = ref<undefined | "select" | "edit">(undefined)
const selectModalOpen = computed({
	get: () => open.value === "select",
	set: val => open.value = val ? "select" : undefined,
})
const editModalOpen = computed({
	get: () => open.value === "edit",
	set: val => open.value = val ? "edit" : undefined,
})
</script>
