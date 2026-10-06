<i18n lang="yaml">
en:
  search:
    placeholder: Search Flows...
  sort:
    lastExecution:
      asc: Oldest Executed
      desc: Latest Executed
    name:
      asc: Name (A-Z)
      desc: Name (Z-A)
    modifiedAt:
      desc: Last Modified
de:
  search:
    placeholder: Flows suchen...
  sort:
    lastExecution:
      asc: Älteste Ausführung
      desc: Letzte Ausführung
    name:
      asc: Name (A-Z)
      desc: Name (Z-A)
    modifiedAt:
      desc: Letzte Modifizierung
</i18n>

<script setup lang="ts">
import { useFlowFiltersStore } from "~/stores/flowFilters"

const { t } = useI18n({ useScope: "local" })

const flowFiltersStore = useFlowFiltersStore()
const { query, sort } = storeToRefs(flowFiltersStore)
const sortOptions = flowFiltersStore.sortOptions.map(it => ({
	...it,
	label: t(`sort.${it.key}.${it.desc ? "desc" : "asc"}`),
}))

const queryInput = useTemplateRef("queryInput")
defineShortcuts({
	"/": () => {
		queryInput.value?.inputRef?.focus()
	},
})
</script>

<template>
	<div class="flex justify-between">
		<UInput
			ref="queryInput"
			v-model="query"
			:placeholder="t('search.placeholder')"
			icon="i-lucide-search"
		>
			<template #trailing>
				<UButton
					v-if="query?.length"
					color="neutral"
					variant="link"
					size="sm"
					icon="i-lucide-circle-x"
					@click="query = ''"
				/>
				<UKbd value="/"/>
			</template>
		</UInput>
		<USelect
			v-model="sort"
			:items="sortOptions"
			:icon="sort.icon"
			value-key="id"
			class="w-3xs"
		/>
	</div>
</template>
