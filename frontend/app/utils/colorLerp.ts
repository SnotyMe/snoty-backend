export function calculateColor(ms: number) {
	const maxL = 0.95
	const minL = 0.45
	const thirtyDays = 30 * 24 * 60 * 60 * 1000

	const x = ms / (1000 * 60)
	const sigma = 200
	const gaussianDecay = Math.exp(-Math.pow(x / sigma, 2))

	const linearDecay = 1 - Math.min(ms / thirtyDays, 1)

	const finalRatio = (gaussianDecay * 0.8) + (linearDecay * 0.2)

	const L = minL + (maxL - minL) * finalRatio

	return `oklch(${L.toFixed(4)} 0 0)`
}
