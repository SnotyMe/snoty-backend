<i18n lang="yaml">
en:
  status:
    SUCCESS:
      title: Flow succeeded!
    FAILED:
      title: Flow failed!
      description: Take a look at the journal to figure out why.
</i18n>

<script setup lang="ts">
import { useFlowExecutionEvent } from "~/composables/flow/useFlowExecutionSse.ts"
import FlowExecutionStatus from "~/components/flow/FlowExecutionStatus.vue"

const { t } = useI18n({ useScope: "local" })

const toast = useToast()

useFlowExecutionEvent("FlowEnded", (_, data) => {
	toast.add({
		title: t(`status.${data.status}.title`),
		description: tOrUndefined(t, `status.${data.status}.description`),
		color: data.status === "SUCCESS" ? "success" : "error",
		icon: h(FlowExecutionStatus, { value: data.status, variant: "ghost" }),
		ui: {
			root: "flex items-center justify-center fuck",
		},
	})
})
</script>

<!-- eslint-disable-next-line vue/valid-template-root -->
<template/>
