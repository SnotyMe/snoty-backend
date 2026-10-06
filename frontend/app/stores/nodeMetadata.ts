import { useAsyncData } from "#app"
import { useApi } from "~/api/backend/api-clients.ts"
import { NodeMetadataApi } from "~/api/backend/generated"

export const useNodeMetadataStore = defineStore("nodeMetadata", () => {
	const nodeMetadataApi = useApi(NodeMetadataApi)

	const { data: metadata, pending, execute } = useAsyncData("nodeMetadata", () =>
		nodeMetadataApi.wiringNodeMetadataGet(),
	)

	return {
		metadata,
		pending,
		execute,
	}
})
