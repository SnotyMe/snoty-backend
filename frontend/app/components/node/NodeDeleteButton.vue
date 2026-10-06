<script setup lang="ts">
import { useFlowNodes } from "~/composables/node/useFlowNodes"
import { useFlowState } from "~/composables/flow/useFlowState"

const props = defineProps<{
	nodeId: string
}>()

const { deleteNode } = useFlowNodes()
const flowState = useFlowState()

const onDelete = async () => {
	await deleteNode(props.nodeId)
	flowState.deleteNode(props.nodeId)
}
</script>

<template>
	<UPopover arrow :content="{ sideOffset: 0 }">
		<UButton
			icon="i-lucide-trash-2"
			variant="link"
			color="neutral"
			size="xs"
		/>

		<template #content="{ close }">
			<UEmpty
				icon="i-lucide-trash-2"
				title="Are you sure you want to delete this Node?"
				description="This action cannot be undone."
				:actions="[
					{
						icon: 'i-lucide-trash-2',
						label: 'Delete',
						color: 'error',
						onClick: onDelete,
					},
					{
						icon: 'i-lucide-x',
						label: 'Cancel',
						color: 'neutral',
						variant: 'ghost',
						onClick: close,
					},
				]"
			/>
		</template>
	</UPopover>
</template>
