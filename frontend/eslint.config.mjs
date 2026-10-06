// @ts-check
import withNuxt from "./.nuxt/eslint.config.mjs"
import jsonc from "eslint-plugin-jsonc"

export default withNuxt(
	{
		rules: {
			"vue/html-closing-bracket-spacing": [
				"error",
				{
					startTag: "never",
					endTag: "never",
					selfClosingTag: "never",
				},
			],
			"vue/max-attributes-per-line": [
				"warn",
				{
					singleline: 3,
				},
			],
			"vue/singleline-html-element-content-newline": [
				"error",
				{
					ignores: [
						"pre",
						"textarea",
						"p",
						"h1",
						"h2",
						"h3",
						"h4",
						"h5",
						"h6",
					],
				},
			],
			"vue/no-multiple-template-root": [
				"off",
			],
			"object-curly-spacing": [
				"error",
				"always",
			],
			"array-element-newline": [
				"error",
				{ multiline: true, minItems: 2 },
			],
			"comma-dangle": [
				"error",
				"always-multiline",
			],
			"no-irregular-whitespace": ["off"],
		},
	},
	{
		files: ["app/components/form/*"],
		rules: {
			"vue/require-prop-types": ["off"],
		},
	},
	{
		ignores: [
			"app/components/vue-bits/**", // full of non-conformant code using different formatting opinions
			"app/api/*/generated/**",
		],
	},
	...jsonc.configs["flat/recommended-with-json5"],
	{
		files: [
			"**/*.json5",
		],
		rules: {
			"jsonc/comma-dangle": [
				"error",
				"always-multiline",
			],
			"jsonc/indent": [
				"error",
				2,
			],
			"jsonc/quote-props": [
				"error",
				"always",
			],
		},
	},
	{
		ignores: [
			"app/components/vue-bits/**", // full of non-conformant code using different formatting opinions
		],
	},
)
