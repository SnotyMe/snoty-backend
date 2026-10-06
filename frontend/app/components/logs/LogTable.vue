<script setup lang="ts">
import LogLine from "~/components/logs/LogLine.vue"
import type { NodeLogEntryDto } from "~/api/backend/generated"

defineProps<{
	logs: NodeLogEntryDto[]
}>()
</script>

<template>
	<div class="bg-elevated p-1 border border-inverted rounded-sm mb-2">
		<table class="border-spacing-0 w-full">
			<tbody>
				<LogLine v-for="log of logs" :key="log.id" :log="log"/>
			</tbody>
		</table>
	</div>
</template>
