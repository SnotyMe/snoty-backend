<i18n lang="yaml">
en:
  settings: Settings
  input: Input
  output: Output
  query:
    placeholder: Search Node...
de:
  settings: Einstellungen
  input: Eingabe
  output: Ausgabe
  query:
    placeholder: Node suchen...
</i18n>

<script setup lang="ts">
import FlowMenuDrawer from "~/components/flow/button/FlowMenuDrawer.vue"
import { FlowDrawer } from "~/types/drawer"
import NodeFieldRow from "~/components/node/NodeFieldRow.vue"
import { type NodeMetadata, NodeMetadataStereotypeEnum } from "~/api/backend/generated"
import { defaultRecordFromSchema } from "~/utils/node"
import { useFlowNodes } from "~/composables/node/useFlowNodes"
import { useFlowState } from "~/composables/flow/useFlowState.ts"
import PillFilter from "~/components/ui/PillFilter.vue"

const { t } = useI18n({ useScope: "local" })
const { createNode } = useFlowNodes()
const flowState = useFlowState()

const open = defineModel<FlowDrawer | undefined>()
const query = ref("")

const FILTER_ALL = "all"

const explicitStereotypeFilter = ref()
const stereotypeFilter = computed({
	get: () => explicitStereotypeFilter.value
		?? (flowState.nodes.value.length > 0 ? FILTER_ALL : NodeMetadataStereotypeEnum.Start),
	set: it => explicitStereotypeFilter.value = it,
})
const stereotypeItems = [
	{ label: "All", value: FILTER_ALL },
	...Object.entries(NodeMetadataStereotypeEnum)
	// eslint-disable-next-line array-element-newline
		.map(([label, value]) => ({ label, value })),
]

const { metadata } = storeToRefs(useNodeMetadataStore())
const items = computed(() => metadata.value
	?.filter(({ metadata }) => {
		const requiredStereotype = stereotypeFilter.value

		return metadata.displayName.toLowerCase().includes(query.value.toLowerCase())
			&& (requiredStereotype == FILTER_ALL || metadata.stereotype == stereotypeFilter.value)
	})
	?.map(({ metadata }) => ({
		label: metadata.displayName,
		icon: metadata.icon,
		metadata,
	})),
)

async function addNode(metadata: NodeMetadata) {
	const settings = {
		...defaultRecordFromSchema(metadata.settings),
		name: metadata.displayName,
	}

	const created = await createNode({
		type: metadata.type,
		name: metadata.displayName,
		position: {
			x: 0,
			y: 0,
			width: 350,
			height: 250,
		},
		settings,
	})

	flowState.createNode(created)
	open.value = undefined
}
</script>

<template>
	<FlowMenuDrawer
		:is-open="open === FlowDrawer.NodeAdd"
		horizontal-align="left"
		width="min(40em, 100%)"
		height="min(50em, 100%)"
		inner-class="flex flex-col gap-2"
	>
		<UInput
			v-model="query"
			icon="i-lucide-search"
			variant="subtle"
			:placeholder="t('query.placeholder')"
			:ui="{ trailing: 'pe-0' }"
		>
			<template v-if="query.length" #trailing>
				<UButton
					color="neutral"
					variant="link"
					size="lg"
					icon="i-lucide-x"
					@click="query = ''"
				/>
			</template>
		</UInput>
		<PillFilter
			v-model="stereotypeFilter"
			:items="stereotypeItems"
			class="block"
			:ui="{ trigger: 'first-of-type:max-w-min' }"
		/>
		<UAccordion
			:items="items"
			class="flex-1 overflow-y-auto"
		>
			<template #leading="{ item }">
				<UIcon
					v-if="item.icon"
					:name="`i-${item.icon.name}`"
					:style="item.icon.color ? `color: ${item.icon.color}` : ''"
					class="size-5"
				/>
			</template>
			<template #body="{ item }">
				<div class="grid grid-cols-[auto_1fr] gap-x-4 grow items-center">
					<NodeFieldRow v-if="item.metadata.input" :name="t('input')" :fields="item.metadata.input"/>
					<NodeFieldRow v-if="item.metadata.settings" :name="t('settings')" :fields="item.metadata.settings"/>
					<NodeFieldRow v-if="item.metadata.output" :name="t('output')" :fields="item.metadata.output"/>
				</div>
			</template>
			<template #trailing="{ item }">
				<span class="ms-auto">
					<UTheme :ui="{ button: { base: 'p-1' } }">
						<UButton
							icon="i-lucide-chevron-down"
							variant="link"
							color="neutral"
							size="lg"
						/>
						<UButton
							icon="i-lucide-plus"
							variant="ghost"
							size="lg"
							loading-auto
							@click.stop="addNode(item.metadata)"
						/>
					</UTheme>
				</span>
			</template>
		</UAccordion>
	</FlowMenuDrawer>
</template>
