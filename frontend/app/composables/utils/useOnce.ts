export const useOnce = (fn: () => unknown) => {
	let hasRun = false

	return () => {
		if (hasRun) return

		hasRun = true
		fn()
	}
}
