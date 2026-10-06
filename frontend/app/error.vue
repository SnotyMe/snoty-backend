<script setup lang="ts">
import type { NuxtError } from "#app"
import AppRoot from "#layers/base/app/components/framework/AppRoot.vue"
import { useNavigation } from "~/composables/framework/useNavigation.ts"
import { useUserMenu } from "~/composables/framework/useUserMenu.ts"

defineProps<{
	error: NuxtError
}>()

const { links } = useNavigation()
const { items: userMenu } = useUserMenu()
</script>

<template>
	<AppRoot :links="links" :user-menu="userMenu" :use-auth="useAuth">
		<UError :error="error" :ui="{ root: 'w-full' }"/>
	</AppRoot>
</template>
