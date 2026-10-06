<script setup lang="ts">
import AppLogoHeader from "./AppLogoHeader.vue"
import AppLogo from "./AppLogo.vue"
import UserMenu from "./UserMenu.vue"
import type { DropdownMenuItem, NavigationMenuItem } from "@nuxt/ui"
import type { AuthComposable } from "#layers/base/app/types/auth.ts"

defineProps<{
	links: [NavigationMenuItem[], NavigationMenuItem[]]
	userMenu: [DropdownMenuItem[], DropdownMenuItem[]]
	useAuth: AuthComposable
}>()

const ui = (collapsed: boolean) => {
	const buttonPropsArray = ["p-1.5"]
	if (collapsed) buttonPropsArray.push("*:mx-auto")

	const buttonProps = buttonPropsArray.join(" ")

	return { button: { base: buttonProps }, navigationMenu: { link: buttonProps } }
}
</script>

<template>
	<UDashboardSidebar
		collapsible
		resizable
		:min-size="15"
		:default-size="15"
		:max-size="25"
		:ui="{ footer: 'border-t border-default' }"
		class="bg-muted min-w-12"
	>
		<template #header="{ collapsed }">
			<div v-if="!collapsed" class="w-full flex items-center justify-between">
				<AppLogoHeader class="shrink-0 size-12"/>
				<UDashboardSidebarCollapse/>
			</div>
			<AppLogo v-else class="absolute"/> <!-- absolute ignores single-pixel cutoff => no layout shift -->
		</template>

		<template #default="{ collapsed }">
			<UTheme :ui="ui(collapsed)">
				<div class="flex flex-col">
					<UDashboardSidebarCollapse v-if="collapsed" :size="collapsed ? 'sm' : 'md'"/>
					<UDashboardSearchButton :collapsed="collapsed" :size="collapsed ? 'sm' : 'md'"/>
				</div>

				<UNavigationMenu
					:collapsed="collapsed"
					:items="links[0]"
					:ui="{ link: !collapsed && 'py-4', label: 'pl-1 pb-0' }"
					orientation="vertical"
				/>

				<UNavigationMenu
					:collapsed="collapsed"
					:items="links[1]"
					orientation="vertical"
					class="mt-auto"
				/>
			</UTheme>
		</template>

		<template #footer="{ collapsed }">
			<UserMenu :items="userMenu" :use-auth="useAuth" :collapsed="collapsed"/>
		</template>
	</UDashboardSidebar>
</template>
