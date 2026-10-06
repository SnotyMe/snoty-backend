<script setup lang="ts">
import type { SchemaField } from "~/api/backend/generated"

defineProps<{
	field: SchemaField
}>()

const modelValue = defineModel<string>({ required: true })

const showCensored = ref(false)
</script>

<template>
	<UInput
		v-model="modelValue"
		:type="field.censored && !showCensored ? 'password' : 'text'"
		variant="outline"
		:ui="{ base: 'bg-transparent' }"
		class="w-full"
	>
		<template v-if="field.censored" #trailing>
			<UButton
				color="neutral"
				variant="link"
				size="sm"
				:icon="showCensored ? 'i-lucide-eye-off' : 'i-lucide-eye'"
				:aria-label="showCensored ? 'Hide password' : 'Show password'"
				:aria-pressed="showCensored"
				aria-controls="password"
				class="cursor-pointer"
				@click="showCensored = !showCensored"
			/>
		</template>
	</UInput>
</template>
