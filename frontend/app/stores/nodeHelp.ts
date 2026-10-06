import type { NodeMetadata } from "~/api/backend/generated"
import type { NodeDocs } from "~/types/docs.ts"

export const useNodeHelpStore = defineStore("nodeHelp", () => {
	const { public: { docsBaseUrl } } = useRuntimeConfig()
	const { locale, defaultLocale } = useI18n()

	const selectedMetadata = ref<NodeMetadata>()

	const clearHelp = () =>
		selectedMetadata.value = undefined

	const toggleHelpFor = (metadata: NodeMetadata) => {
		if (selectedMetadata.value?.type === metadata.type) {
			clearHelp()
			return
		}

		selectedMetadata.value = metadata
	}

	const { data: docsSitemap } = useAsyncData("docsSitemap", () =>
		fetch(`${docsBaseUrl}/sitemap-nodes`).then(res => res.json() as Promise<NodeDocs>),
	)

	const getDocsUrl = (metadata: NodeMetadata) => {
		const sitemap = docsSitemap.value
		if (!sitemap) return

		const nodeSitemap = sitemap[metadata.type]
		if (!nodeSitemap) return

		let nodePath = nodeSitemap[locale.value] ?? nodeSitemap[defaultLocale]
		if (!nodePath) return
		if (nodePath.startsWith("/")) nodePath = nodePath.substring(1)

		return `${docsBaseUrl}/${nodePath}`
	}

	return {
		selectedMetadata,
		toggleHelpFor,
		clearHelp,
		docsSitemap,
		getDocsUrl,
	}
})
