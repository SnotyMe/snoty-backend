import type { WatchSource, WatchCallback, WatchOptions } from "vue"
import { watchDebounced, type WatchDebouncedOptions } from "@vueuse/core"

export const useOnChanged = <T>(
	data: WatchSource<T>,
	onChange: (value: T) => void,
	options?: ({
		debounced?: boolean
	} & WatchDebouncedOptions<never>)
	| ({ debounced: undefined } & WatchOptions<never>),
) => {
	const getCurrentValue = () => {
		switch (typeof data) {
			case "function":
				return data()
			case "object":
				return data.value
			default:
				return data
		}
	}

	const snapshot = (value: T) => JSON.stringify(value)

	let lastValueSnapshot = snapshot(getCurrentValue())
	const watcher: WatchCallback<T> = (newValue) => {
		const newValueSnapshot = snapshot(newValue)
		if (newValueSnapshot === lastValueSnapshot) return

		lastValueSnapshot = newValueSnapshot
		onChange(newValue)
	}

	if (options?.debounced) {
		watchDebounced(data, watcher, options)
	} else {
		watch(data, watcher, options)
	}
}
