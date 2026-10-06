import { type BaseAPI, Configuration, type FetchParams } from "~/api/backend/generated"
import { injectAuth } from "~/utils/fetch.ts"

export const getBaseUrl = (): string => {
	const config = useRuntimeConfig()
	return import.meta.server
		? config.internalApiBaseUrl ?? config.public.apiBaseUrl
		: config.public.apiBaseUrl
}

export const getApiConfig = () => new Configuration({
	basePath: getBaseUrl(),
	middleware: [
		{
			pre: async (context): Promise<FetchParams> => {
				if (context.init.credentials === "omit") return context

				return {
					...context,
					init: injectAuth(context.init),
				}
			},
		},
	],
})

export function useApi<T extends BaseAPI>(builder: new (config: Configuration) => T) {
	const config = getApiConfig()
	return new builder(config)
}
