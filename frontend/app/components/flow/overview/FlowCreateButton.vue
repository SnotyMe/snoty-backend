<i18n lang="yaml">
en:
  createFlow: Create Flow
  create: Create
  form:
    name:
      label: Name
      placeholder: My Flow
de:
  createFlow: Flow erstellen
  create: Erstellen
  form:
    name:
      label: Name
      placeholder: Mein Flow
</i18n>

<script setup lang="ts">
import { useFlows } from "~/composables/flow/useFlows"
import type { FlowCreateRequest } from "~/api/backend/generated"
import { z } from "zod"

const { t } = useI18n({ useScope: "local" })

const form = ref<FlowCreateRequest>({
	name: "",
	settings: {
		schedule: {
			type: "recurring",
			interval: "PT15M",
		},
	},
})

const schema = z.object({
	name: z.string().min(1, { error: $t("validation.required") }),
})

const { createFlow } = useFlows()

async function oncreate() {
	const createdFlow = await createFlow(form.value)

	navigateTo(`/flows/${createdFlow.id}`)
}

const open = ref(false)

defineExpose({
	open: () => open.value = true,
})
</script>

<template>
	<UPopover
		ref="popover"
		v-model:open="open"
		:content="{ align: 'end', side: 'bottom' }"
		:ui="{ content: 'p-2' }"
	>
		<UButton
			icon="i-lucide-plus"
			:label="t('createFlow')"
			color="primary"
			class="cursor-pointer"
		/>

		<template #content>
			<UForm :schema="schema" :state="form" @submit="oncreate">
				<UFormField
					name="name"
					:label="t('form.name.label')"
					orientation="horizontal"
				>
					<UInput
						v-model="form.name"
						:placeholder="t('form.name.placeholder')"
					/>
				</UFormField>

				<div class="mt-2 flex justify-end">
					<UButton
						class="r-0"
						type="submit"
						icon="i-lucide-check"
						loading-auto
					>
						{{ t('create') }}
					</UButton>
				</div>
			</UForm>
		</template>
	</UPopover>
</template>
