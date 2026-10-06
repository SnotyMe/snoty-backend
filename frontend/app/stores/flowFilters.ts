import type { FlowExecution, StandaloneWorkflow } from "~/api/backend/generated"

export const useFlowFiltersStore = defineStore("flowFilters", () => {
	const query = ref("")

	type SortableValue = StandaloneWorkflow[keyof StandaloneWorkflow] | FlowExecution[keyof FlowExecution]

	const sortOptions = computed(() => [
		{
			key: "name",
			desc: false,
			icon: "i-lucide-arrow-down-a-z",
		},
		{
			key: "name",
			desc: true,
			icon: "i-lucide-arrow-down-z-a",
		},
		{
			key: "lastExecution",
			desc: true,
			icon: "i-lucide-clock-arrow-down",
		},
		{
			key: "lastExecution",
			desc: false,
			icon: "i-lucide-clock-arrow-up",
		},
		{
			key: "modifiedAt",
			desc: true,
			icon: "i-lucide-rotate-ccw-clock",
		},
	].map(it => ({
		...it,
		id: { key: it.key, desc: it.desc, icon: it.icon },
	})))

	const sort = ref(sortOptions.value[0]!.id)

	function sorter(executions: Ref<Map<string, FlowExecution>>) {
		return (a: StandaloneWorkflow, b: StandaloneWorkflow) => {
			const { key, desc } = sort.value

			let aValue: SortableValue = a[key as keyof typeof a]
			let bValue: SortableValue = b[key as keyof typeof b]

			if (key === "lastExecution") {
				aValue = executions.value.get(a.id)?.timestamp
				bValue = executions.value.get(b.id)?.timestamp
			}

			if (aValue === bValue || aValue == undefined || bValue == undefined) return 0
			else if (desc) return aValue > bValue ? -1 : 1
			else return aValue < bValue ? -1 : 1
		}
	}

	return {
		query,
		sortOptions,
		sort,
		sorter,
	}
})
