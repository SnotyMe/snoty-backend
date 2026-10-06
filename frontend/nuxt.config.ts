import { locales } from "./shared/i18n.ts"

const isStaticFileHosting = process.env.FRONTEND_STATIC_FILE_HOSTING === "true"
const apiBaseUrl = process.env.API_BASE_URL ?? (() => {
	if (isStaticFileHosting) return "/api"
	if (import.meta.dev || process.env.NODE_ENV === "development") return "http://localhost:8080/api"

	return undefined
})()

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
	extends: [
		"layers/base",
	],

	modules: [
		"@nuxt/eslint",
		"@nuxt/ui",
		"@nuxt/icon",
		"@nuxtjs/i18n",
		"@nuxtjs/robots",
		"@vueuse/nuxt",
		"nuxt-codemirror",
		"@pinia/nuxt",
	],

	ssr: !isStaticFileHosting,

	devtools: {
		enabled: true,
	},

	app: {
		baseURL: "/",
		buildAssetsDir: "_nuxt/",
	},

	css: [
		"~/assets/css/main.css",
	],

	vue: {
		runtimeCompiler: true,
	},

	site: {
		indexable: false,
	},

	runtimeConfig: {
		internalApiBaseUrl: process.env.INTERNAL_API_BASE_URL ?? apiBaseUrl,
		public: {
			apiBaseUrl,
			docsBaseUrl: process.env.DOCS_BASE_URL ?? "https://docs.snoty.me/embed",
		},
	},

	routeRules: {
	},

	compatibilityDate: "2026-04-01",

	nitro: {
		cloudflare: {
			wrangler: {
				name: "snoty-frontend",
				observability: {
					logs: {
						enabled: true,
					},
				},
			},
			deployConfig: true,
			nodeCompat: true,
		},
	},

	vite: {
		optimizeDeps: {
			include: [
				"@scalar/api-reference",
				"@vue-flow/background",
				"@vue-flow/controls",
				"@vue-flow/core",
				"@vue-flow/node-resizer",
				"cronstrue/i18n", // CJS
				"elkjs/lib/elk.bundled.js", // CJS
				"eventsource",
				"jwt-decode",
				"zod",
				"zod/locales",
			],
		},
	},

	eslint: {
		config: {
			stylistic: {
				braceStyle: "1tbs",
				indent: "tab",
				blockSpacing: true,
				quotes: "double",
			},
		},
	},

	i18n: {
		locales: locales.map(it => ({
			...it,
			file: `${it.code}.json5`,
		})),
		strategy: "no_prefix",

		defaultLocale: "en",
	},
})
