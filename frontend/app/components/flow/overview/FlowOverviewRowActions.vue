<i18n lang="yaml">
en:
  view: View
  delete: Delete Flow
</i18n>

<script setup lang="ts">
import type { DropdownMenuItem } from "@nuxt/ui"
import type { StandaloneWorkflow } from "~/api/backend/generated"
import { LazyFlowDeleteConfirmationDialog } from "#components"

const props = defineProps<{
	flow: StandaloneWorkflow
}>()

const overlay = useOverlay()
const deleteModal = overlay.create(LazyFlowDeleteConfirmationDialog)

const emit = defineEmits<{
	(e: "refresh"): void
}>()

const items = computed<DropdownMenuItem[]>(() => [
	{
		label: $t("ui.copyId"),
		icon: "i-lucide-copy",
		onSelect: () => navigator.clipboard.writeText(props.flow.id),
	},
	{
		label: $t("delete"),
		icon: "i-lucide-trash-2",
		color: "error",
		onSelect: () => deleteModal.open({
			flow: props.flow,
			onClose: (accepted) => {
				if (!accepted) return
				emit("refresh")
			},
		}),
	},
])

const isOpen = ref(false)

const triggerRef = useTemplateRef("trigger")
</script>

<template>
	<div ref="trigger" class="flex sm:opacity-0 group-hover:opacity-100" :class="{ 'opacity-100!': isOpen }">
		<UDropdownMenu
			v-model:open="isOpen"
			:items="items"
			:content="{ reference: triggerRef ?? undefined }"
			arrow
		>
			<UButton icon="i-lucide-ellipsis-vertical" color="neutral" variant="link"/>
		</UDropdownMenu>

		<UButton
			trailing-icon="i-lucide-arrow-right"
			:label="$t('view')"
			:href="`/flows/${flow.id}`"
		/>
	</div>
</template>
