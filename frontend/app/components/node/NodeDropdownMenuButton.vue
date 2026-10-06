<script setup lang="ts">
import type { DropdownMenuItem } from "@nuxt/ui"
import type { StandaloneNode } from "~/api/backend/generated"

const props = defineProps<{
	node: StandaloneNode
}>()

const items = computed<DropdownMenuItem[]>(() => [
	{
		label: $t("ui.copyId"),
		icon: "i-lucide-copy",
		onSelect: () => navigator.clipboard.writeText(props.node.id),
	},
])
</script>

<template>
	<UDropdownMenu :items="items">
		<UButton icon="i-lucide-ellipsis-vertical" color="neutral" variant="link"/>
	</UDropdownMenu>
</template>
