<script setup lang="ts">
// TODO: totally re-do (converted from Svelte)
interface PopupProps {
	isOpen: boolean
	width?: string
	height?: string
	class?: string
	innerClass?: string
	verticalAlign?: "top" | "bottom"
	horizontalAlign?: "left" | "right"
}

const props = defineProps<PopupProps>()

const verticalAlign = computed(() => props.verticalAlign ?? "top")

const containerStyle = computed(() => ({
	[props.horizontalAlign ?? "left"]: "var(--x-padding)",
	[verticalAlign.value]: "var(--y-padding)",
	width: props.width,
	height: props.height,
}))
</script>

<template>
	<div
		v-show="isOpen"
		id="container"
		:class="[
			horizontalAlign === 'right' && 'rtl',
			props.class,
			'z-10 p-2 flow-node card bg-muted border border-accented rounded overflow-hidden block',
		]"
		:style="containerStyle"
	>
		<div :class="innerClass">
			<slot/>
		</div>
	</div>
</template>

<style scoped>
#container {
	position: absolute;

	--y-padding: calc(var(--flow-control-padding) + 0.5em + var(--flow-control-button-size));
	--x-padding: var(--flow-control-padding);

	max-width: calc(100% - 2 * var(--x-padding));
	max-height: calc(100% - var(--y-padding) - var(--flow-control-padding));

	user-select: text;
	-moz-user-select: text;
	-webkit-user-select: text;

	resize: both;
}

#container.rtl {
	direction: rtl;
}

#container > div {
	direction: ltr;
	height: 100%;
	overflow-y: auto;
}
</style>
