<i18n lang="yaml">
en:
  longerThanExpected: This operation is taking longer than expected...
  unrealisticallyLong: This is taking unrealistically long, something might have gone wrong.
de:
  longerThanExpected: Dieser Vorgang dauert länger als erwartet...
  unrealisticallyLong: Dies dauert unrealistisch lange, möglicherweise ist etwas schiefgelaufen.
</i18n>

<script setup lang="ts">
const longerThanExpected = useTimeout(5000)
const unrealisticallyLong = useTimeout(10000)

const { t } = useI18n({ useScope: "local" })
</script>

<template>
	<span v-if="unrealisticallyLong" class="text-error">
		{{ t('unrealisticallyLong') }}
	</span>
	<span v-else-if="longerThanExpected" class="text-warning">
		{{ t('longerThanExpected') }}
	</span>
</template>
