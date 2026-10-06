<script setup>
const { locale, setLocale, locales } = useI18n()
</script>

<template>
	<ULocaleSelect
		:model-value="locale"
		:locales="locales"
		@update:model-value="it => setLocale(it)"
	/>
</template>
