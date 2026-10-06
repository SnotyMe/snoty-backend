<script setup lang="ts">
import type { DynamicFormValues } from "~/types/node"
import { useNodeMetadataStore } from "~/stores/nodeMetadata"
import NodeHelpButton from "~/components/node/NodeHelpButton.vue"
import NodeSettings from "~/components/node/NodeSettings.vue"

const { metadata: nodeMetadata } = useNodeMetadataStore()

const props = defineProps<{
	type: string
}>()

const name = defineModel<string>("name", { required: true })
const settings = defineModel<DynamicFormValues>("settings", { required: true })

const metadata = computed(() => nodeMetadata?.find(it => it.type === props.type)?.metadata)
</script>

<template>
	<div
		v-if="metadata"
		class="flex flex-col p-3 card bg-muted border border-accented rounded gap-2"
	>
		<div class="flex items-center justify-between gap-2">
			<UInput
				v-model="name"
				variant="ghost"
				color="primary"
				class="w-full font-medium"
			/>
			<NodeHelpButton :metadata="metadata"/>
		</div>

		<NodeSettings v-model="settings" :metadata="metadata"/>
	</div>
</template>
