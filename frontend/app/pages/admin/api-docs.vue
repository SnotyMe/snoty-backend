<script setup lang="ts">
import Scalar from "~/components/utils/Scalar.client.vue"
import { useApi } from "~/api/backend/api-clients.ts"
import { DefaultApi } from "~/api/backend/generated"

const defaultApi = useApi(DefaultApi)
</script>

<template>
	<Scalar file-name="snoty-backend" :spec="defaultApi.openapiJsonGet() as never as Promise<object>"/>
</template>
