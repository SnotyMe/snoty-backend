<i18n lang="yaml">
en:
  showHiddenSettings: Show hidden Settings
de:
  showHiddenSettings: Versteckte Einstellungen anzeigen
</i18n>

<script setup lang="ts">
import type { NodeMetadata, SchemaField } from "~/api/backend/generated"
import type { DynamicFormValues } from "~/types/node"
import DynamicForm from "~/components/form/DynamicForm.vue"

const { t } = useI18n({ useScope: "local" })

const props = defineProps<{
	metadata?: NodeMetadata
	fields?: SchemaField[]
}>()

const settings = defineModel<DynamicFormValues>({ required: true })

const allFields = computed(() => props.fields ?? props.metadata?.settings ?? [])
const visibleFields = computed(() => allFields.value.filter(it => !it.hidden))
const hiddenFields = computed(() => allFields.value.filter(it => it.hidden))
</script>

<template>
	<div
		v-if="allFields.length > 0"
		class="nodrag nowheel flow-node-options grow table-wrap border-y-4 border-accented my-1 pr-0.5 overflow-y-auto"
	>
		<DynamicForm
			v-model="settings"
			:fields="visibleFields"
		/>

		<UCollapsible v-if="hiddenFields.length > 0" class="w-full">
			<UButton
				:label="t('showHiddenSettings')"
				color="neutral"
				variant="link"
				icon="i-lucide-chevron-right"
				:ui="{
					leadingIcon: 'group-data-[state=open]:rotate-90 transition-transform duration-300',
				}"
				class="group"
				block
			/>

			<template #content>
				<DynamicForm v-model="settings" :fields="hiddenFields"/>
			</template>
		</UCollapsible>
	</div>
</template>
