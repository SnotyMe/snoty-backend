<script setup lang="ts">
import DateTimeFormat from "~/components/primitives/DateTimeFormat.vue"
import type { NodeLogEntryDto } from "~/api/backend/generated"

defineProps<{
	log: NodeLogEntryDto
}>()

const expanded = ref(false)
let lastWasContainer = false
function toggleExpanded(target: "container" | "handle") {
	if (target === "container") {
		// it only acted if it was closed previously
		lastWasContainer = !expanded.value
		expanded.value = true
	} else if (target == "handle") {
		// prevents insta-closing when the container just opened it
		if (!lastWasContainer) {
			expanded.value = !expanded.value
		}
		lastWasContainer = false
	}
}
</script>

<template>
	<tr class="logline flex w-full overflow-auto" @mousedown="toggleExpanded('container')">
		<td class="level" :style="`--log-level: var(--color-log-${log.level.toLowerCase()})`"/>
		<td class="pl-1 cursor-pointer flex items-center" @mouseup="toggleExpanded('handle')">
			<UIcon class="size-5" :name="expanded ? 'i-lucide-chevron-down' : 'i-lucide-chevron-right'"/>
		</td>
		<td class="text-nowrap pr-3">
			<DateTimeFormat relative="NEVER" :date="log.timestamp"/>
		</td>
		<td class="w-0 grow message" :class="{ expanded }">
			{{ log.message }}
		</td>
	</tr>
</template>

<style>
tr {
	vertical-align: top;
}

.level {
	position: relative;
	padding-right: 0.1em;

	&::after {
		content: "";
		background-color: var(--log-level);
		display: block;
		position: absolute;
		top: 0.1em;
		bottom: 0.1em;
		left: 0.2em;
		width: 0.25em;
	}
}

td.message {
	white-space: nowrap;
	overflow: clip;
	text-overflow: ellipsis;

	&.expanded {
		white-space: normal;
		/* leave some more space for the expanded message */
		padding-bottom: 0.5em;
	}
}

tr.logline:not(:has(td.message.expanded)) {
	cursor: pointer;
}
</style>
