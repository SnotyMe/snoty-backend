<script lang="ts" setup>
import { dracula } from "@uiw/codemirror-theme-dracula"
import { githubLight } from "@uiw/codemirror-theme-github"
import { liquid } from "@codemirror/lang-liquid"
import { json } from "@codemirror/lang-json"
import { EditorView } from "@codemirror/view"
import type { SchemaFieldDetailsPlaintextDetails } from "~/api/backend/generated"

const colorMode = useColorMode()

const props = defineProps<{
	details: SchemaFieldDetailsPlaintextDetails
}>()

const modelValue = defineModel<string>({ required: true })

function getLanguage() {
	switch (props.details.language) {
		case "json":
			return json()
		case "liquid":
			return liquid()
	}
}
</script>

<template>
	<div class="codemirror-wrapper font-normal overflow-auto text-left [&_.cm-editor]:h-full">
		<NuxtCodeMirror
			v-model="modelValue"
			:extensions="[EditorView.lineWrapping, getLanguage()].filter(it => it != undefined)"
			:theme="colorMode.value === 'dark' ? dracula : githubLight"
			class="resize-y max-w-full overflow-y-hidden"
			:auto-focus="false"
			:editable="true"
			:basic-setup="{ lineNumbers: true, highlightActiveLine: false }"
			:indent-with-tab="true"
		/>
	</div>
</template>
