<script setup lang="ts">
import FlowCreateButton from "~/components/flow/overview/FlowCreateButton.vue"
import FlowImportButton from "~/components/flow/overview/FlowImportButton.vue"

const createButton = useTemplateRef("createButton")

defineExpose({
	openCreate: () => createButton.value!.open(),
})
</script>

<template>
	<FlowImportButton/>
	<FlowCreateButton ref="createButton"/>
</template>
