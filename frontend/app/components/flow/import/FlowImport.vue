<i18n lang="yaml">
en:
  import: Import
  showIntermediateNodes: Show intermediate Nodes
  form:
    file:
      label: Flow File
      description: Drop or select a Snoty Flow JSON
    name:
      label: Flow Name
      placeholder: My Flow
  validation:
    required: Name is required
  success: Flow '{name}' imported successfully
  error:
    importFailed: Failed to import flow
    invalidJson: The provided file is not a valid flow JSON file.
de:
  import: Importieren
  showIntermediateNodes: Zwischenschritte anzeigen
  form:
    file:
      label: Flow-Datei
      description: Selektiere oder ziehe eine Snoty Flow JSON-Datei herein
    name:
      label: Name des Flows
      placeholder: Mein Flow
  validation:
    required: Name ist erforderlich
  success: Flow '{name}' erfolgreich importiert
  error:
    importFailed: Fehler beim Importieren des Flows
    invalidJson: Die angegebene Datei ist keine gültige Flow-JSON-Datei.
</i18n>

<script setup lang="ts">
import FlowImportNode from "~/components/flow/overview/FlowImportNode.vue"
import { type ExportFlow, type ImportFlow, NodeMetadataStereotypeEnum } from "~/api/backend/generated"
import { useFlows } from "~/composables/flow/useFlows.ts"
import { useNodeMetadataStore } from "~/stores/nodeMetadata.ts"
import { breakpointsTailwind } from "@vueuse/core"
import { z } from "zod"
import type { DynamicFormValues } from "~/types/node.ts"

const { t } = useI18n({ useScope: "local" })

const toast = useToast()
const { importFlow } = useFlows()
const nodeMetadataStore = useNodeMetadataStore()
const breakpoints = useBreakpoints(breakpointsTailwind)
const usesOneColumnLayout = breakpoints.smallerOrEqual("md")

const [
	DefineFileField,
	ReuseFileField,
] = createReusableTemplate()

const file = ref<File>()
const fileError = ref<string>()

const root = ref<ImportFlow>()
const showIntermediateNodes = ref(false)

const onFileSelected = async (selected: File | null | undefined) => {
	file.value = selected ?? undefined
	if (!selected) {
		root.value = undefined
		fileError.value = undefined
		return
	}

	const text = await selected.text()
	try {
		const exported: ExportFlow = JSON.parse(text)
		if (exported.templateName == null || exported.nodes == null || !Array.isArray(exported.nodes)) {
			throw Error("Invalid import JSON")
		}

		root.value = {
			name: exported.templateName,
			nodes: exported.nodes,
			settings: exported.settings,
		}
	} catch {
		fileError.value = t("error.invalidJson")
	}
}

const visibleNodes = computed(() => {
	if (!root.value?.nodes) return []
	return root.value.nodes.filter((node) => {
		if (showIntermediateNodes.value) return true
		const meta = nodeMetadataStore.metadata?.find(it => it.type === node.type)?.metadata
		return meta?.stereotype !== NodeMetadataStereotypeEnum.Middle
	})
})

const schema = z.object({
	name: z.string().min(1, { error: t("validation.required") }),
})

const form = useTemplateRef("form")

const onImport = async () => {
	try {
		const value = root.value
		if (!value) return
		const result = await importFlow(value)

		toast.add({
			color: "success",
			title: t("success", { name: value.name }),
		})

		await navigateTo(`/flows/${result.id}`)
	} catch {
		toast.add({
			color: "error",
			title: t("error.importFailed"),
		})
	}
}
</script>

<template>
	<DefineFileField>
		<UFileUpload
			v-model="file"
			accept="application/json"
			layout="list"
			position="inside"
			:highlight="!file"
			:label="t('form.file.label')"
			:description="t('form.file.description')"
			:ui="{ base: root ? 'p-0 border-none' : '' }"
			:class="root ? '' : 'h-64'"
			@update:model-value="onFileSelected"
		/>
	</DefineFileField>

	<ReuseFileField v-if="!file || !root"/>
	<UAlert
		v-if="fileError"
		:title="fileError"
		color="error"
		variant="subtle"
		icon="i-lucide-alert-circle"
	/>
	<UForm
		v-else-if="root"
		ref="form"
		:schema="schema"
		:state="root"
		:class="usesOneColumnLayout ? 'import-form-1col' : 'import-form'"
		class="w-full max-h-full grid gap-4 items-start"
		@submit="onImport"
	>
		<div class="flex flex-col gap-4" style="grid-area: settings">
			<UCard variant="subtle" :ui="{ footer: 'flex justify-end' }">
				<template #title>
					<ReuseFileField/>
				</template>

				<UFormField
					name="name"
					:label="t('form.name.label')"
				>
					<UInput
						v-model="root.name"
						:placeholder="t('form.name.placeholder')"
						class="w-full"
					/>
				</UFormField>

				<template #footer>
					<UButton
						type="submit"
						icon="i-lucide-send"
						:label="t('import')"
						:disabled="(form?.errors?.length ?? -1) > 0"
						loading-auto
						class="place-self-end"
					/>
				</template>
			</UCard>

			<div style="grid-area: options">
				<USwitch v-model="showIntermediateNodes" :label="t('showIntermediateNodes')" class="flex-row-reverse gap-2"/>
			</div>
		</div>

		<div
			class="flex flex-col gap-2 h-full overflow-y-auto -mr-2 pr-2"
			style="grid-area: nodes"
		>
			<FlowImportNode
				v-for="(node, index) in visibleNodes"
				:key="node.id || index"
				v-model:settings="node.settings as DynamicFormValues"
				v-model:name="node.name as string"
				:type="node.type!"
			/>
		</div>
	</UForm>
</template>

<style>
.import-form-1col {
	grid-template-areas:
    'settings'
    'options'
    'nodes';
}

.import-form {
	grid-template-areas:
	'settings nodes'
	'options nodes';
	grid-template-columns: auto 1fr;
}
</style>
