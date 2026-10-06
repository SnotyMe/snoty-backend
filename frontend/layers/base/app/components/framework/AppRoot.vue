<i18n lang="yaml">
en:
  goto: Go to
de:
  goto: Gehe zu
</i18n>

<script setup lang="ts">
import AppSidebar from "./AppSidebar.vue"
import type { NavigationMenuItem, CommandPaletteGroup, CommandPaletteItem, DropdownMenuItem } from "@nuxt/ui"
import type { AuthComposable } from "#layers/base/app/types/auth.ts"

const { t } = useI18n({ useScope: "local" })

const props = defineProps<{
	links: [NavigationMenuItem[], NavigationMenuItem[]]
	userMenu: [DropdownMenuItem[], DropdownMenuItem[]]
	useAuth: AuthComposable
}>()

const groups = computed<CommandPaletteGroup[]>(() => [
	{
		id: "links",
		label: t("goto"),
		items: props.links.flat() as CommandPaletteItem[],
	},
])
</script>

<template>
	<UDashboardGroup :ui="{ base: 'overflow-y-auto' }" :persistent="false" unit="rem">
		<AppSidebar :links="links" :user-menu="userMenu" :use-auth="useAuth"/>

		<UDashboardSearch :groups="groups"/>

		<slot/>
	</UDashboardGroup>
</template>
