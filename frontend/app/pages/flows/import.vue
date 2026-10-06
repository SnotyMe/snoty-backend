<i18n lang="yaml">
en:
  title: Import Flow
de:
  title: Flow importieren
</i18n>

<script setup lang="ts">
useHead({
	title: $t("title"),
})
</script>

<template>
	<UDashboardPanel id="flow-import" :ui="{ body: 'lg:py-12 mx-auto md:max-w-4xl w-full' }">
		<template #header>
			<UDashboardNavbar :title="$t('title')">
				<template #leading>
					<UButton
						icon="i-lucide-arrow-left"
						color="neutral"
						variant="ghost"
						to="/flows"
					/>
				</template>
			</UDashboardNavbar>
		</template>

		<template #body>
			<FlowImport/>
		</template>
	</UDashboardPanel>
	<NodeHelpSidebar/>
</template>
