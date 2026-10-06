<script setup lang="ts">
import { BaseEdge, EdgeLabelRenderer, type EdgeProps, getBezierPath, useVueFlow } from "@vue-flow/core"

const props = defineProps<EdgeProps>()

const { removeEdges } = useVueFlow()

const path = computed(() => getBezierPath(props))
</script>

<template>
	<BaseEdge
		:id="id"
		:style="style"
		:path="path[0]"
		:marker-end="markerEnd"
	/>

	<EdgeLabelRenderer>
		<div
			:style="{
				pointerEvents: 'all',
				position: 'absolute',
				transform: `translate(-50%, -50%) translate(${path[1]}px,${path[2]}px)`,
			}"
			class="nodrag nopan"
		>
			<UButton
				variant="subtle"
				color="neutral"
				class="rounded-full leading-0 p-1 bg-default hover:bg-muted"
				@click="removeEdges(id)"
			>
				<UIcon name="i-lucide-x"/>
			</UButton>
		</div>
	</EdgeLabelRenderer>
</template>
