<template>
	<ClientOnly>
		<ApiReference
			v-if="configuration"
			:configuration="configuration"
			style="--full-height: 100%"
			class="w-full flex"
		/>
		<UError
			v-else-if="status === 'error'"
			:error="{
				status: error?.status,
				statusMessage: 'Error fetching API Spec',
				message: error?.message,
			}"
			class="w-full"
		/>
	</ClientOnly>
</template>

<script lang="ts" setup>
import { useAuth } from "~/stores/auth"
import { ApiReference, type ApiReferenceConfiguration } from "@scalar/api-reference"
import "@scalar/api-reference/style.css"
import { getBaseUrl } from "~/api/backend/api-clients.ts"

const props = defineProps<{
	fileName: string
	spec: Promise<object>
}>()

const { data: finalSpec, pending, status, error } = useAsyncData(() => props.spec.then(mapSpec))

function mapSpec(originalSpec: object) {
	return {
		...originalSpec,
		servers: [{ url: getBaseUrl() }],
	}
}

const { accessToken } = storeToRefs(useAuth())

const configuration = computed<Partial<ApiReferenceConfiguration> | undefined>(() => (pending.value || status.value === "error")
	? undefined
	: ({
			content: finalSpec.value,
			hideDarkModeToggle: true,
			slug: props.fileName,
			showOperationId: true,
			onBeforeRequest: (async (request) => {
				request.requestBuilder.headers.set("Authorization", "Bearer " + accessToken.value)
			}) satisfies ApiReferenceConfiguration["onBeforeRequest"],
			mcp: {
				disabled: true,
			},
		}),
)
</script>

<style>
[class~="references-layout"] {
	--full-height: unset !important;
	min-height: unset !important;

	grid-template-rows: 0 minmax(0, 1fr) !important;
	grid-template-columns: 1fr auto !important;
	grid-template-areas:
		'header     header    '
		'rendered   navigation'
		'footer     footer    ' !important;
}

.scalar-api-reference {
	flex: 1;
	overflow-y: auto;
}

:root, .dark-mode, .light-mode {
	--scalar-color-1: var(--ui-text);
	--scalar-color-2: var(--ui-text-toned);
	--scalar-color-3: var(--ui-text-muted);
	--scalar-color-accent: var(--ui-text-primary);
	--scalar-background-1: var(--ui-bg);
	--scalar-background-2: var(--ui-bg-muted);
	--scalar-background-3: var(--ui-bg-elevated);
}
</style>
