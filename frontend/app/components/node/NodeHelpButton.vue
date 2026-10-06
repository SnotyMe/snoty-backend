<script setup lang="ts">
import type { NodeMetadata } from "~/api/backend/generated"

const props = defineProps<{
	metadata: NodeMetadata
}>()

const nodeHelpStore = useNodeHelpStore()
const { toggleHelpFor } = nodeHelpStore
const { selectedMetadata, docsSitemap } = storeToRefs(nodeHelpStore)

const hasDocsPage = computed(() => docsSitemap.value?.[props.metadata.type])

const selected = computed(() => selectedMetadata?.value?.type === props.metadata.type)
</script>

<template>
	<UButton
		v-if="hasDocsPage"
		icon="i-lucide-help-circle"
		:color="selected ? 'primary' : 'neutral'"
		:variant="selected ? 'outline' : 'ghost'"
		@click="toggleHelpFor(metadata)"
	/>
</template>
