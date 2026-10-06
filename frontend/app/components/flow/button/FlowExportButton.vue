<i18n lang="yaml">
en:
  form:
    withSensitiveData:
      label: With sensitive data
      description: Will include secret data and Credential references. No Credential data will be exported.
      warning: Do not share this file publicly!
  download: Download
</i18n>

<script setup lang="ts">
import { ControlButton } from "@vue-flow/controls"
import { downloadText } from "~/utils/download"
import { type ExportOptions, FlowApi, type StandaloneWorkflow } from "~/api/backend/generated"
import { useApi } from "~/api/backend/api-clients.ts"

const props = defineProps<{
	flow: StandaloneWorkflow
}>()

const { t } = useI18n({ useScope: "local" })

const form = ref<ExportOptions>({
	withSensitiveData: false,
})

const flowApi = useApi(FlowApi)

async function ondownload() {
	const opts = await flowApi.wiringFlowIdExportPostRequestOpts({ id: props.flow.id, exportOptions: form.value })
	const result = await flowApi.request(opts)
	const blob = await result.blob()

	downloadText(`${props.flow.name}.json`, blob)
}
</script>

<template>
	<UPopover :content="{ align: 'start', side: 'left', sideOffset: 0 }" :ui="{ content: 'p-2' }">
		<ControlButton>
			<UIcon name="i-lucide-share-2"/>
		</ControlButton>
		<template #content>
			<UForm :state="form" @submit="ondownload">
				<UFormField
					name="withSensitiveData"
					:label="t('form.withSensitiveData.label')"
					:description="t('form.withSensitiveData.description')"
					orientation="horizontal"
				>
					<UCheckbox v-model="form.withSensitiveData"/>
				</UFormField>

				<div class="mt-2 flex justify-end">
					<UButton
						class="r-0"
						type="submit"
						icon="i-lucide-download"
						loading-auto
					>
						{{ t('download') }}
					</UButton>
				</div>
			</UForm>
		</template>
	</UPopover>
</template>
