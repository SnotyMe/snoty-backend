<script setup lang="ts">
import AppLocaleSelector from "#layers/base/app/components/framework/AppLocaleSelector.vue"
import AppLogo from "#layers/base/app/components/framework/AppLogo.vue"
</script>

<template>
	<div class="absolute top-0 left-0 p-2 flex gap-2">
		<AppLogo/>
	</div>
	<div class="absolute top-0 right-0 p-2 flex gap-2">
		<UColorModeButton/>
		<AppLocaleSelector/>
	</div>
	<div class="flex items-center justify-center w-screen h-screen">
		<slot/>
	</div>
</template>
