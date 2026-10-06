export const useDebouncedAction = <T extends (...args: never[]) => Promise<unknown>>(
	func: T,
	options: {
		debounce: number
		maxWait: number
	},
) => {
	const isUpdating = ref(false)
	const isAwaitingUpdate = ref(false)
	const error = ref<Error>()

	const { isPending: isUpdateDone, start: onUpdateDone } = useTimeout(1000, { controls: true, immediate: false })

	const debouncedFunction = useDebounceFn(async (...args: Parameters<T>) => {
		try {
			isUpdating.value = true
			error.value = undefined
			await func(...args)
		} catch (e) {
			error.value = e as never
		} finally {
			isUpdating.value = false
			isAwaitingUpdate.value = false
			onUpdateDone()
		}
	}, options.debounce, { maxWait: options.maxWait })

	const call = async (...args: Parameters<T>) => {
		isAwaitingUpdate.value = true
		await debouncedFunction(...args)
	}

	return {
		call,
		isUpdating,
		isAwaitingUpdate,
		error,
		isUpdateDone,
	}
}
