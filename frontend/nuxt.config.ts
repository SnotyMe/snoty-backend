import { locales } from "./shared/i18n.ts"

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
		internalApiBaseUrl: process.env.INTERNAL_API_BASE_URL ?? process.env.API_BASE_URL,
		public: {
			apiBaseUrl: process.env.API_BASE_URL,
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
