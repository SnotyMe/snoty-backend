<script setup lang="ts">
import { useNodeHelpStore } from "~/stores/nodeHelp.ts"

const nodeHelpStore = useNodeHelpStore()
const { selectedMetadata } = storeToRefs(nodeHelpStore)
const { clearHelp, getDocsUrl } = nodeHelpStore
</script>

<template>
	<UDashboardSidebar
		v-if="selectedMetadata"
		collapsible
		resizable
		:min-size="22"
		:default-size="25"
		:max-size="50"
		side="right"
		toggle-side="left"
		:ui="{ root: 'border-s border-s-default', body: 'px-1 py-0' }"
		@update:collapsed="clearHelp"
	>
		<template #default="{ collapse }">
			<div class="absolute h-full flex -ml-5 items-center">
				<UButton
					icon="i-lucide-x"
					variant="subtle"
					class="z-5 rounded-full"
					size="sm"
					@click="collapse(true)"
				/>
			</div>

			<iframe
				:src="getDocsUrl(selectedMetadata)"
				class="size-full bg-default"
			/>
		</template>
	</UDashboardSidebar>
</template>
