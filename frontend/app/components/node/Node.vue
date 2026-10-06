<i18n lang="yaml">
en:
  unsaved: Unsaved
  saved: Saved
  error: Error
</i18n>

<script setup lang="ts">
import { type Dimensions, Handle, Position, useNode, type XYPosition } from "@vue-flow/core"
import { NodeResizer } from "@vue-flow/node-resizer"
import type { DynamicFormValues } from "~/types/node"
import { useNodeMetadataStore } from "~/stores/nodeMetadata"
import NodeSettings from "~/components/node/NodeSettings.vue"

import "@vue-flow/node-resizer/dist/style.css"
import LogLevelSelect from "~/components/primitives/LogLevelSelect.vue"
import type { StandaloneNode, StandaloneNodeLogLevelEnum } from "~/api/backend/generated"
import NodeDeleteButton from "~/components/node/NodeDeleteButton.vue"
import { useFlowNode } from "~/composables/node/useFlowNode"
import { useDebouncedAction } from "~/composables/utils/useDebouncedAction"
import { useOnChanged } from "~/composables/utils/useOnChanged.ts"
import NodeHelpButton from "~/components/node/NodeHelpButton.vue"
import NodeTriggerButton from "~/components/node/NodeTriggerButton.vue"

const { t } = useI18n({ useScope: "local" })

const { metadata: nodeMetadata } = useNodeMetadataStore()

const props = defineProps<{
	data: StandaloneNode
	type: string
	highlight?: boolean
}>()

const metadata = computed(() => nodeMetadata?.find(it => it.type === props.type)?.metadata)

const logLevel = defineModel<StandaloneNodeLogLevelEnum>("logLevel", { required: true })
const settings = defineModel<DynamicFormValues>("settings", { required: true })
const name = defineModel<string>("name", { required: true })

const node = useNode()
const hasInputEdges = computed(() => node.connectedEdges.value.filter(it => it.target === node.id).length > 0)
const hasOutputEdges = computed(() => node.connectedEdges.value.filter(it => it.source === node.id).length > 0)

const { updatePosition, updateName, updateLogLevel, updateSettings, trigger } = useFlowNode(node.id)

useOnChanged(
	() => [
		node.node.position,
		node.node.dimensions,
	] satisfies [XYPosition, Dimensions],
	([
		pos,
		dim,
	]) => updatePosition({
		x: Math.round(pos.x),
		y: Math.round(pos.y),
		width: Math.round(dim.width),
		height: Math.round(dim.height),
	}),
	{
		debounced: true,
		debounce: 1000,
		maxWait: 2500,
	},
)

const { call: debouncedSettingsUpdate, isUpdating, isAwaitingUpdate, error, isUpdateDone } = useDebouncedAction(
	updateSettings,
	{
		debounce: 1000,
		maxWait: 2500,
	},
)
watch(settings, debouncedSettingsUpdate, { deep: true })
</script>

<template>
	<div
		ref="container"
		class="h-full"
		:class="{ 'animate-highlight': highlight }"
	>
		<Handle
			v-if="metadata?.input"
			type="target"
			:position="Position.Left"
			class="handle size-2 rounded border border-inverted"
			:class="!hasInputEdges ? 'bg-error' : 'bg-muted'"
		/>

		<div
			v-if="metadata"
			class="h-full flex flex-col cursor-auto flow-node p-2 card bg-muted border border-accented rounded overflow-hidden"
		>
			<NodeResizer :min-width="250" :min-height="150"/>

			<div class="flex items-center">
				<!-- hardcoded size is necessary (for now) to avoid layout shift race condition -->
				<UIcon name="i-lucide-grip-vertical" class="drag-handle cursor-pointer"/>

				<UInput
					v-model="name"
					variant="ghost"
					color="primary"
					class="w-full nodrag"
					@change="updateName(name)"
				/>

				<NodeDropdownMenuButton :node="data" class="nodrag"/>
			</div>

			<NodeSettings v-model="settings" :metadata="metadata"/>

			<div class="nodrag mt-2 w-full flex justify-between gap-2">
				<div class="flex items-center gap-1">
					<NodeDeleteButton :node-id="node.id"/>
					<UBadge
						v-if="isAwaitingUpdate || isUpdating || isUpdateDone || error"
						icon="i-lucide-circle-small"
						:label="isAwaitingUpdate ? t('unsaved') : (error ? t('error') : t('saved'))"
						variant="soft"
						color="neutral"
						size="xs"
						class="gap-0.5"
					>
						<template #leading>
							<span v-if="isAwaitingUpdate || isUpdating" class="size-3 animate-pulse">
								⚪
							</span>
							<UIcon v-else-if="error" name="i-lucide-triangle-alert" class="size-2 pr-3"/>
							<UIcon v-else name="i-lucide-check" class="size-2 pr-3"/>
						</template>
					</UBadge>
				</div>

				<div class="flex gap-1">
					<NodeTriggerButton v-if="metadata.input == undefined" @trigger="input => trigger(input)"/>
					<LogLevelSelect
						v-model="logLevel"
						class="w-32"
						placeholder="Log Level"
						@update:model-value="updateLogLevel"
					/>
					<NodeHelpButton :metadata="metadata"/>
				</div>
			</div>
		</div>

		<Handle
			v-if="metadata?.output !== null"
			type="source"
			:position="Position.Right"
			class="handle size-2 rounded border border-inverted"
			:class="metadata?.stereotype !== 'END' && !hasOutputEdges ? 'bg-red-500!' : ''"
		/>
	</div>
</template>

<style>
.handle {
	z-index: 9;
}
</style>
