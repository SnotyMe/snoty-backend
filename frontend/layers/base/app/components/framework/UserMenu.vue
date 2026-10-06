<script setup lang="ts">
import type { AuthComposable } from "#layers/base/app/types/auth.ts"
import type { DropdownMenuItem } from "@nuxt/ui"

const props = defineProps<{
	items: DropdownMenuItem[][]
	useAuth: AuthComposable
	collapsed?: boolean
}>()

const { user } = props.useAuth()
</script>

<template>
	<UDropdownMenu
		:items="items"
		:content="{ align: 'center', collisionPadding: 12 }"
		:ui="{ content: collapsed ? 'w-48' : 'w-(--reka-dropdown-menu-trigger-width)' }"
	>
		<UButton
			:avatar="{
				text: (user?.firstName?.[0] ?? '') + (user?.lastName?.[0] ?? ''),
			}"
			:label="collapsed ? undefined : user?.name"
			:trailing-icon="collapsed ? undefined : 'i-lucide-chevrons-up-down'"
			color="neutral"
			variant="ghost"
			block
			:ui="{
				trailingIcon: 'text-dimmed',
			}"
		/>

		<template #chip-leading="{ item }">
			<div class="inline-flex items-center justify-center shrink-0 size-5">
				<span
					class="rounded-full ring ring-bg bg-(--chip-light) dark:bg-(--chip-dark) size-2"
					:style="{
						'--chip-light': `var(--color-${(item as any).chip}-500)`,
						'--chip-dark': `var(--color-${(item as any).chip}-400)`,
					}"
				/>
			</div>
		</template>
	</UDropdownMenu>
</template>
