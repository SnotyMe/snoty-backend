<script setup lang="ts">
import type { NavigationMenuItem } from "@nuxt/ui"

const links = computed(() => [
	[
		{
			label: $t("navigation.page.settings.general"),
			icon: "i-lucide-user",
			to: "/settings",
			exact: true,
		},
	],
	[
		{
			label: $t("navigation.page.documentation"),
			icon: "i-lucide-book-open",
			to: "https://docs.snoty.me",
			target: "_blank",
		},
	],
] satisfies NavigationMenuItem[][])
</script>

<template>
	<UDashboardPanel id="settings" :ui="{ body: 'lg:py-12' }">
		<template #header>
			<UDashboardNavbar title="Settings"/>

			<UDashboardToolbar>
				<UNavigationMenu :items="links" highlight class="-mx-3 flex-1"/>
			</UDashboardToolbar>
		</template>

		<template #body>
			<div class="flex flex-col gap-4 sm:gap-6 lg:gap-12 w-full lg:max-w-2xl mx-auto">
				<NuxtPage/>
			</div>
		</template>
	</UDashboardPanel>
</template>
