<i18n lang="yaml">
en:
  title: Are you sure you want to delete "{name}"?
  description: The Flow will be deleted, along with all its Nodes, data, logs, just, everything!
  confirm: Confirm
  success: Successfully deleted "{name}"
</i18n>

<script setup lang="ts">
import type { StandaloneWorkflow } from "~/api/backend/generated"
import { useFlow } from "~/composables/flow/useFlow.ts"

const { t } = useI18n()

const props = defineProps<{
	flow: StandaloneWorkflow
}>()

const toast = useToast()

const { deleteFlow } = useFlow(props.flow.id)

const emit = defineEmits<{
	(e: "close", confirmed: boolean): void
}>()

async function onConfirm() {
	await deleteFlow()

	toast.add({
		color: "error",
		title: t("success", { name: props.flow.name }),
	})

	emit("close", true)
}
</script>

<template>
	<UModal :title="t('title', { name: flow.name })" :description="t('description')">
		<template #footer>
			<UButton color="error" @click="onConfirm">
				{{ t("confirm") }}
			</UButton>
		</template>
	</UModal>
</template>
