export default defineAppConfig({
	ui: {
		accordion: {
			slots: {
				trigger: "hover:!bg-elevated/50 hover:rounded p-2",
				content: "px-4",
			},
		},
		input: {
			compoundVariants: [
				{
					variant: "ghost",
					color: "primary",
					class: "px-2 py-1 focus-visible:ring-2 focus-visible:ring-primary",
				},
			],
		},
		empty: {
			slots: {
				root: "p-3 sm:p-5 lg:p-6",
			},
		},
		button: {
			slots: {
				base: "cursor-pointer",
			},
		},
	},
})
